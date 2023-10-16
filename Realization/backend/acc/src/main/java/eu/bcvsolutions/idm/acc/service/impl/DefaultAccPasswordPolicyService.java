package eu.bcvsolutions.idm.acc.service.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordHistoryDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordValidationDto;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordHistoryService;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordPolicyService;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordService;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyIdentityAttributes;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractIdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Account password policy service.
 * validation and generate method.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Ondrej Husnik
 * @author Jirka Koula
 *
 */
@Service("accPasswordPolicyService")
public class DefaultAccPasswordPolicyService
		extends AbstractIdmPasswordPolicyService<AccPasswordValidationDto, AccPasswordDto, AccAccountDto, AccPasswordHistoryDto>
		implements AccPasswordPolicyService {

	// TODO: better place for constant?
	private static final String PASSWORD_SIMILAR_USERNAME = "passwordSimilarUsername";
	private static final String PASSWORD_SIMILAR_USERNAME_PREVALIDATE = "passwordSimilarUsernamePreValidate";

	@Autowired
	public DefaultAccPasswordPolicyService(
			IdmPasswordPolicyRepository repository,
			EntityEventManager entityEventProcessorService,
			SecurityService securityService,
			AccPasswordService passwordService,
			AccPasswordHistoryService passwordHistoryService) {
		super(repository, entityEventProcessorService, securityService, passwordService, passwordHistoryService);
	}

	/**
	 * Method sets to which attribute of identity cannot be similar to password - pre-validation
	 * 
	 * @param passwordPolicy
	 * @param errors
	 */
	@Override
	protected void enhancedControlForSimilar(IdmPasswordPolicyDto passwordPolicy, Map<String, Object> errors) {
		if (passwordPolicy.isEnchancedControl()) {
			String[] attributes = passwordPolicy.getIdentityAttributeCheck().split(", ");
			for (String attribute : attributes) {
				if (attribute.equals(IdmPasswordPolicyIdentityAttributes.USERNAME.name())) {
					errors.put(PASSWORD_SIMILAR_USERNAME_PREVALIDATE, "");
				}
			}
		}
	}

	/**
	 * Method sets to which attribute of identity is similar to password - password validation
	 * 
	 * @param passwordPolicy
	 * @param passwordValidationDto
	 * @param errors
	 */
	@Override
	protected void enhancedControlForSimilar(IdmPasswordPolicyDto passwordPolicy,
			AccPasswordValidationDto passwordValidationDto, Map<String, Object> errors) {
		String password = passwordValidationDto.getPassword().asString();

		if (passwordPolicy.isEnchancedControl()) {
			String[] attributes = passwordPolicy.getIdentityAttributeCheck().split(", ");
			String passwordWithAccents = password.toLowerCase();
			AccAccountDto account = passwordValidationDto.getAccount();

			for (String attributeToCheck : attributes) {

				String value = null;
				String controlledValue = null;
				boolean containsSubstring = false;
				if (account != null) {
					if (IdmPasswordPolicyIdentityAttributes.USERNAME.name().equals(attributeToCheck)) {
						value = account.getUid();
						controlledValue = PASSWORD_SIMILAR_USERNAME;
						containsSubstring = containsGeneralSubstring(passwordWithAccents, value);
					}
				}

				if (containsSubstring) {
					errors.put(controlledValue, StringUtils.trim(value));
				}
			}
		}
	}
}
