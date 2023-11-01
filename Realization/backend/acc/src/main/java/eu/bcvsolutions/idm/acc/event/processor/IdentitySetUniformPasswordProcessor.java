package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.service.api.UniformPasswordManager;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.event.EntityPasswordEvent;
import eu.bcvsolutions.idm.core.model.event.EntityPasswordEvent.EntityPasswordEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Set uniform password for identity to the IdM.
 *
 * @author Vít Švanda
 * @since 11.0.0
 */
@Component(IdentitySetUniformPasswordProcessor.PROCESSOR_NAME)
@Description("Set uniform password for identity to the IdM.")
public class IdentitySetUniformPasswordProcessor
		extends IdentityInitUniformPasswordProcessor
		implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "acc-identity-set-common-password-processor";

	@Autowired
	private UniformPasswordManager uniformPasswordManager;

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto previousIdentity = event.getOriginalSource();
		IdmIdentityDto newIdentity = event.getContent();
		if (stateStarting(previousIdentity, newIdentity)) {
			// Change/set uniform password to the identity (IdM)
			GuardedString password = uniformPasswordManager.generateUniformPassword(newIdentity.getId(), IdmIdentityDto.class, newIdentity.getTransactionId());
			PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
			passwordChangeDto.setNewPassword(password);
			passwordChangeDto.setIdm(true);
			// For this case the identity could not have accounts, but we need to set strategy "Change all",
			// because strategy "PasswordChangeType.ALL_ONLY" can be used.
			passwordChangeDto.setAll(true);

			// Publish event for changing password.
			EntityPasswordEvent identityEvent = new EntityPasswordEvent<IdmIdentityDto>(
					EntityPasswordEventType.PASSWORD,
					newIdentity,
					ImmutableMap.of(
							IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO, passwordChangeDto,
							EntityEventManager.EVENT_PROPERTY_SKIP_NOTIFICATION, Boolean.TRUE)); // Notification will be send after end of sync.
			getEntityEventManager().process(identityEvent);
			//
			return new DefaultEventResult<>(event, this);
		}
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean conditional(EntityEvent<IdmIdentityDto> event) {
		return uniformPasswordManager.shouldBePasswordSetToIdM() && super.conditional(event);
	}

	/**
	 * After update IdentityInitUniformPasswordProcessor.
	 */
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 155;
	}
}
