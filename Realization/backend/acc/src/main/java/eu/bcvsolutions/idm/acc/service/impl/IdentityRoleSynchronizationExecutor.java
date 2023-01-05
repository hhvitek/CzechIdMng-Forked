package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;

import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.dto.AccRoleAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccRoleAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccRoleAccountService;
import eu.bcvsolutions.idm.acc.service.api.EntityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationEntityExecutor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.CorrelationFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;

@Component
public class IdentityRoleSynchronizationExecutor extends AbstractSynchronizationExecutor<IdmIdentityRoleDto>
		implements SynchronizationEntityExecutor {

	@Autowired
	private IdmIdentityRoleService service;
	@Autowired
	private AccRoleAccountService identityRoleAccountService;

	public static final String SYSTEM_ENTITY_TYPE = "IDENTITY_ROLE";
	
	/**
	 * Call provisioning for given account
	 * 
	 * @param entity
	 * @param entityType
	 * @param logItem
	 */
	@Override
	protected void callProvisioningForEntity(IdmIdentityRoleDto entity, String entityType, SysSyncItemLogDto logItem) {
		addToItemLog(logItem,
				MessageFormat.format(
						"Call provisioning (process IdentityRoleEvent.UPDATE) for identity-role ({0}).",
						entity.getId()));
		entityEventManager.process(new IdentityRoleEvent(AbstractRoleAssignmentEvent.RoleAssignmentEventType.UPDATE, entity)).getContent();
	}
	
	/**
	 * Save entity
	 * @param entity
	 * @param skipProvisioning
	 * @return
	 */
	@Override
	protected IdmIdentityRoleDto save(IdmIdentityRoleDto entity, boolean skipProvisioning, SynchronizationContext context) {		
		// Content will be set in service (we need do transform entity to DTO). 
		// Here we set only dummy DTO (null content is not allowed)
		EntityEvent<IdmIdentityRoleDto> event = new IdentityRoleEvent(
				service.isNew(entity) ? AbstractRoleAssignmentEvent.RoleAssignmentEventType.CREATE : AbstractRoleAssignmentEvent.RoleAssignmentEventType.UPDATE,
				entity, 
				ImmutableMap.of(ProvisioningService.SKIP_PROVISIONING, skipProvisioning));
		
		return service.publish(event).getContent();
	}

	@Override
	protected EntityAccountFilter createEntityAccountFilter() {
		return new AccRoleAccountFilter();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected EntityAccountService<EntityAccountDto, EntityAccountFilter> getEntityAccountService() {
		return  (EntityAccountService) identityRoleAccountService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccRoleAccountDto();
	}

	@Override
	protected IdmIdentityRoleService getService() {
		return service;
	}
	
	@Override
	protected CorrelationFilter getEntityFilter(SynchronizationContext context) {
		return new IdmIdentityRoleFilter();
	}

	@Override
	protected IdmIdentityRoleDto createEntityDto() {
		return new IdmIdentityRoleDto();
	}
	
	@Override
	public String getSystemEntityType() {
		return SYSTEM_ENTITY_TYPE;
	}
}
