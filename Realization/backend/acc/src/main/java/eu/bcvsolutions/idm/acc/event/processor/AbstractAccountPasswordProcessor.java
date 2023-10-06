package eu.bcvsolutions.idm.acc.event.processor;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Implementation of account password change logic. Purpose of this abstraction is that password can be now changed from various
 * concrete implementations
 *
 * @author Jirka Koula
 */
public abstract class AbstractAccountPasswordProcessor
		extends CoreEventProcessor<AccAccountDto>
		implements AccountProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractAccountPasswordProcessor.class);
	public static final String PROPERTY_PASSWORD_CHANGE_DTO = "acc:password-change-dto";
	//
	private final AccPasswordService passwordService;

	public AbstractAccountPasswordProcessor(AccPasswordService passwordService, EventType... types) {
		super(types);
		//
		Assert.notNull(passwordService, "Password service is required for password processor.");
		//
		this.passwordService = passwordService;
	}

	@Override
	public EventResult<AccAccountDto> process(EntityEvent<AccAccountDto> event) {
		AccAccountDto account = event.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties().get(PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(passwordChangeDto, "Password change DTO is required for processing password change.");
		//
		savePassword(account, passwordChangeDto);
		Map<String, Object> parameters = new LinkedHashMap<>();
		parameters.put("account", new IdmAccountDto(
				account.getId(),
				true,
				account.getUid()));
		return new DefaultEventResult.Builder<>(event, this).setResult(
				new OperationResult.Builder(OperationState.EXECUTED)
					.setModel(new DefaultResultModel(CoreResultCode.PASSWORD_CHANGE_ACCOUNT_SUCCESS, parameters))
					.build()
				).build();
		// return new DefaultEventResult<>(event, this);
	}

	/**
	 * Saves account password
	 *
	 * @param account
	 * @param newPassword
	 */
	protected void savePassword(AccAccountDto account, PasswordChangeDto newPassword) {
		LOG.debug("Saving password for account [{}].", account.getUid());
		//
		this.passwordService.save(account, newPassword);
	}

	/**
	 * Delete identity's password from confidential storage
	 *
	 * @param account
	 */
	protected void deletePassword(AccAccountDto account) {
		LOG.debug("Deleting password for account [{}]. ", account.getUid());
		this.passwordService.delete(account);
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}
}
