package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;

/**
 * Save identity's password
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component(IdentityPasswordProcessor.PROCESSOR_NAME)
@Description("Persist identity's password.")
public class IdentityPasswordProcessor extends AbstractIdentityPasswordProcessor {

	public static final String PROCESSOR_NAME = "identity-password-processor";
	public static final String PROPERTY_PASSWORD_CHANGE_DTO = "idm:password-change-dto";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordProcessor.class);
	private final IdmPasswordService passwordService;
	private final IdmPasswordPolicyService passwordPolicyService;

	@Autowired
	public IdentityPasswordProcessor(
			IdmPasswordService passwordService,
			IdmPasswordPolicyService passwordPolicyService) {
		super(passwordService, IdentityEventType.PASSWORD);
		//
		Assert.notNull(passwordService, "Password service is required for password processor.");
		Assert.notNull(passwordPolicyService, "Password policy service is required for password processor.");
		//
		this.passwordService = passwordService;
		this.passwordPolicyService = passwordPolicyService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	/**
	 * Saves identity's password and fill valid till from password policy.
	 * 
	 * @param entity
	 * @param newPassword
	 */
	protected void savePassword(IdmIdentityDto entity, PasswordChangeDto passwordChangeDto) {
		LOG.debug("Saving password for identity [{}].", entity.getUsername());
		// 
		if (passwordChangeDto.getMaxPasswordAge() == null) {
			IdmPasswordPolicyDto defaultValidatePolicy = passwordPolicyService
					.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
			if (defaultValidatePolicy != null && defaultValidatePolicy.getMaxPasswordAge() != null) {
				// put new valid till by default password policy
				passwordChangeDto.setMaxPasswordAge(ZonedDateTime.now().plusDays(defaultValidatePolicy.getMaxPasswordAge()));
			} else {
				passwordChangeDto.setMaxPasswordAge(null);
				LOG.info("Default validate password policy not exists or max password age is not filled."
						+ " For identity username [{}] will be valid till null.", entity.getUsername());
			}
		}
		this.passwordService.save(entity, passwordChangeDto);
	}

	/**
	 * Delete identity's password from confidential storage
	 * 
	 * @param entity
	 */
	protected void deletePassword(IdmIdentityDto entity) {
		LOG.debug("Deleting password for identity [{}]. ", entity.getUsername());
		this.passwordService.delete(entity);
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}
}
