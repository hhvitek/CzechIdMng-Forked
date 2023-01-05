package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakRecipientDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncIdentityConfigDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccRoleAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakRecipientFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.repository.SysSyncConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.AccRoleAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakRecipientService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentManager;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * Before role delete - deletes all role system mappings.
 * 
 * @author Radek Tomiška
 *
 */
@Component("accRoleDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class RoleDeleteProcessor extends CoreEventProcessor<IdmRoleDto> implements RoleProcessor {

	public static final String PROCESSOR_NAME = "role-delete-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RoleDeleteProcessor.class);
	//
	@Autowired private SysRoleSystemService roleSystemService;
	@Autowired private AccRoleAccountService roleAccountService;
	@Autowired private SysProvisioningBreakRecipientService provisioningBreakRecipientService;
	@Autowired private SysSyncConfigRepository syncConfigRepository;
	@Autowired private SysSyncConfigService syncConfigService;
	@Autowired private IdmRoleAssignmentManager roleAssignmentManager;

	public RoleDeleteProcessor() {
		super(RoleEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		boolean forceDelete = getBooleanProperty(PROPERTY_FORCE_DELETE, event.getProperties());
		IdmRoleDto role = event.getContent();
		//
		if (role.getId() == null) {
			return new DefaultEventResult<>(event, this);
		}
		//
		// delete mapped roles
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setRoleId(role.getId());
		roleSystemService.find(roleSystemFilter, null).forEach(roleSystem -> {
			// Identity-role clear relations to a role-system, but only on force-delete!
			if (forceDelete) {
				IdmRequestIdentityRoleFilter identityRoleFilter = new IdmRequestIdentityRoleFilter();
				identityRoleFilter.setRoleSystemId(roleSystem.getId());

				roleAssignmentManager.find(identityRoleFilter, null, (identityRole, service) -> {
							// Clear role-system. Identity-role will be removed in next processor.
							identityRole.setRoleSystem(null);
							service.saveInternal(identityRole);
						});

			}
			roleSystemService.delete(roleSystem);
		});
		//
		// delete relations on account (includes delete of account )
		AccRoleAccountFilter filter = new AccRoleAccountFilter();
		filter.setRoleId(role.getId());
		roleAccountService.find(filter, null).forEach(roleAccount -> {
			roleAccountService.delete(roleAccount);
		});
		//
		// remove all recipients from provisioning break
		deleteProvisioningRecipient(event.getContent().getId());
		//
		// delete link to sync identity configuration
		syncConfigRepository
			.findByDefaultRole(role.getId())
			.forEach(config -> {
				SysSyncIdentityConfigDto configDto = (SysSyncIdentityConfigDto) syncConfigService.get(config.getId());
				configDto.setDefaultRole(null);
				syncConfigService.save(configDto);
			});

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// right now before role delete
		return CoreEvent.DEFAULT_ORDER - 1;
	}

	@Override
	public boolean isDisableable() {
		return false;
	}

	/**
	 * Method remove all provisioning recipient for role id given in parameter
	 * 
	 * @param roleId
	 */
	private void deleteProvisioningRecipient(UUID roleId) {
		SysProvisioningBreakRecipientFilter filter = new SysProvisioningBreakRecipientFilter();
		filter.setRoleId(roleId);
		for (SysProvisioningBreakRecipientDto recipient : provisioningBreakRecipientService.find(filter, null)
				.getContent()) {
			LOG.debug("Remove recipient from provisioning break [{}]", recipient.getId());
			provisioningBreakRecipientService.delete(recipient);
		}
	}
}
