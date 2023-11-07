package eu.bcvsolutions.idm.acc.event.processor.password;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordValidationDto;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.event.processor.AccountProcessor;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordPolicyService;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordService;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.PasswordManageableProcessor;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.event.EntityPasswordEvent.EntityPasswordEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Processor with password validation.
 * 
 * @author Jirka Koula
 *
 */
@Component("accAccountPasswordValidateProcessor")
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Validates account password, when password is changed.")
public class AccountPasswordValidateProcessor
		extends CoreEventProcessor<AccAccountDto>
		implements AccountProcessor {

	public static final String PROCESSOR_NAME = "account-password-validate-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(AccountPasswordValidateProcessor.class);
	private final AccPasswordPolicyService passwordPolicyService;
	private final AccPasswordService passwordService;
	private final AccAccountRepository accountRepository;

	@Autowired
	public AccountPasswordValidateProcessor(
			AccPasswordPolicyService passwordPolicyService,
			AccPasswordService passwordService,
			AccAccountRepository accountRepository) {
		super(EntityPasswordEventType.PASSWORD);
		//
		Assert.notNull(passwordPolicyService, "Service is required.");
		Assert.notNull(passwordService, "Service is required.");
		Assert.notNull(accountRepository, "Repository is required.");
		//
		this.passwordPolicyService = passwordPolicyService;
		this.passwordService = passwordService;
		this.accountRepository = accountRepository;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccAccountDto> process(EntityEvent<AccAccountDto> event) {
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties()
				.get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		AccAccountDto account = event.getContent();
		//
		Assert.notNull(passwordChangeDto, "Password change dto is required.");
		Assert.notNull(account, "Account is required.");
		//
		LOG.debug("Call validate password for systems and default password policy for account [{}]",
				event.getContent().getUid());
		//
		List<IdmPasswordPolicyDto> passwordPolicyList = validateDefinition(account);
		//
		// validate
		AccPasswordValidationDto passwordValidationDto = new AccPasswordValidationDto();
		// get old password for validation - till, from and password history
		AccPasswordDto oldPassword = this.passwordService.findOneByAccount(account.getId());
		passwordValidationDto.setOldPassword(oldPassword == null ? null : oldPassword.getId());
		passwordValidationDto.setAccount(account);
		passwordValidationDto.setPassword(passwordChangeDto.getNewPassword());
		this.passwordPolicyService.validate(passwordValidationDto, passwordPolicyList);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Method returns password policy list for account
	 * 
	 * @param account
	 * @return
	 * 
	 */
	public List<IdmPasswordPolicyDto> validateDefinition(AccAccountDto account) {
        // get account password policy
		IdmPasswordPolicy passwordPolicyEntity = accountRepository.findById(account.getId()).get()
				.getSystem().getPasswordPolicyValidate();
		IdmPasswordPolicyDto passwordPolicy = null;
		if (passwordPolicyEntity != null) {
			passwordPolicy = passwordPolicyService.get(passwordPolicyEntity.getId());
		}
		// if passwordPolicy is null use default password policy for validate
		if (passwordPolicy == null) {
			passwordPolicy = this.passwordPolicyService.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
		}

		List<IdmPasswordPolicyDto> passwordPolicyList = new ArrayList<>();
		passwordPolicyList.add(passwordPolicy);
		return passwordPolicyList;
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PASSWORD_VALIDATION_ORDER;
	}

	@Override
	public boolean conditional(EntityEvent<AccAccountDto> event) {
		return super.conditional(event) && !getBooleanProperty(PasswordManageableProcessor.SKIP_PASSWORD_VALIDATION, event.getProperties());
	}
}
