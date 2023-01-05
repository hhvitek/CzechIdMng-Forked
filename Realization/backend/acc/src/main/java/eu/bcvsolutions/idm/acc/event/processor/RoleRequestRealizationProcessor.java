package eu.bcvsolutions.idm.acc.event.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.event.IdentityAccountEvent;
import eu.bcvsolutions.idm.acc.event.IdentityAccountEvent.IdentityAccountEventType;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleRequestProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;

/**
 * Realization of request in ACC module - ensure account management and
 * provisioning.
 * 
 * @author Vít Švanda
 *
 */
@Component("accRoleRequestRealizationProcessor")
@Description("Realization of request in ACC module - ensure account management and provisioning.")
public class RoleRequestRealizationProcessor extends CoreEventProcessor<IdmRoleRequestDto>
		implements RoleRequestProcessor {

	public static final String PROCESSOR_NAME = "acc-role-request-realization-processor";
	private static final Logger LOG = LoggerFactory.getLogger(RoleRequestRealizationProcessor.class);

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private AccAccountManagementService accountManagementService;
	@Autowired
	private ProvisioningService provisioningService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private AccIdentityAccountService identityAccountService;

	@Autowired
	public RoleRequestRealizationProcessor(IdmRoleRequestService service) {
		super(RoleRequestEventType.NOTIFY);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public boolean conditional(EntityEvent<IdmRoleRequestDto> event) {
		return super.conditional(event) && IdmIdentityDto.class.getCanonicalName().equals(event.getContent().getApplicantInfo().getApplicantType());
	}

	@Override
	public EventResult<IdmRoleRequestDto> process(EntityEvent<IdmRoleRequestDto> event) {
		IdmRoleRequestDto request = event.getContent();
		IdmIdentityDto identity = identityService.get(request.getApplicant());

		Set<AbstractRoleAssignmentDto> addedIdentityRoles = this
				.getSetProperty(AbstractRoleAssignmentEvent.PROPERTY_ASSIGNED_NEW_ROLES, event, AbstractRoleAssignmentDto.class);
		Set<AbstractRoleAssignmentDto> updatedIdentityRoles = this.getSetProperty(
				AbstractRoleAssignmentEvent.PROPERTY_ASSIGNED_UPDATED_ROLES, event, AbstractRoleAssignmentDto.class);
		Set<UUID> removedIdentityAccounts = this.getSetProperty(IdmAccountDto.IDENTITY_ACCOUNT_FOR_DELAYED_ACM,
				event, UUID.class);
		Set<UUID> accountsForAdditionalProvisioning = this.getSetProperty(IdmAccountDto.ACCOUNT_FOR_ADDITIONAL_PROVISIONING,
				event, UUID.class);
		boolean skipProvisioning = this.getBooleanProperty(ProvisioningService.SKIP_PROVISIONING, event.getProperties());

		Set<UUID> accountsForProvisioning = new HashSet<>(accountsForAdditionalProvisioning);

		if (addedIdentityRoles.size() > 0) {
			LOG.debug("Call account management for identity [{}] and new identity-roles [{}]",
					identity.getUsername(), addedIdentityRoles);
			List<UUID> accounts = accountManagementService.resolveNewIdentityRoles(identity,
					addedIdentityRoles.toArray(new AbstractRoleAssignmentDto[0]));
			addAccounts(accountsForProvisioning, accounts);
		}

		if (updatedIdentityRoles.size() > 0) {
			LOG.debug("Call account management for identity [{}] and updated identity-roles [{}]",
					identity.getUsername(), updatedIdentityRoles);
			List<UUID> accounts = accountManagementService.resolveUpdatedIdentityRoles(identity,
					updatedIdentityRoles.toArray(new AbstractRoleAssignmentDto[0]));
			addAccounts(accountsForProvisioning, accounts);
		}

		// Remove delayed identity-accounts (includes provisioning)
		if (removedIdentityAccounts.size() > 0) {
			LOG.debug("Call account management for identity [{}] - remove identity-accounts [{}]",
					identity.getUsername(), removedIdentityAccounts);
			removedIdentityAccounts.stream().distinct().forEach(identityAccountId -> {
				AccIdentityAccountDto identityAccountDto = identityAccountService.get(identityAccountId);
				if (identityAccountDto != null) {
					IdentityAccountEvent eventIdentityAccount = new IdentityAccountEvent(IdentityAccountEventType.DELETE, identityAccountDto,
							ImmutableMap.of(AccIdentityAccountService.DELETE_TARGET_ACCOUNT_KEY, Boolean.TRUE,
									AccIdentityAccountService.FORCE_DELETE_OF_IDENTITY_ACCOUNT_KEY, Boolean.FALSE,
									IdmRoleRequestService.ROLE_REQUEST_ID_KEY, request.getId())); 
					identityAccountService.publish(eventIdentityAccount);
					accountsForProvisioning.add(identityAccountDto.getAccount());
				}
			});
		}
		
		// Init context in identity DTO and set ID of role-request to it.
		initContext(identity, request);
		
		// Skip provisionig
		if (skipProvisioning) {
			return new DefaultEventResult<>(event, this);
		}
		
		// Provisioning for modified account
		accountsForProvisioning.forEach(accountId -> {
			AccAccountDto account = accountService.get(accountId);
			if (account != null) { // Account could be null (was deleted).
				LOG.debug("Call provisioning for identity [{}] and account [{}]", identity.getUsername(),
						account.getUid());
				provisioningService.doProvisioning(account, identity);
			}
		});
		
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Init context in identity DTO and set ID of role-request to it.
	 * @param identity
	 * @param request
	 */
	private void initContext(IdmIdentityDto identity, IdmRoleRequestDto request) {
		Map<String, Object> context = identity.getContext();
		if (context == null) {
			context = new HashMap<String, Object>();
		}
		context.put(IdmRoleRequestService.ROLE_REQUEST_ID_KEY, request.getId());
		identity.setContext(context);
	}


	private void addAccounts(Set<UUID> accountsForProvisioning, List<UUID> accounts) {
		if (accounts != null) {
			accounts.forEach(accountId -> {
				accountsForProvisioning.add(accountId);
			});
		}
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}
