package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.MappingContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute_;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem_;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping_;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity_;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.exception.SystemEntityNotFoundException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityTypeManager;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.Contextable;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormValue_;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Abstract service for do provisioning
 * 
 * @author svandav
 * @author Radek Tomiška
 * @author Roman Kucera
 *
 * @param <DTO>
 *            provisioned dto
 */
public abstract class AbstractProvisioningExecutor<DTO extends AbstractDto> implements ProvisioningEntityExecutor<DTO> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractProvisioningExecutor.class);
	protected final SysSystemMappingService systemMappingService;
	protected final SysSystemAttributeMappingService attributeMappingService;
	private final IcConnectorFacade connectorFacade;
	private final SysSystemService systemService;
	protected final SysRoleSystemAttributeService roleSystemAttributeService;
	private final SysSystemEntityService systemEntityService;
	protected final AccAccountService accountService;
	private final ProvisioningExecutor provisioningExecutor;
	private final EntityEventManager entityEventManager;
	private final SysSchemaAttributeService schemaAttributeService;
	private final SysSchemaObjectClassService schemaObjectClassService;
	private final SysSystemAttributeMappingService systemAttributeMappingService;
	protected final SysRoleSystemService roleSystemService;
	private final IdmRoleService roleService;
	private final SysSystemEntityTypeManager systemEntityManager;

	@Autowired
	private FormService formService;

	@Autowired
	public AbstractProvisioningExecutor(SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService, IcConnectorFacade connectorFacade,
			SysSystemService systemService, SysRoleSystemService roleSystemService,
			SysRoleSystemAttributeService roleSystemAttributeService, SysSystemEntityService systemEntityService,
			AccAccountService accountService, ProvisioningExecutor provisioningExecutor,
			EntityEventManager entityEventManager, SysSchemaAttributeService schemaAttributeService,
			SysSchemaObjectClassService schemaObjectClassService,
			SysSystemAttributeMappingService systemAttributeMappingService, IdmRoleService roleService,
			SysSystemEntityTypeManager systemEntityManager) {

		Assert.notNull(systemMappingService, "Service is required.");
		Assert.notNull(attributeMappingService, "Service is required.");
		Assert.notNull(connectorFacade, "Connector facade is required.");
		Assert.notNull(systemService, "Service is required.");
		Assert.notNull(roleSystemService, "Service is required.");
		Assert.notNull(roleSystemAttributeService, "Service is required.");
		Assert.notNull(systemEntityService, "Service is required.");
		Assert.notNull(accountService, "Service is required.");
		Assert.notNull(provisioningExecutor, "Provisioning executor is required.");
		Assert.notNull(entityEventManager, "Manager is required.");
		Assert.notNull(schemaAttributeService, "Service is required.");
		Assert.notNull(schemaObjectClassService, "Service is required.");
		Assert.notNull(systemAttributeMappingService, "Service is required.");
		Assert.notNull(roleService, "Service is required.");
		Assert.notNull(systemEntityManager, "Service is required.");
		//
		this.systemMappingService = systemMappingService;
		this.attributeMappingService = attributeMappingService;
		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
		this.roleSystemAttributeService = roleSystemAttributeService;
		this.systemEntityService = systemEntityService;
		this.accountService = accountService;
		this.provisioningExecutor = provisioningExecutor;
		this.entityEventManager = entityEventManager;
		this.schemaAttributeService = schemaAttributeService;
		this.schemaObjectClassService = schemaObjectClassService;
		this.systemAttributeMappingService = systemAttributeMappingService;
		this.roleSystemService = roleSystemService;
		this.roleService = roleService;
		this.systemEntityManager = systemEntityManager;
	}

	/**
	 * Returns entity type for this provisioning executor
	 * 
	 * @return
	 */
	protected String getEntityType() {
		return systemEntityManager.getSystemEntityByClass(getService().getDtoClass()).getSystemEntityCode();
	}
	
	/**
	 * Returns entity type for this provisioning executor.
	 * 
	 * @return
	 */
	public String getSystemEntityType() {
		// must be overridden in each executor
		return null;
	}

	public boolean supports(String delimiter) {
		return getEntityType() == delimiter;
	}

	@Override
	public void doProvisioning(AccAccountDto account) {
		Assert.notNull(account, "Account is required.");

		EntityAccountFilter filter = createEntityAccountFilter();
		filter.setAccountId(account.getId());
		filter.setOwnership(Boolean.TRUE);
		
		// Find first entity-account relation (we need Id of entity)
		List<? extends EntityAccountDto> entityAccoutnList = getEntityAccountService()
				.find(filter, PageRequest.of(0, 1)).getContent();
		if (entityAccoutnList.isEmpty()) {
			return;
		}
		EntityAccountDto entityAccountDto = entityAccoutnList.get(0);
		
		// Start provisioning
		doProvisioning(account, getService().get(entityAccountDto.getEntity()));
	}

	@Override
	public void doProvisioning(DTO dto) {
		Assert.notNull(dto, "DTO is required.");
		//
		EntityAccountFilter filter = createEntityAccountFilter();
		filter.setEntityId(dto.getId());
		filter.setOwnership(Boolean.TRUE);
		List<? extends EntityAccountDto> entityAccountList = this.getEntityAccountService().find(filter, null)
				.getContent();

		List<UUID> accounts = new ArrayList<>();
		entityAccountList.forEach(entityAccount -> {
			if (!accounts.contains(entityAccount.getAccount())) {
				accounts.add(entityAccount.getAccount());
			}
		});

		accounts.forEach(account -> {
			EntityAccountDto entityAccountDto = entityAccountList
					.stream()
					.filter(entityAccount -> account.equals(entityAccount.getAccount()))
					.findFirst()
					.orElseThrow(() -> new CoreException(String.format("No entity account found for account uuid %s", account)));

			AccAccountDto accountDto = DtoUtils.getEmbedded((AbstractDto) entityAccountDto,
					AccIdentityAccount_.account.getName(), AccAccountDto.class, null);
			if (accountDto == null) {
				accountDto = accountService.get(account);
			}

			// if account has no provisioning mapping, get one from account or system
			UUID systemMappingId = getSystemMappingForAccount(entityAccountDto, accountDto);
			if (systemMappingId != null) {
				accountDto.setSystemMapping(systemMappingId);
				accountDto = accountService.saveInternal(accountDto);
			}

			this.doProvisioning(accountDto, dto);
		});
	}

	@Override
	public void doProvisioning(AccAccountDto account, DTO dto) {
		Assert.notNull(account, "Account cannot be null!");
		Assert.notNull(dto, "Dto cannot be null");
		//
		LOG.debug("Start provisioning for account [{}]", account.getUid());
		entityEventManager.process(new ProvisioningEvent(ProvisioningEvent.ProvisioningEventType.START, account,
				ImmutableMap.of(ProvisioningService.DTO_PROPERTY_NAME, dto)));
	}

	@Override
	public EventContext<AccAccountDto> doProvisioning(AccAccountDto account, DTO dto, Map<String,Serializable> properties) {
		Assert.notNull(account, "Account cannot be null!");
		Assert.notNull(dto, "Dto cannot be null");
		//
		LOG.debug("Start provisioning for account [{}]", account.getUid());
		Map<String,Serializable> fullProperties = ImmutableMap.<String,Serializable>builder()
				.putAll(properties)
				.put(ProvisioningService.DTO_PROPERTY_NAME, dto)
				.build();
		return entityEventManager.process(new ProvisioningEvent(ProvisioningEvent.ProvisioningEventType.START, account,
				fullProperties));
	}

	@Override
	public void doInternalProvisioning(AccAccountDto account, DTO dto) {
		doInternalProvisioning(account,dto,false);
	}

	@Override
	public SysProvisioningOperationDto doInternalProvisioning(AccAccountDto account, DTO dto, boolean isDryRun) {
		Assert.notNull(account, "Account is required.");
		Assert.notNull(dto, "DTO is required.");
		//
		ProvisioningOperationType operationType;
		SysSystemDto system = DtoUtils.getEmbedded(account, AccAccount_.system);
		SysSystemEntityDto systemEntity = getSystemEntity(account);
		String entityType = systemEntityManager.getSystemEntityByClass(dto.getClass()).getSystemEntityCode();
		String uid = account.getUid();
		//
		if (systemEntity == null) {
			// prepare system entity - uid could be changed by provisioning, but
			// we need to link her with account
			// First we try find system entity with same uid.
			systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, entityType, uid);
			if (systemEntity == null) {
				systemEntity = new SysSystemEntityDto();
				systemEntity.setEntityType(entityType);
				systemEntity.setSystem(system.getId());
				systemEntity.setUid(uid);
				systemEntity.setWish(true);
				systemEntity = systemEntityService.save(systemEntity);
			}
			account.setSystemEntity(systemEntity.getId());
			account = accountService.save(account);
			// we wont create account, but after target system call can be
			// switched to UPDATE
			operationType = ProvisioningOperationType.CREATE;
		} else {
			// we wont update account, but after target system call can be
			// switched to CREATE
			operationType = ProvisioningOperationType.UPDATE;
		}

		List<AttributeMapping> finalAttributes = resolveMappedAttributes(account, dto, system,
				systemEntity.getEntityType());
		if (CollectionUtils.isEmpty(finalAttributes)) {
			// nothing to do - mapping is empty
			return null;
		}
		SysSystemMappingDto systemMappingDto = DtoUtils.getEmbedded(account, AccAccount_.systemMapping, SysSystemMappingDto.class);
		return doProvisioning(systemEntity, dto, dto.getId(), operationType, finalAttributes, isDryRun, systemMappingDto, account);
	}

	@Override
	public void doDeleteProvisioning(AccAccountDto account, UUID entityId) {
		Assert.notNull(account, "Account is required.");
		SysSystemEntityDto systemEntity = getSystemEntity(account);
		SysSystemMappingDto systemMappingDto = null;
		if (account.getSystemMapping() != null) {
			systemMappingDto = DtoUtils.getEmbedded(account, AccAccount_.systemMapping, SysSystemMappingDto.class);
		}
		//
		if (systemEntity != null) {
			doProvisioning(systemEntity, null, entityId, ProvisioningOperationType.DELETE, null, systemMappingDto, account);
		}
	}

	@Override
	public List<OperationResult> changePassword(DTO dto, PasswordChangeDto passwordChange) {
		Assert.notNull(dto, "DTO is required.");
		Assert.notNull(dto.getId(), "Password can be changed, when dto is already persisted.");
		Assert.notNull(passwordChange, "Password change dto is required.");
		List<SysProvisioningOperationDto> preparedOperations = new ArrayList<>();
		//
		EntityAccountFilter filter = this.createEntityAccountFilter();
		filter.setEntityId(dto.getId());
		List<? extends EntityAccountDto> entityAccountList = getEntityAccountService().find(filter, null).getContent();

		// Distinct by accounts
		List<UUID> accountIds = new ArrayList<>();
		entityAccountList.stream().filter(entityAccount -> {
			if (!entityAccount.isOwnership()) {
				return false;
			}
			if (passwordChange.isAll()) {
				// Add all account supports change password
				if (entityAccount.getAccount() == null) {
					return false;
				}
				// Check if system for this account support change password
				AccAccountFilter accountFilter = new AccAccountFilter();
				accountFilter.setSupportChangePassword(Boolean.TRUE);
				accountFilter.setId(entityAccount.getAccount());
				List<AccAccountDto> accountsChecked = accountService.find(accountFilter, null).getContent();
				if (accountsChecked.size() == 1) {
					return true;
				}
				return false;
			} else {
				return passwordChange.getAccounts().contains(entityAccount.getAccount().toString());
			}
		}).forEach(entityAccount -> {
			if (!accountIds.contains(entityAccount.getAccount())) {
				accountIds.add(entityAccount.getAccount());
			}
		});
		//
		// Is possible that some account has disabled password attributes
		List<OperationResult> notExecutedPasswordChanged = new ArrayList<>();
		//
		List<AccAccountDto> accounts = new ArrayList<>();
		accountIds.forEach(accountId -> {
			AccAccountDto account = accountService.get(accountId);
			// Skip account in protection
			if (account.isInProtection()) {
				return; // Skip this iteration
			}
			//
			accounts.add(account);
			// find UID from system entity or from account
			SysSystemDto system = DtoUtils.getEmbedded(account, AccAccount_.system);
			if (account.getSystemEntity() == null) {
				throw new SystemEntityNotFoundException(
						AccResultCode.PROVISIONING_PASSWORD_SYSTEM_ENTITY_NOT_FOUND,
						String.valueOf(account.getUid()),
						system.getCode()
				);
			}
			SysSystemEntityDto systemEntity = systemEntityService.get(account.getSystemEntity());
			//
			// Find mapped attributes (include overloaded attributes)
			List<AttributeMapping> finalAttributes = resolveMappedAttributes(account, dto, system,
					systemEntity.getEntityType());
			if (CollectionUtils.isEmpty(finalAttributes)) {
				return;
			}

			// We try find __PASSWORD__ attribute in mapped attributes
			AttributeMapping mappedAttribute = finalAttributes.stream()
					.filter((attribute) -> {
						SysSchemaAttributeDto schemaAttributeDto = getSchemaAttribute(attribute);
						return ProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME.equals(schemaAttributeDto.getName());
					}).findFirst().orElse(null);
			//
			// get all another passwords, list with all passwords (included primary password marked as __PASSWORD__)
			SysSystemMappingDto systemMappingDto = DtoUtils.getEmbedded(account, AccAccount_.systemMapping, SysSystemMappingDto.class);
			List<SysSystemAttributeMappingDto> passwordAttributes = attributeMappingService
					.getAllPasswordAttributes(system.getId(), systemMappingDto.getId());
			//
			// create account object with all another password
			Map<ProvisioningAttributeDto, Object> accountObjectWithAnotherPassword = new HashMap<>(passwordAttributes.size());
			for (AttributeMapping passwordAttribute : passwordAttributes) {
				// all password attributes contains also main __PASSWORD__ the attribute must be skipped
				if (mappedAttribute != null && mappedAttribute.equals(passwordAttribute)) {
					continue;
				}
				GuardedString transformPassword = transformPassword(passwordChange.getNewPassword(), passwordAttribute,
						systemEntity.getUid(), dto);
				SysSchemaAttributeDto schemaAttribute = schemaAttributeService
						.get(passwordAttribute.getSchemaAttribute());
				ProvisioningAttributeDto passwordProvisiongAttributeDto = ProvisioningAttributeDto
						.createProvisioningAttributeKey(passwordAttribute, schemaAttribute.getName(),
								schemaAttribute.getClassType());
				accountObjectWithAnotherPassword.put(passwordProvisiongAttributeDto, transformPassword);
			}
			//
			// for this account doesn't exist mapped attribute as password
			if (accountObjectWithAnotherPassword.isEmpty() && mappedAttribute == null) {
				// Beware we cant use AccAccountDto from acc module, in core is checked by this
				notExecutedPasswordChanged.add(new OperationResult.Builder(OperationState.NOT_EXECUTED)
						.setModel(new DefaultResultModel(CoreResultCode.PASSWORD_CHANGE_ACCOUNT_FAILED,
								ImmutableMap.of(IdmAccountDto.PARAMETER_NAME, createResultAccount(account, system))))
						.build());
				// for this account is this failed password change
				return;
			}
			//
			// add all account attributes => standard provisioning
			SysProvisioningOperationDto additionalProvisioningOperation = null;
			// resolve another attributes that must be sent together with password
			List<AttributeMapping> additionalPasswordChangeAttributes = resolveAdditionalPasswordChangeAttributes(
					account, dto, system, systemEntity.getEntityType());
			if (!additionalPasswordChangeAttributes.isEmpty()) {
				additionalProvisioningOperation = prepareProvisioning(systemEntity, dto, dto.getId(),
						ProvisioningOperationType.UPDATE, additionalPasswordChangeAttributes, systemMappingDto, account);
			}
			//
			// add another password
			if (!accountObjectWithAnotherPassword.isEmpty()) {
				if (additionalProvisioningOperation == null) {
					// if additional operation is null create one
					additionalProvisioningOperation = prepareProvisioningOperationForAdditionalPassword(systemEntity,
							dto.getId(), ProvisioningOperationType.UPDATE, systemMappingDto,
							accountObjectWithAnotherPassword);
				} else {
					// if additional operation exists just add all account object with additional passwords
					additionalProvisioningOperation.getProvisioningContext().getAccountObject()
							.putAll(accountObjectWithAnotherPassword);
				}
			}
			//
			// password change operation
			SysProvisioningOperationDto operation;
			if (provisioningExecutor.getConfiguration().isSendPasswordAttributesTogether()
					&& additionalProvisioningOperation != null) {
				// all attributes including another password attributes will be sent with password one provisioning operation
				operation = additionalProvisioningOperation;
				//
				if (mappedAttribute != null) {
					// Main password attribute isn't mapped
					// transform password value trough transformation
					GuardedString transformPassword = transformPassword(passwordChange.getNewPassword(), mappedAttribute,
							systemEntity.getUid(), dto);
					//
					// add wish for password
					SysSchemaAttributeDto schemaAttributeDto = schemaAttributeService.get(mappedAttribute.getSchemaAttribute());
					ProvisioningAttributeDto passwordAttribute = ProvisioningAttributeDto.createProvisioningAttributeKey(
							mappedAttribute, schemaAttributeDto.getName(), schemaAttributeDto.getClassType()
							// TODO: use previously loaded attribute
							);
					//
					// newly isn't needed check if password is constant or etc.
					//
					operation.getProvisioningContext().getAccountObject().put(passwordAttribute, transformPassword);
				}
				//
				// do provisioning for additional attributes and password
				// together
				preparedOperations.add(operation);
			} else {
				// Change password on target system - only
				// TODO: refactor password change - use account wish instead
				// filling connector object attributes directly
				//
				if (mappedAttribute != null) {
					// Main password attribute isn't mapped
					// transform password value trough transformation
					GuardedString transformPassword = transformPassword(passwordChange.getNewPassword(), mappedAttribute,
							systemEntity.getUid(), dto);
					//
					operation = prepareProvisioningForAttribute(systemEntity, mappedAttribute,
							transformPassword, dto);
					preparedOperations.add(operation);
				}
				//
				// do provisioning for additional attributes and passwords in second
				if (additionalProvisioningOperation != null) {
					preparedOperations.add(additionalProvisioningOperation);
				}
			}
		});
		//
		// execute prepared operations
		List<OperationResult> results = preparedOperations.stream().map(operation -> {
			SysProvisioningOperationDto result = provisioningExecutor.executeSync(operation);
			Map<String, Object> parameters = new LinkedHashMap<>();
			AccAccountDto account = accounts
					.stream()
					.filter(a -> a.getRealUid().equals(result.getSystemEntityUid())
							&& a.getSystem().equals(operation.getSystem()))
					.findFirst()
					.orElseThrow(() -> new CoreException(String.format("No account found for uid %s on system %s", result.getSystemEntityUid(), operation.getSystem())));

			SysSystemDto system = DtoUtils.getEmbedded(account, AccAccount_.system);
			//
			parameters.put(IdmAccountDto.PARAMETER_NAME, createResultAccount(account, system));
			//
			if (result.getResult().getState() == OperationState.EXECUTED) {
				// Add success changed password account
				return new OperationResult.Builder(OperationState.EXECUTED)
						.setModel(new DefaultResultModel(CoreResultCode.PASSWORD_CHANGE_ACCOUNT_SUCCESS, parameters))
						.build();
			}
			OperationResult changeResult = new OperationResult.Builder(result.getResult().getState())
					.setModel(new DefaultResultModel(CoreResultCode.PASSWORD_CHANGE_ACCOUNT_FAILED, parameters))
					.build();
			changeResult.setCause(result.getResult().getCause());
			changeResult.setCode(result.getResult().getCode());
			return changeResult;
		}).collect(Collectors.toList());
		//
		// add not executed changed from prepare stage
		results.addAll(notExecutedPasswordChanged);
		return results;
	}

	@Override
	public boolean accountManagement(DTO dto) {
		String entityType = systemEntityManager.getSystemEntityByClass(dto.getClass()).getSystemEntityCode();
		List<SysSystemMappingDto> systemMappings = findSystemMappingsForEntityType(dto, entityType);
		systemMappings.forEach(mapping -> {
			SysSchemaObjectClassDto schemaObjectClassDto = schemaObjectClassService.get(mapping.getObjectClass());
			UUID systemId = schemaObjectClassDto.getSystem();
			UUID accountId = this.getAccountByEntity(dto.getId(), systemId);
			if (accountId != null) {
				// We already have account for this system -> next
				return;
			}
			SysSystemDto system = DtoUtils.getEmbedded(schemaObjectClassDto, SysSchemaObjectClass_.system);

			List<SysSystemAttributeMappingDto> mappedAttributes = attributeMappingService.findBySystemMapping(mapping);
			SysSystemAttributeMappingDto uidAttribute = attributeMappingService.getUidAttribute(mappedAttributes,
					system);
			String uid = attributeMappingService.generateUid(dto, uidAttribute);

			// Account management - can be the account created? - execute the script on the system mapping
			if (!this.canBeAccountCreated(uid, dto, mapping, system)) {
				String entityStr = dto.toString();
				if(dto instanceof Codeable){
					entityStr = ((Codeable)dto).getCode();
				}
				LOG.info(MessageFormat.format(
						"For entity [{0}] and entity type [{1}] cannot be created the account (on system [{2}]),"
								+ " because script \"Can be account created\" on the mapping returned \"false\"!",
								entityStr, entityType, system.getName()));
				return;
			}


			// Create AccAccount and relation between account and entity
			createEntityAccount(uid, dto.getId(), systemId);
		});

		return true;
	}

	private boolean canBeAccountCreated(String uid, DTO dto, SysSystemMappingDto mapping, SysSystemDto system) {
		return systemMappingService.canBeAccountCreated(uid, dto, mapping.getCanBeAccountCreatedScript(), system);
	}

	/**
	 * Returns system entity associated to given account
	 *
	 * @param account
	 * @return
	 */
	private SysSystemEntityDto getSystemEntity(AccAccountDto account) {
		if (account.getSystemEntity() == null) {
			return null;
		}
		//
		// TODO: we can find system entity on target system, if no one exists
		// etc.
		//
		SysSystemEntityDto systemEntityDto = DtoUtils.getEmbedded(account, AccAccount_.systemEntity.getName(),
				SysSystemEntityDto.class, null);
		if (systemEntityDto == null) {
			return systemEntityService.get(account.getSystemEntity());
		}
		return systemEntityDto;
	}

	/**
	 * Validate attributes on incompatible strategies
	 *
	 * @param finalAttributes
	 * @param overloadingAttributes
	 */
	protected void validateAttributesStrategy(List<AttributeMapping> finalAttributes, List<SysRoleSystemAttributeDto> overloadingAttributes) {
		if (finalAttributes == null) {
			return;
		}
		finalAttributes.forEach(parentAttribute -> overloadingAttributes.forEach(sysRoleSystemAttributeDto -> {
			AttributeMapping overloadedMapping = DtoUtils.getEmbedded(sysRoleSystemAttributeDto, SysRoleSystemAttribute_.systemAttributeMapping, AttributeMapping.class);
			UUID attributeSchemaId = getSchemaAttributeId(overloadedMapping);
			UUID parentSchemaId = getSchemaAttributeId(parentAttribute);

			if (attributeSchemaId.equals(parentSchemaId)
					&& !(overloadedMapping.getStrategyType() == parentAttribute.getStrategyType()
					|| overloadedMapping.getStrategyType() == AttributeMappingStrategyType.CREATE
					|| overloadedMapping.getStrategyType() == AttributeMappingStrategyType.WRITE_IF_NULL)) {
				IdmRoleDto roleParent = this.getRole(parentAttribute);
				IdmRoleDto roleConflict = this.getRole(overloadedMapping);
				throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_STRATEGY_CONFLICT,
						ImmutableMap.of("strategyParent", parentAttribute.getStrategyType(), //
								"strategyConflict", overloadedMapping.getStrategyType(), //
								"attribute", parentAttribute.getName(), //
								"roleParent", roleParent != null ? roleParent.getCode() : "-", //
								"roleConflict", roleConflict != null ? roleConflict.getCode() : "-")); //
			}
		}));
	}

	/**
	 * Get {@link IdmRoleDto} for attribute mapping. Attribute mapping must be instance of {@link SysRoleSystemAttributeDto}.
	 * And attribute must have role system connection.
	 * If exists data in the embedded map, then is use.
	 *
	 * @param attribute
	 * @return
	 */
	private IdmRoleDto getRole(AttributeMapping attribute) {
		if (attribute instanceof SysRoleSystemAttributeDto) {
			SysRoleSystemAttributeDto roleSystemAttributeDto = (SysRoleSystemAttributeDto) attribute;
			if (roleSystemAttributeDto.getRoleSystem() == null) {
				return null;
			}
			SysRoleSystemDto roleSystem = DtoUtils.getEmbedded(roleSystemAttributeDto,
					SysRoleSystemAttribute_.roleSystem.getName(), SysRoleSystemDto.class, null);
			if (roleSystem == null) {
				roleSystem = roleSystemService.get(roleSystemAttributeDto.getRoleSystem());
			}
			if (roleSystem == null) {
				return null;
			}
			IdmRoleDto role = DtoUtils.getEmbedded(roleSystem, SysRoleSystem_.role.getName(),
					IdmRoleDto.class, null);
			if (role == null) {
				return roleService.get(roleSystem.getRole());
			}
			return role;
		}
		return null;
	}

	private SysProvisioningOperationDto prepareProvisioning(SysSystemEntityDto systemEntity, DTO dto, UUID entityId,
															ProvisioningOperationType operationType, List<? extends AttributeMapping> attributes,
															SysSystemMappingDto systemMappingDto, AccAccountDto accountDto) {
		Assert.notNull(systemEntity, "System entity is required.");
		Assert.notNull(systemEntity.getUid(), "System entity uid is required.");
		Assert.notNull(systemEntity.getEntityType(), "System entity type is required.");
		SysSystemDto system = DtoUtils.getEmbedded(systemEntity, SysSystemEntity_.system);
		//
		// If are input attributes null, then we load default mapped attributes
		if (attributes == null) {
			attributes = findAttributeMappings(systemMappingDto);
		}
		if (attributes == null || attributes.isEmpty()) {
			return null;
		}

		// One IDM object can be mapped to one connector object (= one connector
		// class).
		if (systemMappingDto == null) {
			// mapping not found - nothing to do
			// TODO: delete operation?
			return null;
		}
		// Create mapping context from the script defined on the mapping and by checked options.
		// This context will be propagate to all attributes (transformation to the system).
		MappingContext mappingContext = systemMappingService.getMappingContext(systemMappingDto, systemEntity, dto, system);

		Map<ProvisioningAttributeDto, Object> accountAttributes = prepareMappedAttributesValues(dto, operationType,
				systemEntity, attributes, mappingContext);

		UUID roleRequestId = null;
		if(ProvisioningOperationType.DELETE == operationType) {
			// Return ID of role-request from system-entity's context.
			roleRequestId = getRoleRequestIdFromContext(systemEntity);
		} else {
			// Return ID of role-request from DTO's context.
			roleRequestId = getRoleRequestIdFromContext(dto);
		}

		// public provisioning event
		SysSchemaObjectClassDto schemaObjectClassDto = schemaObjectClassService.get(systemMappingDto.getObjectClass());
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(systemEntity.getUid(),
				new IcObjectClassImpl(schemaObjectClassDto.getObjectClassName()), null);
		// Propagate the role-request ID to the connector (for virtual systems ...)
		connectorObject.getObjectClass().setRoleRequestId(roleRequestId);

		SysProvisioningOperationDto.Builder operationBuilder = new SysProvisioningOperationDto.Builder()
				.setOperationType(operationType) //
				.setSystemEntity(systemEntity) //
				.setEntityIdentifier(entityId) //
				.setRoleRequestId(roleRequestId)
				.setProvisioningContext(new ProvisioningContext(accountAttributes, connectorObject))
				.setAccount(accountDto.getId());
		//
		return operationBuilder.build();
	}

	/**
	 * Return ID of role-request from DTO's context. If context or value missing,
	 * then return null.
	 *
	 * @param dto
	 * @return
	 */
	private UUID getRoleRequestIdFromContext(AbstractDto dto) {
		if (dto instanceof Contextable) {
			Contextable contextable = (Contextable) dto;
			Map<String, Object> context = contextable.getContext();
			if (context != null && context.containsKey(IdmRoleRequestService.ROLE_REQUEST_ID_KEY)) {
				Object value = context.get(IdmRoleRequestService.ROLE_REQUEST_ID_KEY);
				if (value instanceof UUID) {
					return (UUID) value;
				}
			}
		}
		return null;
	}

	/**
	 * Do provisioning on given system for given entity
	 *
	 * @param systemEntity
	 * @param dto
	 * @param operationType
	 * @param attributes
	 */
	private void doProvisioning(SysSystemEntityDto systemEntity, DTO dto, UUID entityId,
								ProvisioningOperationType operationType, List<? extends AttributeMapping> attributes, SysSystemMappingDto systemMappingDto,
								AccAccountDto accountDto) {
		SysProvisioningOperationDto provisioningOperation = prepareProvisioning(systemEntity, dto, entityId,
				operationType, attributes, systemMappingDto, accountDto);
		//
		if (provisioningOperation != null) {
			provisioningExecutor.execute(provisioningOperation);
		}
	}

	private SysProvisioningOperationDto doProvisioning(SysSystemEntityDto systemEntity, DTO dto, UUID entityId,
													   ProvisioningOperationType operationType, List<? extends AttributeMapping> attributes, boolean isDryRun,
													   SysSystemMappingDto systemMappingDto, AccAccountDto accountDto) {
		SysProvisioningOperationDto provisioningOperation = prepareProvisioning(systemEntity, dto, entityId,
				operationType, attributes, systemMappingDto, accountDto);
		//
		if (provisioningOperation != null) {
			provisioningOperation.setDryRun(isDryRun);
			return provisioningExecutor.execute(provisioningOperation, isDryRun);
		}
		return null;
	}

	/**
	 * Prepare all mapped attribute values (= account)
	 *
	 * @param dto
	 * @param operationType
	 * @param systemEntity
	 * @param attributes
	 * @return
	 */
	protected Map<ProvisioningAttributeDto, Object> prepareMappedAttributesValues(DTO dto,
			ProvisioningOperationType operationType, SysSystemEntityDto systemEntity,
			List<? extends AttributeMapping> attributes, MappingContext mappingContext) {
		AccAccountDto account = getAccountSystemEntity(systemEntity.getId());
		String uid = systemEntity.getUid();
		SysSystemDto system = DtoUtils.getEmbedded(systemEntity, SysSystemEntity_.system);
		Map<ProvisioningAttributeDto, Object> accountAttributes = new HashMap<>();

		// delete - account attributes is not needed
		if (ProvisioningOperationType.DELETE == operationType) {
			return accountAttributes;
		}

		// First we will resolve attribute without MERGE strategy
		attributes.stream().filter(attribute -> !attribute.isDisabledAttribute() && !attribute.isPasswordAttribute()
						&& AttributeMappingStrategyType.AUTHORITATIVE_MERGE != attribute.getStrategyType()
						&& AttributeMappingStrategyType.MERGE != attribute.getStrategyType())
				.forEach(attribute -> {
					SysSchemaAttributeDto schemaAttributeDto = getSchemaAttribute(attribute);
					if (attribute.isUid()) {
						// TODO: now we set UID from SystemEntity, may be UID from
						// AccAccount will be more correct
						Object uidValue = getAttributeValue(uid, dto, attribute, system, mappingContext, account);
						if (uidValue == null) {
							throw new ProvisioningException(AccResultCode.PROVISIONING_GENERATED_UID_IS_NULL,
									ImmutableMap.of("system", system.getName()));
						}
						if (!(uidValue instanceof String)) {
							throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_UID_IS_NOT_STRING,
									ImmutableMap.of("uid", uidValue, "system", system.getName()));
						}
						updateAccountUid(account, uid, (String) uidValue);
						accountAttributes.put(ProvisioningAttributeDto.createProvisioningAttributeKey(attribute,
								schemaAttributeDto.getName(), schemaAttributeDto.getClassType()), uidValue);
					} else {
						accountAttributes.put(ProvisioningAttributeDto.createProvisioningAttributeKey(attribute,
								schemaAttributeDto.getName(), schemaAttributeDto.getClassType()), getAttributeValue(uid, dto, attribute, system, mappingContext, account));
					}
				});

		// Second we will resolve MERGE attributes
		List<? extends AttributeMapping> attributesMerge = attributes.stream()
				.filter(attribute -> !attribute.isDisabledAttribute()
						&& (AttributeMappingStrategyType.AUTHORITATIVE_MERGE == attribute.getStrategyType()
						|| AttributeMappingStrategyType.MERGE == attribute.getStrategyType()))
				.collect(Collectors.toList());

		for (AttributeMapping attributeParent : attributesMerge) {
			SysSchemaAttributeDto schemaAttributeParent = getSchemaAttribute(attributeParent);
			ProvisioningAttributeDto attributeParentKey = ProvisioningAttributeDto
					.createProvisioningAttributeKey(attributeParent, schemaAttributeParent.getName(), schemaAttributeParent.getClassType());
			if (!schemaAttributeParent.isMultivalued()) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_MERGE_ATTRIBUTE_IS_NOT_MULTIVALUE,
						ImmutableMap.of("object", uid, "attribute", schemaAttributeParent.getName(), "system",
								system.getName()));
			}

			// We use SET collection because we want collection of merged values without duplicates
			Set<Object> mergedValues = new LinkedHashSet<>();
			attributesMerge.stream() //
					.filter(attribute -> { //
						SysSchemaAttributeDto schemaAttribute = getSchemaAttribute(attribute);
						return !accountAttributes.containsKey(attributeParentKey)
								&& schemaAttributeParent.equals(schemaAttribute)
								&& attributeParent.getStrategyType() == attribute.getStrategyType();
					}).forEach(attribute -> {
						Object value = getAttributeValue(uid, dto, attribute, system, mappingContext, account);
						// We don`t want null item in list (problem with
						// provisioning in IC)
						if (value != null) {
							// If is value collection, then we add all its items to
							// main list!
							if (value instanceof Collection) {
								Collection<?> collectionNotNull = ((Collection<?>) value).stream().filter(Objects::nonNull).collect(Collectors.toList());
								mergedValues.addAll(collectionNotNull);
							} else {
								mergedValues.add(value);
							}
						}
					});
			if (!accountAttributes.containsKey(attributeParentKey)) {
				// we must put merged values as array list
				accountAttributes.put(attributeParentKey, new ArrayList<>(mergedValues));
 			}
    	}
		return accountAttributes;
	}

	protected Object getAttributeValue(String uid, DTO dto, AttributeMapping attribute, SysSystemDto system, MappingContext mappingContext, AccAccountDto accountDto) {
		return attributeMappingService.getAttributeValue(uid, dto, attribute, mappingContext, accountDto);
	}

	@Override
	public void doProvisioningForAttribute(SysSystemEntityDto systemEntity, AttributeMapping attributeMapping,
			Object value, ProvisioningOperationType operationType, DTO dto) {
		provisioningExecutor
				.execute(prepareProvisioningForAttribute(systemEntity, attributeMapping, value, dto));
	}

	private SysProvisioningOperationDto prepareProvisioningForAttribute(SysSystemEntityDto systemEntity,
			AttributeMapping attributeMapping, Object value, DTO dto) {

		Assert.notNull(systemEntity, "System entity is required.");
		Assert.notNull(systemEntity.getSystem(), "Relation to system is required for system entity.");
		Assert.notNull(systemEntity.getEntityType(), "System entity type is required.");
		Assert.notNull(systemEntity.getUid(), "System entity uid is required.");
		Assert.notNull(attributeMapping, "Attribute mapping is required.");

		SysSchemaAttributeDto schemaAttributeDto = getSchemaAttribute(attributeMapping);

		if (!schemaAttributeDto.isUpdateable()) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_SCHEMA_ATTRIBUTE_IS_NOT_UPDATEABLE,
					ImmutableMap.of("attribute", StringUtils.defaultIfBlank(attributeMapping.getIdmPropertyName(),
							attributeMapping.getName()), "entity", systemEntity.getUid()));
		}
		SysSchemaObjectClassDto schemaObjectClassDto = schemaObjectClassService
				.get(schemaAttributeDto.getObjectClass());
		String objectClassName = schemaObjectClassDto.getObjectClassName();
		// We do transformation to system if is attribute only constant
		Object valueTransformed = value;
		// If is attribute handling resolve as constant, then we don't want
		// do transformation again (was did in getAttributeValue)
		if (attributeMapping.isEntityAttribute() || attributeMapping.isExtendedAttribute()) {
			valueTransformed = attributeMappingService.transformValueToResource(systemEntity.getUid(), value,
					attributeMapping, dto);
		}
		IcAttribute icAttributeForCreate = attributeMappingService.createIcAttribute(schemaAttributeDto,
				valueTransformed);
		//
		// Call IC modul for update single attribute
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(systemEntity.getUid(),
				new IcObjectClassImpl(objectClassName), ImmutableList.of(icAttributeForCreate));
		SysProvisioningOperationDto.Builder operationBuilder = new SysProvisioningOperationDto.Builder()
				.setOperationType(ProvisioningEventType.UPDATE).setSystemEntity(systemEntity)
				.setEntityIdentifier(dto == null ? null : dto.getId())
				.setProvisioningContext(new ProvisioningContext(connectorObject));
		//
		return operationBuilder.build();
	}

	@Override
	public IcUidAttribute authenticate(String username, GuardedString password, SysSystemDto system,
			String entityType) {

		Assert.notNull(username, "Username is required.");
		Assert.notNull(system, "System is required.");
		Assert.notNull(entityType, "Entity type is required.");

		// Find connector configuration persisted in system
		IcConnectorConfiguration connectorConfig = systemService.getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Call IC module for check authenticate
		return connectorFacade.authenticateObject(systemService.getConnectorInstance(system), connectorConfig, null, username,
				password);
	}

	/**
	 * Return all mapped attributes for this account (include overridden
	 * attributes)
	 *
	 * @param account
	 * @param system
	 * @param entityType
	 * @return
	 */
	@Override
	public List<AttributeMapping> resolveMappedAttributes(AccAccountDto account, DTO dto, SysSystemDto system,
			String entityType) {

		// All role system attributes (overloading) for this uid and same system
		List<SysRoleSystemAttributeDto> roleSystemAttributesAll = findOverloadingAttributes(dto, system,
				account, entityType);

		// All default mapped attributes from system
		SysSystemMappingDto systemMappingDto = null;
		if (account.getSystemMapping() != null) {
			systemMappingDto = DtoUtils.getEmbedded(account, AccAccount_.systemMapping, SysSystemMappingDto.class);
		}
		List<? extends AttributeMapping> defaultAttributes = findAttributeMappings(systemMappingDto);

		// Final list of attributes use for provisioning
		List<AttributeMapping> attributeMappings = compileAttributes(defaultAttributes, roleSystemAttributesAll, entityType);

		// add attribute which is overloaded via EAV of account
		if (account.getFormDefinition() != null && systemMappingDto != null) {
			SysSystemMappingDto finalSystemMappingDto = systemMappingDto;
			List<IdmFormValueDto> values = formService.getValues(account, account.getFormDefinition());

			values.forEach(idmFormValueDto -> {

				IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(idmFormValueDto, IdmFormValue_.formAttribute, IdmFormAttributeDto.class);

				SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
				schemaAttributeFilter.setName(formAttributeDto.getCode());
				schemaAttributeFilter.setSystemId(account.getSystem());
				schemaAttributeFilter.setObjectClassId(finalSystemMappingDto.getObjectClass());
				List<SysSchemaAttributeDto> schemaAttributeDtos = schemaAttributeService.find(schemaAttributeFilter, null).getContent();

				if (schemaAttributeDtos.size() == 1) {
					SysSystemAttributeMappingDto firstname = new SysSystemAttributeMappingDto();
					firstname.setStrategyType(AttributeMappingStrategyType.SET);
					firstname.setName(formAttributeDto.getCode());
					firstname.setSchemaAttribute(schemaAttributeDtos.get(0).getId());
					firstname.setSystemMapping(finalSystemMappingDto.getId());
					attributeMappings.add(firstname);
				}
			});
		}

		return attributeMappings;
	}

	private List<AttributeMapping> resolveAdditionalPasswordChangeAttributes(AccAccountDto account, DTO dto,
			SysSystemDto system, String entityType) {

		UUID mapping = account.getSystemMapping();
		if (mapping == null) {
			return Collections.<AttributeMapping>emptyList();
		}
		//
		// All additional mapped attributes from system, witch has to be send on
		// password change
		SysSystemAttributeMappingFilter attributeFilter = new SysSystemAttributeMappingFilter();
		attributeFilter.setSystemMappingId(mapping);
		attributeFilter.setSendOnPasswordChange(Boolean.TRUE);
		// we want only active attributes
		attributeFilter.setDisabledAttribute(Boolean.FALSE);
		List<? extends AttributeMapping> additionalPasswordChangeAttributes = attributeMappingService
				.find(attributeFilter, null).getContent();
		//
		// All role system attributes (overloading) for this uid and same system
		List<SysRoleSystemAttributeDto> roleSystemAttributesAll = findOverloadingAttributes(dto, system,
				account, entityType);
		//
		// Final list of attributes use for provisioning
		List<AttributeMapping> results = compileAttributes(additionalPasswordChangeAttributes, roleSystemAttributesAll,
				entityType);
		//
		return results == null ? Collections.<AttributeMapping>emptyList() : results;
	}

	/**
	 * Create final list of attributes for provisioning.
	 *
	 */
	@Override
	public List<AttributeMapping> compileAttributes(List<? extends AttributeMapping> defaultAttributes,
			List<SysRoleSystemAttributeDto> overloadingAttributes, String entityType) {
		Assert.notNull(overloadingAttributes, "List of overloading attributes cannot be null!");

		List<AttributeMapping> finalAttributes = new ArrayList<>();
		if (defaultAttributes == null) {
			return null;
		}

		defaultAttributes.forEach(defaultAttribute -> {
			for (AttributeMappingStrategyType strategy : AttributeMappingStrategyType.values()) {
				finalAttributes.addAll(compileAtributeForStrategy(strategy, defaultAttribute, overloadingAttributes));
			}
		});

		// Validate attributes on incompatible strategies
		validateAttributesStrategy(finalAttributes, overloadingAttributes);

		return finalAttributes;
	}

	/**
	 * Compile given attribute for strategy
	 *
	 * @param strategy
	 * @param defaultAttribute
	 * @param overloadingAttributes
	 *
	 * @return
	 */
	protected List<AttributeMapping> compileAtributeForStrategy(AttributeMappingStrategyType strategy,
			AttributeMapping defaultAttribute, List<SysRoleSystemAttributeDto> overloadingAttributes) {

		List<AttributeMapping> finalAttributes = new ArrayList<>();

		List<SysRoleSystemAttributeDto> attributesOrdered = overloadingAttributes.stream()
				.filter(roleSystemAttribute -> {
					// Search attribute override same schema attribute
					SysSystemAttributeMappingDto attributeMapping = DtoUtils.getEmbedded(roleSystemAttribute,
							SysRoleSystemAttribute_.systemAttributeMapping.getName(),
							SysSystemAttributeMappingDto.class);
					return attributeMapping.equals(defaultAttribute);
				}).sorted((att1, att2) -> {
					// Sort attributes by role priority
					IdmRoleDto role1 = this.getRole(att1);
					IdmRoleDto role2 = this.getRole(att2);
					return Integer.compare(role2.getPriority(), role1.getPriority());
				}).collect(Collectors.toList());

		// We have some overloaded attributes
		if (!attributesOrdered.isEmpty()) {
			List<SysRoleSystemAttributeDto> attributesOrderedGivenStrategy = attributesOrdered.stream()
					.filter(attribute -> strategy == attribute.getStrategyType()).collect(Collectors.toList());

			// We do not have overloaded attributes for given strategy
			if (attributesOrderedGivenStrategy.isEmpty()) {
				return finalAttributes;
			}

			// First element have role with max priority
			IdmRoleDto roleForSetMaxPriority = this.getRole((AttributeMapping) attributesOrderedGivenStrategy.get(0));
			int maxPriority = roleForSetMaxPriority.getPriority();

			// We will search for attribute with highest priority (and role
			// name)
			// Filter attributes by max priority
			// Second filtering, if we have same priority, then
			// we will sort by role name
			//
			Optional<SysRoleSystemAttributeDto> highestPriorityAttributeOptional = attributesOrderedGivenStrategy
					.stream().filter(attribute -> {
						IdmRoleDto roleDto = this.getRole(attribute);
						// Filter attributes by max priority
						return maxPriority == roleDto.getPriority();
					}).min((att1, att2) -> {
						// Second filtering, if we have same priority, then
						// we will sort by role name
						IdmRoleDto roleDto1 = this.getRole(att1);
						IdmRoleDto roleDto2 = this.getRole(att2);
						//
						return roleDto2.getCode().compareTo(roleDto1.getCode());
					});

			if (highestPriorityAttributeOptional.isPresent()) {
				SysRoleSystemAttributeDto highestPriorityAttribute = highestPriorityAttributeOptional.get();

				// For merge strategies, will be add to final list all
				// overloaded attributes
				if (strategy == AttributeMappingStrategyType.AUTHORITATIVE_MERGE
						|| strategy == AttributeMappingStrategyType.MERGE) {
					attributesOrderedGivenStrategy.forEach(attribute -> {
						// Disabled attribute will be skipped
						if (!attribute.isDisabledDefaultAttribute()) {
							// Default values (values from schema attribute
							// handling)
							attribute.setSchemaAttribute(defaultAttribute.getSchemaAttribute());
							attribute.setTransformFromResourceScript(defaultAttribute.getTransformFromResourceScript());

							// Common properties (for MERGE strategy) will be
							// set from MERGE attribute with highest priority
							attribute.setSendAlways(highestPriorityAttribute.isSendAlways());
							attribute.setSendOnlyIfNotNull(highestPriorityAttribute.isSendOnlyIfNotNull());

							// Add modified attribute to final list
							finalAttributes.add(attribute);
						}
					});
					return finalAttributes;
				}

				// We will search for disabled overloaded attribute
				Optional<SysRoleSystemAttributeDto> disabledOverloadedAttOptional = attributesOrderedGivenStrategy
						.stream().filter(attribute -> {
							// Filter attributes by max priority
							IdmRoleDto roleDto = this.getRole(attribute);
							return maxPriority == roleDto.getPriority();
						}).filter(attribute -> {
							// Second filtering, we will search for disabled
							// overloaded attribute
							return attribute.isDisabledDefaultAttribute();
						}).findFirst();
				if (disabledOverloadedAttOptional.isPresent()) {
					// We found disabled overloaded attribute with highest
					// priority
					return finalAttributes;
				}

				// None overloaded attribute are disabled, we will search for
				// attribute with highest priority (and role name)
				// Disabled attribute will be skipped
				if (!highestPriorityAttribute.isDisabledDefaultAttribute()) {
					// Default values (values from schema attribute handling)
					highestPriorityAttribute.setSchemaAttribute(defaultAttribute.getSchemaAttribute());
					highestPriorityAttribute.setCached(defaultAttribute.isCached());
					highestPriorityAttribute
							.setTransformFromResourceScript(defaultAttribute.getTransformFromResourceScript());
					// Add modified attribute to final list
					finalAttributes.add(highestPriorityAttribute);
					return finalAttributes;
				}
			}
		}
		// We don't have overloading attribute, we will use default
		// if has given strategy
		// If is default attribute disabled, then we don't use him

		if (!defaultAttribute.isDisabledAttribute() && strategy == defaultAttribute.getStrategyType()) {
			finalAttributes.add(defaultAttribute);
		}

		return finalAttributes;
	}

	/**
	 * Return list of all overloading attributes for given identity, system and
	 * uid
	 *
	 */
	protected abstract List<SysRoleSystemAttributeDto> findOverloadingAttributes(DTO dto, SysSystemDto system,
			AccAccountDto account, String entityType);

	/**
	 * Find list of {@link SysSystemAttributeMapping} by provisioning type and
	 * entity type on given system
	 *
	 * @param systemMappingDto
	 * @return
	 */
	protected List<? extends AttributeMapping> findAttributeMappings(SysSystemMappingDto systemMappingDto) {
		if (systemMappingDto == null) {
			return null;
		}
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemMappingId(systemMappingDto.getId());
		// We don't want attributes for password change only.
		filter.setSendOnlyOnPasswordChange(Boolean.FALSE);

		return attributeMappingService.find(filter, null).getContent();
	}

	protected List<SysSystemMappingDto> findSystemMappingsForEntityType(DTO dto, String entityType) {
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setEntityType(entityType);
		mappingFilter.setOperationType(SystemOperationType.PROVISIONING);
		return systemMappingService.find(mappingFilter, null).getContent();
	}

	/**
	 * Create AccAccount and relation between account and entity
	 *
	 * @param uid
	 * @param entityId
	 * @param systemId
	 * @return Id of new EntityAccount
	 */
	protected UUID createEntityAccount(String uid, UUID entityId, UUID systemId) {
		AccAccountDto account = new AccAccountDto();
		account.setSystem(systemId);
		account.setUid(uid);
		account.setEntityType(getEntityType());
		account = accountService.save(account);
		// Create new entity account relation
		EntityAccountDto entityAccount = this.createEntityAccountDto();
		entityAccount.setAccount(account.getId());
		entityAccount.setEntity(entityId);
		entityAccount.setOwnership(true);
		entityAccount = getEntityAccountService().save(entityAccount);
		return (UUID) entityAccount.getId();
	}

	protected UUID getAccountByEntity(UUID entityId, UUID systemId) {
		EntityAccountFilter entityAccountFilter = createEntityAccountFilter();
		entityAccountFilter.setEntityId(entityId);
		entityAccountFilter.setSystemId(systemId);
		entityAccountFilter.setOwnership(Boolean.TRUE);
		List<? extends EntityAccountDto> entityAccounts = this.getEntityAccountService().find(entityAccountFilter, null)
				.getContent();
		if (entityAccounts.isEmpty()) {
			return null;
		} else {
			// We assume that all entity accounts
			// (mark as
			// ownership) have same account!
			return entityAccounts.get(0).getAccount();
		}
	}

	protected UUID getEntityByAccount(UUID accountId, UUID systemId) {
		EntityAccountFilter entityAccountFilter = createEntityAccountFilter();
		entityAccountFilter.setAccountId(accountId);
		entityAccountFilter.setSystemId(systemId);
		entityAccountFilter.setOwnership(Boolean.TRUE);
		List<? extends EntityAccountDto> entityAccounts = this.getEntityAccountService().find(entityAccountFilter, null)
				.getContent();
		if (entityAccounts.isEmpty()) {
			return null;
		} else {
			// We assume that all entity accounts
			// (mark as
			// ownership) have same entity!
			return entityAccounts.get(0).getEntity();
		}
	}

	protected AccAccountDto getAccountSystemEntity(UUID systemEntity) {
		AccAccountFilter filter = new AccAccountFilter();
		filter.setSystemEntityId(systemEntity);
		List<AccAccountDto> accounts = this.accountService.find(filter, null).getContent();
		if (accounts.isEmpty()) {
			return null;
		} else {
			// We assume that system entity has only one account!
			return accounts.get(0);
		}
	}

	protected abstract <F extends EntityAccountFilter> F createEntityAccountFilter();

	protected abstract EntityAccountDto createEntityAccountDto();

	/**
	 * Returns service, which controls DTO's accounts
	 *
	 * @return
	 */
	protected abstract <A extends EntityAccountDto, F extends EntityAccountFilter> ReadWriteDtoService<A, F> getEntityAccountService();

	/**
	 * Returns service, which controls DTO
	 *
	 * @return
	 */
	protected abstract ReadWriteDtoService<DTO, ?> getService();

	/**
	 * Method get {@link SysSystemDto} from uuid schemaAttribute.
	 *
	 * @param schemaAttributeId
	 * @return
	 */
	protected SysSystemDto getSytemFromSchemaAttribute(UUID schemaAttributeId) {
		Assert.notNull(schemaAttributeId, "Schema attribute identifier is required.");
		return getSytemFromSchemaAttribute(schemaAttributeService.get(schemaAttributeId));
	}

	/**
	 * Method get {@link SysSystemDto} from {@link SysSchemaAttributeDto}.
	 *
	 * @param attribute
	 * @return
	 */
	protected SysSystemDto getSytemFromSchemaAttribute(SysSchemaAttributeDto attribute) {
		Assert.notNull(attribute, "Attribute is required.");
		return getSystemFromSchemaObjectClass(schemaObjectClassService.get(attribute.getObjectClass()));
	}

	/**
	 * Method get {@link SysSystemDto} from {@link SysSchemaObjectClassDto}.
	 *
	 * @param schemaObjectClass
	 * @return
	 */
	protected SysSystemDto getSystemFromSchemaObjectClass(SysSchemaObjectClassDto schemaObjectClass) {
		Assert.notNull(schemaObjectClass, "Schema object class is required.");
		return DtoUtils.getEmbedded(schemaObjectClass, SysSchemaObjectClass_.system);
	}

	/**
	 * Update account UID in IDM
	 *
	 */
	private AccAccountDto updateAccountUid(AccAccountDto account, String uid, String uidValue) {
		// If is value form UID attribute null, then we will use UID
		// from existed account (AccAccount/SystemEntity)
		uidValue = uidValue == null ? uid : uidValue;
		if (account != null && !account.getUid().equals(uidValue)) {
			// UID value must be string
			account.setUid(uidValue);

			AccAccountFilter accountFilter = new AccAccountFilter();
			accountFilter.setUid(uidValue);
			List<UUID> content = accountService.findIds(accountFilter, null).getContent();
			if (content.size() > 0 && !content.get(0).equals(account.getId())) {
				throw new ResultCodeException(AccResultCode.PROVISIONING_ACCOUNT_UID_ALREADY_EXISTS,
						ImmutableMap.of("uid", uidValue, "account", account.getId(), "system", account.getSystem(), "mapping", account.getSystemMapping()));
			}
			account = accountService.save(account);
		}
		return account;
	}

	/**
	 * Method return schema attribute from interface attribute mapping. Schema
	 * may be null from RoleSystemAttribute.
	 *
	 */
	protected SysSchemaAttributeDto getSchemaAttribute(AttributeMapping attributeMapping) {
		if (attributeMapping.getSchemaAttribute() != null) {
			SysSchemaAttributeDto schemaAttributeDto = DtoUtils.getEmbedded((AbstractDto) attributeMapping,
					SysSystemAttributeMapping_.schemaAttribute.getName(), SysSchemaAttributeDto.class, null);
			if(schemaAttributeDto != null) {
				return schemaAttributeDto;
			}
			if (attributeMapping instanceof SysRoleSystemAttributeDto) {
				SysSystemAttributeMappingDto systemAttributeMappingDto = DtoUtils.getEmbedded(
						(SysRoleSystemAttributeDto) attributeMapping,
						SysRoleSystemAttribute_.systemAttributeMapping,
						SysSystemAttributeMappingDto.class,
						null
				);
				if (systemAttributeMappingDto != null) {
					schemaAttributeDto = DtoUtils.getEmbedded(
							systemAttributeMappingDto,
							SysSystemAttributeMapping_.schemaAttribute,
							SysSchemaAttributeDto.class,
							null
					);
					if (schemaAttributeDto != null) {
						return schemaAttributeDto;
					}
				}
			}

			return schemaAttributeService.get(attributeMapping.getSchemaAttribute());
		} else {
			// schema attribute is null = roleSystemAttribute
			SysSystemAttributeMappingDto dto = systemAttributeMappingService
					.get(((SysRoleSystemAttributeDto) attributeMapping).getSystemAttributeMapping());
			return schemaAttributeService.get(dto.getSchemaAttribute());
		}
	}

	/**
	 * Method returns schema attribute ID from interface attribute mapping. Schema
	 * can be null for RoleSystemAttribute.
	 *
	 */
	protected UUID getSchemaAttributeId(AttributeMapping attributeMapping) {
		if (attributeMapping.getSchemaAttribute() != null) {
			return attributeMapping.getSchemaAttribute();
		} else {
			// schema attribute is null = roleSystemAttribute
			if (attributeMapping instanceof SysRoleSystemAttributeDto) {
				return systemAttributeMappingService
						.get(((SysRoleSystemAttributeDto) attributeMapping).getSystemAttributeMapping())
						.getSchemaAttribute();
			}
			return null;
		}
	}


	/**
	 * Transform password via transformation stored in {@link AttributeMapping}.
	 * Script for transformation must return null or {@link GuardedString}.
	 *
	 */
	private GuardedString transformPassword(GuardedString newPassword, AttributeMapping mappedAttribute, String uid, DTO dto) {
		// transformed password must be type from schema (eq. GuardedString, String, ...)
		Object transformValue = attributeMappingService.transformValueToResource(uid, newPassword,
				mappedAttribute, dto);
		if (transformValue == null) {
			return null;
		} else if (transformValue instanceof GuardedString) {
			return (GuardedString) transformValue;
		}
		throw new ResultCodeException(AccResultCode.PROVISIONING_PASSWORD_TRANSFORMATION_FAILED,
				ImmutableMap.of("uid", uid, "mappedAttribute", mappedAttribute.getName()));
	}

	/**
	 * Prepare provisioning operation for additional password given in parameter.
	 * Account object with password is not again transformed trough scripts.
	 *
	 */
	private SysProvisioningOperationDto prepareProvisioningOperationForAdditionalPassword(
			SysSystemEntityDto systemEntity, UUID entityId, ProvisioningOperationType operationType,
			SysSystemMappingDto systemMappingDto,
			Map<ProvisioningAttributeDto, Object> accountObjectWithAnotherPassword) {
		SysSchemaObjectClassDto schemaObjectClassDto = schemaObjectClassService.get(systemMappingDto.getObjectClass());
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(systemEntity.getUid(),
				new IcObjectClassImpl(schemaObjectClassDto.getObjectClassName()), null);
		SysProvisioningOperationDto.Builder operationBuilder = new SysProvisioningOperationDto.Builder()
				.setOperationType(operationType).setSystemEntity(systemEntity).setEntityIdentifier(entityId)
				.setProvisioningContext(new ProvisioningContext(accountObjectWithAnotherPassword, connectorObject));
		//
		return operationBuilder.build();
	}

	/**
	 * Method create result idm account. This class is used by password change, beacuse account is send back to core module.
	 *
	 */
	private IdmAccountDto createResultAccount(AccAccountDto account, SysSystemDto system) {
		IdmAccountDto resultAccountDto = new IdmAccountDto();
		resultAccountDto.setId(account.getId());
		resultAccountDto.setUid(account.getUid());
		resultAccountDto.setRealUid(account.getRealUid());
		resultAccountDto.setSystemId(system.getId());
		resultAccountDto.setSystemName(system.getName());
		return resultAccountDto;
	}

	/**
	 * Get mapping from role for identity if the role has some. In other cases, get mapping from system
	 * @param entityAccountDto entity account for which we want the mapping
	 * @param accountDto account dto for which we want the mapping
	 * @return UUID of mapping
	 */
	private UUID getSystemMappingForAccount(EntityAccountDto entityAccountDto, AccAccountDto accountDto) {
		if (accountDto.getSystemMapping() != null) {
			return accountDto.getSystemMapping();
		}

		if (entityAccountDto instanceof AccIdentityAccountDto) {
			// for user we will use mapping from role, if there is some, otherwise we will user first from system
			AccIdentityAccountDto identityAccountDto = (AccIdentityAccountDto) entityAccountDto;
			if (identityAccountDto.getIdentityRole() != null) {
				IdmIdentityRoleDto identityRoleDto = DtoUtils.getEmbedded(identityAccountDto, AccIdentityAccount_.identityRole, IdmIdentityRoleDto.class);

				SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
				roleSystemFilter.setSystemId(accountDto.getSystem());
				roleSystemFilter.setRoleId(identityRoleDto.getRole());
				List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();

				if (!roleSystemDtos.isEmpty()) {
					SysRoleSystemDto roleSystem = roleSystemDtos.get(0);
					if (roleSystem.getSystemMapping() != null) {
						return roleSystem.getSystemMapping();
					}
				}
			}
		}
		return getMappingFromSystem(accountDto.getSystem(), accountDto.getEntityType());
	}

	/**
	 * Get first provisioning mapping from system
	 * @param system UUID of system
	 * @param entityType type of entity
	 * @return UUID of mapping or null if there is none provisioning mapping
	 */
	private UUID getMappingFromSystem(UUID system, String entityType) {
		// This is fallback, if account is not found, but system has some provisioning mapping, get first one
		List<SysSystemMappingDto> systemMappings = systemMappingService.findBySystemId(system,
				SystemOperationType.PROVISIONING, entityType);
		if (systemMappings == null || systemMappings.isEmpty()) {
			return null;
		}
		return systemMappings.get(0).getId();
	}
}
