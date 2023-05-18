package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Identity role account management after save
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes account management and provisioning after identity role is saved.")
public class IdentityRoleSaveProvisioningProcessor extends AbstractEntityEventProcessor<AbstractRoleAssignmentDto> {

	public static final String PROCESSOR_NAME = "identity-role-save-provisioning-processor";
	private static final Logger LOG = LoggerFactory.getLogger(IdentityRoleSaveProvisioningProcessor.class);
	//
	@Autowired private EntityEventManager entityEventManager;
	@Autowired private AccAccountManagementService accountManagementService;
	@Autowired private ProvisioningService provisioningService;
	@Autowired private IdmRoleAssignmentManager roleAssignmentManager;
	@Autowired private AccAccountService accountService;
	

	public IdentityRoleSaveProvisioningProcessor() {
		super(CoreEventType.NOTIFY);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	/**
	 *  Account management should be executed from parent event - request. 
	 *  Look out, request event is already closed, when asynchronous processing is disabled.
	 */
	@Override
	public boolean conditional(EntityEvent<AbstractRoleAssignmentDto> event) {
		if (!super.conditional(event)) {
			return false;
		}
		if (this.getBooleanProperty(IdmAccountDto.SKIP_PROPAGATE, event.getProperties())) {
			// Skip account management
			return false;
		}
		if (event.getRootId() == null) {
			// parent event not exists - provisioning is needed.
			return true;
		}
		if (!entityEventManager.isRunnable(event.getRootId())) {
			// parent event is already executed - provisioning is needed.
			return true;
		}
		// check parent event is role request and is running - we can skip provisioning. Provisioning occurs after role request is completely executed
		IdmEntityEventDto parentEvent = entityEventManager.getEvent(event.getRootId());
		if (parentEvent.getOwnerType().equals(entityEventManager.getOwnerType(IdmRoleRequestDto.class)))  {
			// role request is processed
			// prevent to skip provisioning for other events (e.g. sub roles are processed).
			return false;
		}
		// 
		return true;
	}
 
	@Override
	public EventResult<AbstractRoleAssignmentDto> process(EntityEvent<AbstractRoleAssignmentDto> event) {
		AbstractRoleAssignmentDto roleAssignment = event.getContent();
		
		// If for this role doesn't exists any mapped system, the is provisioning useless!
		UUID roleId = roleAssignment.getRole();
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setRoleId(roleId);
		
		// I had to turn off this optimization, because we need to recalculate (remove
		// identity-account) identity-role mapped on role where mapped system no longer
		// exists.

		// long numberOfMappedSystem = roleSystemService.count(roleSystemFilter);
		// if(numberOfMappedSystem == 0) {
		// return new DefaultEventResult<>(event, this);
		// }
				
		IdmIdentityDto identity = roleAssignmentManager.getServiceForAssignment(roleAssignment).getRelatedIdentity(roleAssignment);
		
		// TODO: Basically this doesn't work, because identity-role is already created
		// (not committed). So every times will be called update ACM, but this isn't big issue (only
		// some redundant selects will be executed).
		// TODO: event.getParentType() === CREATE can be used instead
		boolean isNew = roleAssignmentManager.getServiceForAssignment(roleAssignment).isNew(roleAssignment);
		
		LOG.debug("Call account management for identity [{}] and identity-role [{}]", identity.getUsername(), roleAssignment);
		
		List<UUID> accountIds = null;
		if(isNew) {
			accountIds = accountManagementService.resolveNewIdentityRoles(identity, roleAssignment);
		} else {
			accountIds = accountManagementService.resolveUpdatedIdentityRoles(identity, roleAssignment);
		}
		
		if (accountIds != null) {
			accountIds.forEach(accountId -> {
				AccAccountDto account = accountService.get(accountId);
				if (account != null) { // Account could be null (was deleted).
					LOG.debug("Call provisioning for identity [{}] and account [{}]", identity.getUsername(), account.getUid());
					provisioningService.doProvisioning(account, identity);
				}
			});	
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}