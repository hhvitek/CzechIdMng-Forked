package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordService;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.event.EntityPasswordEvent.EntityPasswordEventType;

/**
 * Save account password
 * 
 * @author Jirka Koula
 *
 */
@Component(AccountPasswordProcessor.PROCESSOR_NAME)
@Description("Persist account password.")
public class AccountPasswordProcessor extends AbstractAccountPasswordProcessor {

	public static final String PROCESSOR_NAME = "account-password-processor";
	public static final String PROPERTY_PASSWORD_CHANGE_DTO = "password-change-dto";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccountPasswordProcessor.class);
	private final AccPasswordService passwordService;

	@Autowired
	public AccountPasswordProcessor(AccPasswordService passwordService) {
		super(passwordService, EntityPasswordEventType.PASSWORD);
		//
		Assert.notNull(passwordService, "Password service is required for password processor.");
		//
		this.passwordService = passwordService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	/**
	 * Saves account password
	 * 
	 * @param entity
	 * @param passwordChangeDto
	 */
	protected void savePassword(AccAccountDto entity, PasswordChangeDto passwordChangeDto) {
		LOG.debug("Saving password for account [{}].", entity.getUid());
		// 
		this.passwordService.save(entity, passwordChangeDto);
	}

	/**
	 * Delete account password from confidential storage
	 * 
	 * @param entity
	 */
	protected void deletePassword(AccAccountDto entity) {
		LOG.debug("Deleting password for account [{}]. ", entity.getUid());
		this.passwordService.delete(entity);
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}
}
