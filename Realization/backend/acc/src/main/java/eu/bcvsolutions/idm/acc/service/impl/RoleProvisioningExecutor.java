package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.domain.MappingContext;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccRoleAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccRoleAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccRoleAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccRoleAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityTypeManager;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.system.entity.SystemEntityTypeRegistrable;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Service for do Role provisioning
 * 
 * @author svandav
 *
 */
@Service
@Qualifier(value = RoleProvisioningExecutor.NAME)
public class RoleProvisioningExecutor extends AbstractProvisioningExecutor<IdmRoleDto> {
 
	public static final String NAME = "roleProvisioningService";
	
	public static final String SYSTEM_ENTITY_TYPE = "ROLE";
	
	private final AccRoleAccountService roleAccountService;
	private final IdmRoleService roleService;
	
	@Autowired
	public RoleProvisioningExecutor(
			SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService, 
			IcConnectorFacade connectorFacade,
			SysSystemService systemService, 
			SysRoleSystemService roleSystemService,
			SysRoleSystemAttributeService roleSystemAttributeService, 
			SysSystemEntityService systemEntityService,
			AccAccountService accountService, 
			AccRoleAccountService roleAccountService,
			ProvisioningExecutor provisioningExecutor, 
			EntityEventManager entityEventManager, 
			SysSchemaAttributeService schemaAttributeService,
			SysSchemaObjectClassService schemaObjectClassService,
			SysSystemAttributeMappingService systemAttributeMappingService,
			IdmRoleService roleService,
			SysSystemEntityTypeManager systemEntityManager) {
		
		super(systemMappingService, attributeMappingService, connectorFacade, systemService, roleSystemService,
				roleSystemAttributeService, systemEntityService, accountService,
				provisioningExecutor, entityEventManager, schemaAttributeService, schemaObjectClassService,
				systemAttributeMappingService, roleService, systemEntityManager);
		//
		Assert.notNull(roleAccountService, "Service is required.");
		//
		this.roleAccountService = roleAccountService;
		this.roleService = roleService;
	}
	
	public void doProvisioning(AccAccountDto account) {
		Assert.notNull(account, "Account is required.");

		AccRoleAccountFilter filter = new AccRoleAccountFilter();
		filter.setAccountId(account.getId());
		List<AccRoleAccountDto> entityAccoutnList = roleAccountService.find(filter, null).getContent();
		if (entityAccoutnList == null) {
			return;
		}
		entityAccoutnList.stream().filter(entityAccount -> {
			return entityAccount.isOwnership();
		}).forEach((roleAccount) -> {
			doProvisioning(account, DtoUtils.getEmbedded(roleAccount, AccRoleAccount_.role, IdmRoleDto.class));
		});
	}
	
	@Override
	protected List<SysRoleSystemAttributeDto> findOverloadingAttributes(IdmRoleDto entity, SysSystemDto system,
			AccAccountDto account, String entityType) {
		// Overloading attributes is not implemented for RoleNode
		return new ArrayList<>();
	}
	
	@Override
	protected Object getAttributeValue(String uid, IdmRoleDto entity, AttributeMapping attribute, SysSystemDto system, MappingContext mappingContext,
									   AccAccountDto accountDto) {
		Object idmValue = super.getAttributeValue(uid, entity, attribute, system, mappingContext, accountDto);

		if (attribute.isEntityAttribute()
				&& RoleSynchronizationExecutor.ROLE_TYPE_FIELD.equals(attribute.getIdmPropertyName())) {
			// Role type enumeration we will do transform to String (name)
			if (idmValue instanceof RoleType) {
				return ((RoleType)idmValue).name();
			}
		}
		return idmValue;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected AccRoleAccountFilter createEntityAccountFilter() {
		return new AccRoleAccountFilter();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected AccRoleAccountService getEntityAccountService() {
		return roleAccountService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccRoleAccountDto();
	}

	@Override
	protected IdmRoleService getService() {
		return roleService;
	}
	
	@Override
	public String getSystemEntityType() {
		return SYSTEM_ENTITY_TYPE;
	}
	
	@Override
	public boolean supports(SystemEntityTypeRegistrable delimiter) {
		return delimiter.getSystemEntityCode().equals(SYSTEM_ENTITY_TYPE) && delimiter.isSupportsProvisioning();
	}
}
