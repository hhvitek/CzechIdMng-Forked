package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyIdentityAttributes;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.exception.PasswordChangeException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordHistoryService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Password policy service.
 * validation and generate method.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Ondrej Husnik
 *
 */
public class DefaultIdmPasswordPolicyService
		extends AbstractIdmPasswordPolicyService<IdmPasswordValidationDto, IdmPasswordDto, IdmIdentityDto, IdmPasswordHistoryDto>
		implements IdmPasswordPolicyService {
	
	// TODO: better place for constant?
	private static final String PASSWORD_SIMILAR_USERNAME = "passwordSimilarUsername";
	private static final String PASSWORD_SIMILAR_EMAIL = "passwordSimilarEmail";
	private static final String PASSWORD_SIMILAR_FIRSTNAME = "passwordSimilarFirstName";
	private static final String PASSWORD_SIMILAR_LASTNAME = "passwordSimilarLastName";
	private static final String PASSWORD_SIMILAR_TITLESAFTER = "passwordSimilarTitlesAfter";
	private static final String PASSWORD_SIMILAR_TITLESBEFORE = "passwordSimilarTitlesBefore";
	private static final String PASSWORD_SIMILAR_EXTERNALCODE = "passwordSimilarExternalCode";
	private static final String PASSWORD_SIMILAR_USERNAME_PREVALIDATE = "passwordSimilarUsernamePreValidate";
	private static final String PASSWORD_SIMILAR_EMAIL_PREVALIDATE = "passwordSimilarEmailPreValidate";
	private static final String PASSWORD_SIMILAR_FIRSTNAME_PREVALIDATE = "passwordSimilarFirstNamePreValidate";
	private static final String PASSWORD_SIMILAR_LASTNAME_PREVALIDATE = "passwordSimilarLastNamePreValidate";
	private static final String PASSWORD_SIMILAR_TITLESAFTER_PREVALIDATE = "passwordSimilarTitlesAfterPreValidate";
	private static final String PASSWORD_SIMILAR_TITLESBEFORE_PREVALIDATE = "passwordSimilarTitlesBeforePreValidate";
	private static final String PASSWORD_SIMILAR_EXTERNALCODE_PREVALIDATE = "passwordSimilarExternalCodePreValidate";

	@Autowired
	public DefaultIdmPasswordPolicyService(
			IdmPasswordPolicyRepository repository,
			EntityEventManager entityEventProcessorService,
			SecurityService securityService,
			IdmPasswordService passwordService,
			IdmPasswordHistoryService passwordHistoryService) {
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
				if (attribute.equals(IdmPasswordPolicyIdentityAttributes.EMAIL.name())) {
					errors.put(PASSWORD_SIMILAR_EMAIL_PREVALIDATE, "");
				} else if (attribute.equals(IdmPasswordPolicyIdentityAttributes.FIRSTNAME.name())) {
					errors.put(PASSWORD_SIMILAR_FIRSTNAME_PREVALIDATE, "");
				} else if (attribute.equals(IdmPasswordPolicyIdentityAttributes.LASTNAME.name())) {
					errors.put(PASSWORD_SIMILAR_LASTNAME_PREVALIDATE, "");
				} else if (attribute.equals(IdmPasswordPolicyIdentityAttributes.USERNAME.name())) {
					errors.put(PASSWORD_SIMILAR_USERNAME_PREVALIDATE, "");
				} else if (attribute.equals(IdmPasswordPolicyIdentityAttributes.TITLESBEFORE.name())) {
					errors.put(PASSWORD_SIMILAR_TITLESBEFORE_PREVALIDATE, "");
				} else if (attribute.equals(IdmPasswordPolicyIdentityAttributes.TITLESAFTER.name())) {
					errors.put(PASSWORD_SIMILAR_TITLESAFTER_PREVALIDATE, "");
				} else if (attribute.equals(IdmPasswordPolicyIdentityAttributes.EXTERNALCODE.name())) {
					errors.put(PASSWORD_SIMILAR_EXTERNALCODE_PREVALIDATE, "");
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
			IdmPasswordValidationDto passwordValidationDto, Map<String, Object> errors) {
		String password = passwordValidationDto.getPassword().asString();

		if (passwordPolicy.isEnchancedControl()) {
			String[] attributes = passwordPolicy.getIdentityAttributeCheck().split(", ");
			String passwordWithAccents = password.toLowerCase();
			IdmIdentityDto identity = passwordValidationDto.getIdentity();

			for (String attributeToCheck : attributes) {

				String value = null;
				String controlledValue = null;
				boolean containsSubstring = false;

				if (identity != null) {
					if (IdmPasswordPolicyIdentityAttributes.EMAIL.name().equals(attributeToCheck)) {
						value = identity.getEmail();
						controlledValue = PASSWORD_SIMILAR_EMAIL;
						containsSubstring = containsEmailSubstring(passwordWithAccents, value);
					} else if (IdmPasswordPolicyIdentityAttributes.FIRSTNAME.name().equals(attributeToCheck)) {
						value = identity.getFirstName();
						controlledValue = PASSWORD_SIMILAR_FIRSTNAME;
						containsSubstring = containsGeneralSubstring(passwordWithAccents, value);
					} else if (IdmPasswordPolicyIdentityAttributes.LASTNAME.name().equals(attributeToCheck)) {
						value = identity.getLastName();
						controlledValue = PASSWORD_SIMILAR_LASTNAME;
						containsSubstring = containsGeneralSubstring(passwordWithAccents, value);
					} else if (IdmPasswordPolicyIdentityAttributes.USERNAME.name().equals(attributeToCheck)) {
						value = identity.getUsername();
						controlledValue = PASSWORD_SIMILAR_USERNAME;
						containsSubstring = containsGeneralSubstring(passwordWithAccents, value);
					} else if (IdmPasswordPolicyIdentityAttributes.EXTERNALCODE.name().equals(attributeToCheck)) {
						value = identity.getExternalCode();
						controlledValue = PASSWORD_SIMILAR_EXTERNALCODE;
						containsSubstring = containsGeneralSubstring(passwordWithAccents, value);
					} else if (IdmPasswordPolicyIdentityAttributes.TITLESBEFORE.name().equals(attributeToCheck)) {
						value = identity.getTitleBefore();
						controlledValue = PASSWORD_SIMILAR_TITLESBEFORE;
						containsSubstring = containsTitleSubstring(passwordWithAccents, value);
					} else if (IdmPasswordPolicyIdentityAttributes.TITLESAFTER.name().equals(attributeToCheck)) {
						value = identity.getTitleAfter();
						controlledValue = PASSWORD_SIMILAR_TITLESAFTER;
						containsSubstring = containsTitleSubstring(passwordWithAccents, value);
					}
				}

				if (containsSubstring) {
					errors.put(controlledValue, StringUtils.trim(value));
				}
			}
		}
	}

	/**
	 * This method checks presence of title string in the password.
	 * Needs special treating not to split titles according 'period' sign.
	 * Some of them would fall apart. ex. 'Ph.D.' -> 'Ph' and 'D' instead of required 'PhD'
	 * 
	 * @param passwod
	 * @param checkedStr
	 * @return
	 */
	private boolean containsTitleSubstring(String passwod, String checkedStr) {
		if (checkedStr == null) {
			return false;
		}
		String checkStringNoPeriod = checkedStr.replace(".", "");
		return containsGeneralSubstring(passwod, checkStringNoPeriod);
	}
	
	/**
	 * This method is specialization for searching of the email contained in password.
	 * Email is searched as a whole.
	 * 
	 * @param password
	 * @param checkedStr
	 * @return
	 */
	public boolean containsEmailSubstring(String password, String checkedStr) {
		checkedStr = StringUtils.trimToNull(checkedStr);
		if (StringUtils.isEmpty(checkedStr)) {
			return false;
		}
		String transformedValueWithAccents = StringUtils.lowerCase(checkedStr);
		boolean contains = password.contains(transformedValueWithAccents);
		if (!contains) {
			String passwordWithoutAccents = StringUtils.stripAccents(password);
			String transformedValueWithoutAccents = StringUtils.stripAccents(transformedValueWithAccents);
			contains = passwordWithoutAccents.contains(transformedValueWithoutAccents);
		}
		return contains;
	}

	@Override
	protected void checkMinimalAge(
			IdmPasswordValidationDto passwordValidationDto,
			IdmPasswordPolicyDto passwordPolicy,
			boolean prevalidation,
			ZonedDateTime now
	) {
		IdmIdentityDto entity = passwordValidationDto.getEntity();
		IdmPasswordDto oldPassword = null;
		// For checking with old password identity must has ID (for create doesn't exist ID)
		if (entity != null && entity.getId() != null) {
			oldPassword = passwordService.findOneByEntity(entity.getId());
		}

		// check if can change password for minimal age for change
		Integer minPasswordAge = passwordPolicy.getMinPasswordAge();
		boolean enforceMinPasswordAgeValidation = passwordValidationDto.isEnforceMinPasswordAgeValidation();
		if (minPasswordAge != null
				&& oldPassword != null
				&& !oldPassword.isMustChange()
				&& !prevalidation
				&& !securityService.isAdmin()
				&& (enforceMinPasswordAgeValidation // force => checked even when owner and logged user is different
				|| Objects.equals(securityService.getCurrentId(), oldPassword.getEntity()))) {
			LocalDate passwordValidFrom = oldPassword.getValidFrom();
			if (passwordValidFrom != null // check can be disabled (by previous change attempt)
					&& passwordValidFrom.plusDays(minPasswordAge).isAfter(now.toLocalDate())) {
				throw new PasswordChangeException(passwordValidFrom.plusDays(minPasswordAge));
			}
		}
	}
}
