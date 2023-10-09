package eu.bcvsolutions.idm.core.model.event.processor;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PasswordManageable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.service.AbstractPasswordService;

/**
 * Implementation of password change logic. Purpose of this abstraction is that password can be now changed from various
 * concrete implementations
 *
 * @author Jirka Koula
 */
public abstract class AbstractPasswordProcessor<DTO extends AbstractDto & PasswordManageable, S extends AbstractPasswordService<?, DTO, ?>>
		extends CoreEventProcessor<DTO> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractPasswordProcessor.class);
	public static final String PROPERTY_PASSWORD_CHANGE_DTO = "password-change-dto";
	//
	private final S passwordService;

	public AbstractPasswordProcessor(S passwordService, EventType... types) {
		super(types);
		//
		Assert.notNull(passwordService, "Password service is required for password processor.");
		//
		this.passwordService = passwordService;
	}

	@Override
	public EventResult<DTO> process(EntityEvent<DTO> event) {
		DTO entity = event.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties().get(PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(passwordChangeDto, "Password change DTO is required for processing password change.");
		//
		if (passwordChangeDto.isIdm()) { // change IdM entity password
			savePassword(entity, passwordChangeDto);
			Map<String, Object> parameters = new LinkedHashMap<>();
			parameters.put("account", new IdmAccountDto(
					entity.getId(),
					true, 
					entity.getName()));
			return new DefaultEventResult.Builder<>(event, this).setResult(
					new OperationResult.Builder(OperationState.EXECUTED)
						.setModel(new DefaultResultModel(CoreResultCode.PASSWORD_CHANGE_ACCOUNT_SUCCESS, parameters))
						.build()
					).build();
		}
		return new DefaultEventResult<>(event, this);
	}

	/**
	 * Save entity password
	 *
	 * @param entity
	 * @param newPassword
	 */
	protected void savePassword(DTO entity, PasswordChangeDto newPassword) {
		LOG.debug("Saving password for entity [{}].", entity.getName());
		//
		this.passwordService.save(entity, newPassword);
	}

	/**
	 * Delete entity password from confidential storage
	 *
	 * @param entity
	 */
	protected void deletePassword(DTO entity) {
		LOG.debug("Deleting password for entity [{}]. ", entity.getName());
		this.passwordService.delete(entity);
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}
}
