package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.domain.PasswordManageable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordPolicyFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractPasswordHistoryService;
import eu.bcvsolutions.idm.core.api.service.AbstractPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.AbstractPasswordService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy_;
import eu.bcvsolutions.idm.core.model.event.PasswordPolicyEvent;
import eu.bcvsolutions.idm.core.model.event.PasswordPolicyEvent.PasswordPolicyEvenType;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Default common password policy service.
 * validation and generate method.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Ondrej Husnik
 * @author Jirka Koula
 *
 */

public abstract class AbstractIdmPasswordPolicyService<
		V extends AbstractPasswordValidationDto<E>,
		P extends AbstractPasswordDto,
		E extends AbstractDto & PasswordManageable,
		H extends AbstractPasswordHistoryDto>
		extends AbstractReadWriteDtoService<IdmPasswordPolicyDto, IdmPasswordPolicy, IdmPasswordPolicyFilter>
		implements AbstractPasswordPolicyService<V> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractIdmPasswordPolicyService.class);

	// TODO: better place for constant?
	private static final String MIN_LENGTH = "minLength";
	private static final String MAX_LENGTH = "maxLength";
	private static final String MIN_UPPER_CHAR = "minUpperChar";
	private static final String MIN_LOWER_CHAR = "minLowerChar";
	private static final String MIN_NUMBER = "minNumber";
	private static final String MIN_SPECIAL_CHAR = "minSpecialChar";
	private static final String COINTAIN_PROHIBITED = "prohibited";
	private static final String BEGIN_PROHIBITED = "beginProhibited";
	private static final String END_PROHIBITED = "endProhibited";
	// private static final String CONTAIN_WEAK_PASS = "weakPass"; // TODO
	private static final String MIN_RULES_TO_FULFILL = "minRulesToFulfill";
	private static final String MIN_RULES_TO_FULFILL_COUNT = "minRulesToFulfillCount";
	private static final String POLICY_NAME = "policiesNames";
	private static final String POLICY_NAME_PREVALIDATION = "policiesNamesPreValidation";
	private static final String SPECIAL_CHARACTER_BASE = "specialCharacterBase";
	private static final String FORBIDDEN_CHARACTER_BASE = "forbiddenCharacterBase";
	private static final String FORBIDDEN_BEGIN_CHARACTER_BASE = "forbiddenBeginCharacterBase";
	private static final String FORBIDDEN_END_CHARACTER_BASE = "forbiddenEndCharacterBase";
	private static final String MAX_HISTORY_SIMILAR = "maxHistorySimilar";
	private static final int MIN_CONSIDERED_ATTR_LEN = 3;
	private static final String DELIMITER_SET_REGEXP = ",\\.\\-—_£\\s";
	private static final int TEST_POLICY_CYCLES = 20;

	private PasswordGenerator passwordGenerator;
	private final IdmPasswordPolicyRepository repository;
	protected final SecurityService securityService;
	private final EntityEventManager entityEventProcessorService;
	protected final AbstractPasswordService<P, E, ?> passwordService;
	private final AbstractPasswordHistoryService<H, ?> passwordHistoryService;

	@Autowired
	protected AbstractIdmPasswordPolicyService(
			IdmPasswordPolicyRepository repository,
			EntityEventManager entityEventProcessorService,
			SecurityService securityService,
			AbstractPasswordService<P, E, ?> passwordService,
			AbstractPasswordHistoryService<H, ?> passwordHistoryService) {
		super(repository);
		//
		Assert.notNull(entityEventProcessorService, "Service is required.");
		Assert.notNull(repository, "Repository is required.");
		Assert.notNull(securityService, "Service is required.");
		Assert.notNull(passwordService, "Service is required.");
		Assert.notNull(passwordHistoryService, "Service is required.");
		//
		this.entityEventProcessorService = entityEventProcessorService;
		this.repository = repository;
		this.securityService = securityService;
		this.passwordService = passwordService;
		this.passwordHistoryService = passwordHistoryService;
	}

	@Override
	@Transactional
	public IdmPasswordPolicyDto save(IdmPasswordPolicyDto dto, BasePermission... permission) {
		Assert.notNull(dto, "DtTO is required to be saved.");
		//
		// TODO: this should be moved to save internal, can be bypassed by event publishing
		if (!ObjectUtils.isEmpty(permission)) {
			IdmPasswordPolicy persistEntity = null;
			if (dto.getId() != null) {
				persistEntity = this.getEntity(dto.getId());
				if (persistEntity != null) {
					// check access on previous entity - update is needed
					checkAccess(persistEntity, IdmBasePermission.UPDATE);
				}
			}
			checkAccess(toEntity(dto, persistEntity), permission); // TODO: remove one checkAccess?
		}
		// Check, if max attempts attribute is defined, then time of blocking must have defined too
		Integer maxAttempts = dto.getMaxUnsuccessfulAttempts();
		Integer blockLogin = dto.getBlockLoginTime();
		if (maxAttempts != null && maxAttempts > 0 && (blockLogin == null || blockLogin <= 0)) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_BLOCK_TIME_IS_REQUIRED, ImmutableMap.of("definition", dto.getName()));
		}
		if (dto.getType() == IdmPasswordPolicyType.GENERATE && dto.getGenerateType() == IdmPasswordPolicyGenerateType.RANDOM) {
			if (!this.getPasswordGenerator().testPolicySetting(dto, TEST_POLICY_CYCLES)) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_INVALID_SETTING);
			}
		}

		//
		LOG.debug("Saving entity [{}]", dto.getName());
		if (isNew(dto)) {
			// throw event with create
			return entityEventProcessorService.process(new PasswordPolicyEvent(PasswordPolicyEvenType.CREATE, dto)).getContent();
		}
		// else throw event with update
		return entityEventProcessorService.process(new PasswordPolicyEvent(PasswordPolicyEvenType.UPDATE, dto)).getContent();
	}

	@Override
	@Transactional
	public void delete(IdmPasswordPolicyDto dto, BasePermission... permission) {
		checkAccess(this.getEntity(dto.getId()), permission);
		//
		LOG.debug("Delete entity [{}]", dto.getName());
		//
		entityEventProcessorService.process(new PasswordPolicyEvent(PasswordPolicyEvenType.DELETE, dto));
	}

	@Override
	public void validate(V passwordValidationDto) {
		this.validate(passwordValidationDto, this.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE));
	}

	@Override
	public void validate(V passwordValidationDto, IdmPasswordPolicyDto passwordPolicy) {
		List<IdmPasswordPolicyDto> passwordPolicyList = new ArrayList<>();

		if (passwordPolicy != null) {
			passwordPolicyList.add(passwordPolicy);
		}

		this.validate(passwordValidationDto, passwordPolicyList);
	}

	@Override
	public void validate(V passwordValidationDto, List<IdmPasswordPolicyDto> passwordPolicyList) {
		validate(passwordValidationDto, passwordPolicyList, false);
	}

	@Override
	public IdmPasswordPolicyDto getDefaultPasswordPolicy(IdmPasswordPolicyType type) {
		IdmPasswordPolicy defaultPolicy = repository.findOneDefaultType(type);
		return this.toDto(defaultPolicy);
	}

	@Override
	public String generatePassword(IdmPasswordPolicyDto passwordPolicy) {
		Assert.notNull(passwordPolicy, "Password policy is requred to generate password.");
		Assert.doesNotContain(passwordPolicy.getType().name(), IdmPasswordPolicyType.VALIDATE.name(), "Bad type.");
		String generateRandom;

		// generate password with passphrase or random
		if (passwordPolicy.getGenerateType() == IdmPasswordPolicyGenerateType.PASSPHRASE) {
			generateRandom = this.getPasswordGenerator().generatePassphrase(passwordPolicy);
		} else {
			generateRandom = this.getPasswordGenerator().generateRandom(passwordPolicy);
		}

		StringBuilder result = new StringBuilder();
		// prepare prefix and suffix
		String prefix = passwordPolicy.getPrefix();
		String suffix = passwordPolicy.getSuffix();

		if (StringUtils.isNotEmpty(prefix)) {
			result.append(prefix);
		}

		// append default generated password
		result.append(generateRandom);

		if (StringUtils.isNotEmpty(suffix)) {
			result.append(suffix);
		}

		return result.toString();
	}

	@Override
	public PasswordGenerator getPasswordGenerator() {
		if (this.passwordGenerator == null) {
			passwordGenerator = new PasswordGenerator();
		}
		return passwordGenerator;
	}

	@Override
	public String generatePasswordByDefault() {
		IdmPasswordPolicyDto defaultPasswordPolicy = this.getDefaultPasswordPolicy(IdmPasswordPolicyType.GENERATE);

		// if default password policy for generating not exist
		// generate random string
		if (defaultPasswordPolicy == null) {
			return this.getPasswordGenerator().generateRandom();
		}

		return this.generatePassword(defaultPasswordPolicy);
	}

	@Override
	public Integer getMaxPasswordAge(List<IdmPasswordPolicyDto> policyList) {
		Assert.notNull(policyList, "Policy list is required.");
		//
		if (policyList.isEmpty()) {
			return null;
		}
		//
		Integer passwordAge = Integer.MIN_VALUE;
		for (IdmPasswordPolicyDto idmPasswordPolicy : policyList) {
			if (idmPasswordPolicy.getMaxPasswordAge() != 0 &&
					idmPasswordPolicy.getMaxPasswordAge() > passwordAge) {
				passwordAge = idmPasswordPolicy.getMaxPasswordAge();
			}
		}
		//
		if (passwordAge.equals(Integer.MIN_VALUE)) {
			return null;
		}
		//
		return passwordAge;
	}

	public void preValidate(V passwordValidationDto) {
		IdmPasswordPolicyDto defaultPolicy = this.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
		if (defaultPolicy == null) {
			defaultPolicy = new IdmPasswordPolicyDto();
		}
		List<IdmPasswordPolicyDto> passwordPolicyList = new ArrayList<>();
		passwordPolicyList.add(defaultPolicy);
		preValidate(passwordValidationDto, passwordPolicyList);
	}

	public void preValidate(V passwordValidationDto,
							List<IdmPasswordPolicyDto> passwordPolicyList) {
		passwordValidationDto.setPassword("");
		validate(passwordValidationDto, passwordPolicyList, true);
	}

	protected void checkMinimalAge(
			V passwordValidationDto,
			IdmPasswordPolicyDto passwordPolicy,
			boolean prevalidation,
			ZonedDateTime now
	) {
		// now account password doesn't have minimal age check
		// so let identity service override this empty default implementation
	}

	private void validate(
			V passwordValidationDto,
			List<IdmPasswordPolicyDto> passwordPolicies,
			boolean prevalidation) {
		Assert.notNull(passwordPolicies, "Password policies are required.");
		Assert.notNull(passwordValidationDto, "Password validation dto is required.");
		E entity = passwordValidationDto.getEntity();

		// default password policy is used when list of password policies is empty, or for get maximum equals password
		IdmPasswordPolicyDto defaultPolicy = this.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);

		// if list is empty, get default password policy
		if (passwordPolicies.isEmpty() && !prevalidation) {
			if (defaultPolicy != null) {
				passwordPolicies.add(defaultPolicy);
			}
		}

		// if list with password policies is empty, validate is always true
		if (passwordPolicies.isEmpty()) {
			// this state means that system IdM hasn't default password policy
			return;
		}

		String password = passwordValidationDto.getPassword().asString();

		ZonedDateTime now = ZonedDateTime.now();

		Map<String, Object> errors = new HashMap<>();
		Set<Character> prohibitedChar = new HashSet<>();
		String prohibitedBeginChar=null;
		String prohibitedEndChar=null;
		List<String> policyNames = new ArrayList<>();
		Map<String, Object> specialCharBase = new HashMap<>();
		Map<String, Object> forbiddenCharBase = new HashMap<>();
		Map<String, Object> forbiddenBeginCharBase = new HashMap<>();
		Map<String, Object> forbiddenEndCharBase = new HashMap<>();

		for (IdmPasswordPolicyDto passwordPolicy : passwordPolicies) {
			if (passwordPolicy.isDisabled()) {
				continue;
			}
			boolean validateNotSuccess = false;

			// check if can change password for minimal age for change xax
			checkMinimalAge(passwordValidationDto, passwordPolicy, prevalidation, now);

			// minimum rules to fulfill
			Map<String, Object> notPassRules = new HashMap<>();

			int minRulesToFulfill = passwordPolicy.getMinRulesToFulfill() == null ? 0
					: passwordPolicy.getMinRulesToFulfill();

			// check to max password length
			if (!isNull(passwordPolicy.getMaxPasswordLength()) && (password.length() > passwordPolicy.getMaxPasswordLength() ||  prevalidation)) {
				if (!passwordPolicy.isPasswordLengthRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MAX_LENGTH,
							Math.min(convertToInt(errors.get(MAX_LENGTH)), passwordPolicy.getMaxPasswordLength()));
				} else if (!(errors.containsKey(MAX_LENGTH)
						&& compareInt(passwordPolicy.getMaxPasswordLength(), errors.get(MAX_LENGTH)))) {
					errors.put(MAX_LENGTH, passwordPolicy.getMaxPasswordLength());
				}
				validateNotSuccess = true;
			}
			// check to minimal password length
			if (!isNullOrZeroValue(passwordPolicy.getMinPasswordLength())
					&& password.length() < passwordPolicy.getMinPasswordLength()) {
				if (!passwordPolicy.isPasswordLengthRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_LENGTH,
							Math.max(convertToInt(errors.get(MIN_LENGTH)), passwordPolicy.getMinPasswordLength()));
				} else if (!(errors.containsKey(MIN_LENGTH)
						&& compareInt(errors.get(MIN_LENGTH), passwordPolicy.getMinPasswordLength()))) {
					errors.put(MIN_LENGTH, passwordPolicy.getMinPasswordLength());
				}
				validateNotSuccess = true;
			}
			// check to prohibited characters
			if (!Strings.isNullOrEmpty(passwordPolicy.getProhibitedCharacters())
					&& !password.matches("[^" + Pattern.quote(passwordPolicy.getProhibitedCharacters()) + "]*")) {
				for (char character : passwordPolicy.getProhibitedCharacters().toCharArray()) {
					if (password.indexOf(character) >= 0) {
						prohibitedChar.add(character);
					}
				}
				validateNotSuccess = true;
			}
			// check character at the beginning
			prohibitedBeginChar = checkInitialFinalCharForbidden(password, passwordPolicy.getProhibitedBeginCharacters(), true);
			// check character at the end
			prohibitedEndChar = checkInitialFinalCharForbidden(password, passwordPolicy.getProhibitedEndCharacters(), false);

			// check to minimal numbers
			if (!isNullOrZeroValue(passwordPolicy.getMinNumber()) && !password.matches("(.*["
					+ Pattern.quote(passwordPolicy.getNumberBase()) + "].*){" + passwordPolicy.getMinNumber() + ",}")) {
				if (!passwordPolicy.isNumberRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_NUMBER,
							Math.max(convertToInt(errors.get(MIN_NUMBER)), passwordPolicy.getMinNumber()));
				} else if (!(errors.containsKey(MIN_NUMBER)
						&& compareInt(errors.get(MIN_NUMBER), passwordPolicy.getMinNumber()))) {
					errors.put(MIN_NUMBER, passwordPolicy.getMinNumber());
				}
				validateNotSuccess = true;
			}
			// check to minimal lower characters
			if (!isNullOrZeroValue(passwordPolicy.getMinLowerChar())
					&& !password.matches("(.*[" + Pattern.quote(passwordPolicy.getLowerCharBase()) + "].*){"
							+ passwordPolicy.getMinLowerChar() + ",}")) {
				if (!passwordPolicy.isLowerCharRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_LOWER_CHAR,
							Math.max(convertToInt(errors.get(MIN_LOWER_CHAR)), passwordPolicy.getMinLowerChar()));
				} else if (!(errors.containsKey(MIN_LOWER_CHAR)
						&& compareInt(errors.get(MIN_LOWER_CHAR), passwordPolicy.getMinLowerChar()))) {
					errors.put(MIN_LOWER_CHAR, passwordPolicy.getMinLowerChar());
				}
				validateNotSuccess = true;
			}
			// check to minimal upper character
			if (!isNullOrZeroValue(passwordPolicy.getMinUpperChar())
					&& !password.matches("(.*[" + Pattern.quote(passwordPolicy.getUpperCharBase()) + "].*){"
							+ passwordPolicy.getMinUpperChar() + ",}")) {
				if (!passwordPolicy.isUpperCharRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_UPPER_CHAR,
							Math.max(convertToInt(errors.get(MIN_UPPER_CHAR)), passwordPolicy.getMinUpperChar()));
				} else if (!(errors.containsKey(MIN_UPPER_CHAR)
						&& compareInt(errors.get(MIN_UPPER_CHAR), passwordPolicy.getMinUpperChar()))) {
					errors.put(MIN_UPPER_CHAR, passwordPolicy.getMinUpperChar());
				}
				validateNotSuccess = true;
			}
			// check to minimal special character and add special character base
			if (!isNullOrZeroValue(passwordPolicy.getMinSpecialChar())
					&& !password.matches("(.*[" + Pattern.quote(passwordPolicy.getSpecialCharBase()) + "].*){"
							+ passwordPolicy.getMinSpecialChar() + ",}")) {
				if (!passwordPolicy.isSpecialCharRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_SPECIAL_CHAR,
							Math.max(convertToInt(errors.get(MIN_SPECIAL_CHAR)), passwordPolicy.getMinSpecialChar()));
					specialCharBase.put(passwordPolicy.getName(), passwordPolicy.getSpecialCharBase());
				} else if (!(errors.containsKey(MIN_SPECIAL_CHAR)
						&& compareInt(errors.get(MIN_SPECIAL_CHAR), passwordPolicy.getMinSpecialChar()))) {
					errors.put(MIN_SPECIAL_CHAR, passwordPolicy.getMinSpecialChar());
					specialCharBase.put(passwordPolicy.getName(), passwordPolicy.getSpecialCharBase());
				}
				validateNotSuccess = true;
			}

			// building of character bases of forbidden characters in passwords for password hints 
			if (passwordPolicy.getProhibitedCharacters() != null) {
				forbiddenCharBase.put(passwordPolicy.getName(), passwordPolicy.getProhibitedCharacters());
			}
			if(!Strings.isNullOrEmpty(passwordPolicy.getProhibitedBeginCharacters())) {
				forbiddenBeginCharBase.put(passwordPolicy.getName(), passwordPolicy.getProhibitedBeginCharacters());
			}

			if(!Strings.isNullOrEmpty(passwordPolicy.getProhibitedEndCharacters())) {
				forbiddenEndCharBase.put(passwordPolicy.getName(), passwordPolicy.getProhibitedEndCharacters());
			}

			if (!notPassRules.isEmpty() && passwordPolicy.isEnchancedControl()) {
				int notRequiredRules = passwordPolicy.getNotRequiredRules();
				int missingRules = notRequiredRules - notPassRules.size();
				if (missingRules - minRulesToFulfill < 0) {
					errors.put(MIN_RULES_TO_FULFILL_COUNT, minRulesToFulfill - missingRules);
					errors.put(MIN_RULES_TO_FULFILL, notPassRules);
				}
			}

			// if not success we want password policy name
			if (validateNotSuccess && !errors.isEmpty() && !prevalidation) {
				policyNames.add(passwordPolicy.getName());
			}

			// check to similar identity attributes, enhanced control
			if (prevalidation) {
				enhancedControlForSimilar(passwordPolicy, errors);
			} else {
				enhancedControlForSimilar(passwordPolicy, passwordValidationDto, errors);
			}

			// TODO: weak words
		}

		if (!specialCharBase.isEmpty() && prevalidation) {
			errors.put(SPECIAL_CHARACTER_BASE, specialCharBase);
		}

		if (!forbiddenCharBase.isEmpty() && prevalidation) {
			errors.put(FORBIDDEN_CHARACTER_BASE, forbiddenCharBase);
		}

		if (!forbiddenBeginCharBase.isEmpty() && prevalidation) {
			errors.put(FORBIDDEN_BEGIN_CHARACTER_BASE, forbiddenBeginCharBase);
		}

		if (!forbiddenEndCharBase.isEmpty() && prevalidation) {
			errors.put(FORBIDDEN_END_CHARACTER_BASE, forbiddenEndCharBase);
		}

		if (!policyNames.isEmpty() && !prevalidation) {
			String name = prevalidation ? POLICY_NAME_PREVALIDATION : POLICY_NAME;
			errors.put(name, String.join(", ", policyNames));
		}

		if (!prohibitedChar.isEmpty()) {
			errors.put(COINTAIN_PROHIBITED, prohibitedChar.toString());
		}

		if(!Strings.isNullOrEmpty(prohibitedBeginChar)) {
			errors.put(BEGIN_PROHIBITED, prohibitedBeginChar);
		}

		if(!Strings.isNullOrEmpty(prohibitedEndChar)) {
			errors.put(END_PROHIBITED, prohibitedEndChar);
		}

		// password history. Skip when doesn't exists settings, or identity isn't saved
		// in some case (tests) are save identity in one transaction and id doesn't exist
		if (!prevalidation && defaultPolicy != null) {
			Integer maxHistorySimilar = defaultPolicy.getMaxHistorySimilar();
			if (maxHistorySimilar != null && entity != null && entity.getId() != null) {
				boolean checkHistory = passwordHistoryService.checkHistory(passwordValidationDto.getEntity().getId(), maxHistorySimilar, passwordValidationDto.getPassword());

				if (checkHistory) {
					errors.put(MAX_HISTORY_SIMILAR, maxHistorySimilar);
				}
			}
		}

		if (!errors.isEmpty()) {
			// TODO: password policy audit
			if(prevalidation) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_PREVALIDATION, errors);
			}
			throw new ResultCodeException(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY, errors);
		}
	}

	/**
	 * Method sets to which attribute of identity cannot be similar to password - pre-validation
	 *
	 * @param passwordPolicy
	 * @param errors
	 */
	protected abstract void enhancedControlForSimilar(IdmPasswordPolicyDto passwordPolicy, Map<String, Object> errors);

	/**
	 * Method sets to which attribute of identity is similar to password - password validation
	 *
	 * @param passwordPolicy
	 * @param passwordValidationDto
	 * @param errors
	 */
	protected abstract void enhancedControlForSimilar(
			IdmPasswordPolicyDto passwordPolicy,
			V passwordValidationDto,
			Map<String, Object> errors
	);

	/**
	 *  Method splits checkedStr argument to individual substrings and then tests
	 *  whether they are contained in the password. If found, true is returned otherwise false.
	 *  Only substrings longer than MIN_CONSIDERED_ATTR_LEN are considered.
	 *
	 * @param password
	 * @param checkedStr
	 * @param delimiterSetRegExp
	 * @return
	 */
	private boolean findSubstringsByDelimiter(String password, String checkedStr, String delimiterSetRegExp) {
		if (password == null || checkedStr == null) {
			return false;
		}

		String[] splitted = checkedStr.split("[" + delimiterSetRegExp + "]+");
		for (String s : splitted) {
			if (s.length() < MIN_CONSIDERED_ATTR_LEN) {
				continue;
			}
			if (password.contains(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * General method searching for any of substrings from attribute in password.
	 *
	 *
	 * @param password
	 * @param checkedStr
	 * @return
	 */
	protected boolean containsGeneralSubstring(String password, String checkedStr) {
		checkedStr = StringUtils.trimToNull(checkedStr);
		if (StringUtils.isEmpty(checkedStr)) {
			return false;
		}
		String transformedValueWithAccents = StringUtils.lowerCase(checkedStr);
		boolean contains = findSubstringsByDelimiter(password, transformedValueWithAccents, DELIMITER_SET_REGEXP);
		if (!contains) {
			String passwordWithoutAccents = StringUtils.stripAccents(password);
			String transformedValueWithoutAccents = StringUtils.stripAccents(transformedValueWithAccents);
			contains = findSubstringsByDelimiter(passwordWithoutAccents, transformedValueWithoutAccents,
					DELIMITER_SET_REGEXP);
		}
		return contains;
	}

	/**
	 * Check whether string starts/ends with forbidden char
	 *
	 * @param checkedStr
	 * @param forbiddenChars
	 * @param isInitial - position toggle: true - initial char otherwise final char
	 * @return
	 */
	private String checkInitialFinalCharForbidden(String checkedStr, String forbiddenChars, boolean isInitial) {
		if (Strings.isNullOrEmpty(forbiddenChars) || Strings.isNullOrEmpty(checkedStr)) {
			return null;
		}
		int index = isInitial ? 0 : checkedStr.length() - 1;
		Character begin = checkedStr.charAt(index);
		if (forbiddenChars.contains(begin.toString())) {
			return begin.toString();
		}
		return null;
	}

	/**
	 * Method compare integer in o1 with o2
	 *
	 * @param o1
	 * @param o2
	 * @return
	 */
	private boolean compareInt(Object o1, Object o2) {
		Integer i1 = Integer.valueOf(o1.toString());
		Integer i2 = Integer.valueOf(o2.toString());

		return i1 > i2;
	}

	/**
	 * Convert integer value in object to int. If Object is null,
	 * return 0.
	 *
	 * @param object
	 * @return
	 */
	private int convertToInt(Object object) {
		if (object == null) {
			return 0;
		}
		return NumberUtils.toInt(object.toString());
	}

	/**
	 * Method checks if given {@link Integer} is null.
	 *
	 * @param number
	 * @return true if Integer is null.
	 */
	private boolean isNull(Integer number) {
		return number == null;
	}

	/**
	 * Method checks if given {@link Integer} is null or zero value.
	 *
	 * @param number
	 * @return true if Integer is null or zero value.
	 */
	private boolean isNullOrZeroValue(Integer number) {
		return isNull(number) || number.equals(0);
	}

	@Override
	public IdmPasswordPolicyDto getByCode(String code) {
		return findOneByName(code);
	}

	@Override
	public IdmPasswordPolicyDto findOneByName(String name) {
		return this.toDto(this.repository.findOneByName(name));
	}

	@Override
	protected List<Predicate> toPredicates(
			Root<IdmPasswordPolicy> root,
			CriteriaQuery<?> query,
			CriteriaBuilder builder,
			IdmPasswordPolicyFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick - "fulltext"
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			List<Predicate> textPredicates = new ArrayList<>(4);
			//
			RepositoryUtils.appendUuidIdentifierPredicate(textPredicates, root, builder, text);
			textPredicates.add(builder.like(builder.lower(root.get(IdmPasswordPolicy_.name)), "%" + text + "%"));
			textPredicates.add(builder.like(builder.lower(root.get(IdmPasswordPolicy_.description)), "%" + text + "%"));
			//
			predicates.add(builder.or(textPredicates.toArray(new Predicate[textPredicates.size()])));
		}
		//
		Boolean passwordLengthRequired = filter.getPasswordLengthRequired();
		if (passwordLengthRequired != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.passwordLengthRequired), passwordLengthRequired));
		}
		//
		Integer minPasswordLength = filter.getMinPasswordLength();
		if (minPasswordLength != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.minPasswordLength), minPasswordLength));
		}
		//
		Integer maxPasswordLength = filter.getMaxPasswordLength();
		if (maxPasswordLength != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.maxPasswordLength), maxPasswordLength));
		}
		//
		Boolean upperCharRequired = filter.getUpperCharRequired();
		if (upperCharRequired != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.upperCharRequired), upperCharRequired));
		}
		//
		Integer minUpperChar = filter.getMinUpperChar();
		if (minUpperChar != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.minUpperChar), minUpperChar));
		}
		//
		Boolean numberRequired = filter.getNumberRequired();
		if (numberRequired != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.numberRequired), numberRequired));
		}
		//
		Integer minNumber = filter.getMinNumber();
		if (minNumber != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.minNumber), minNumber));
		}
		//
		Boolean specialCharRequired = filter.getSpecialCharRequired();
		if (specialCharRequired != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.specialCharRequired), specialCharRequired));
		}
		//
		Integer minSpecialChar = filter.getMinSpecialChar();
		if (minSpecialChar != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.minSpecialChar), minSpecialChar));
		}
		//
		Boolean weakPassRequired = filter.getWeakPassRequired();
		if (weakPassRequired != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.weakPassRequired), weakPassRequired));
		}
		//
		String weakPass = filter.getWeakPass();
		if (!StringUtils.isEmpty(weakPass)) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.weakPass), weakPass));
		}
		//
		Integer maxPasswordAge = filter.getMaxPasswordAge();
		if (maxPasswordAge != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.maxPasswordAge), maxPasswordAge));
		}
		//
		Integer minPasswordAge = filter.getMinPasswordAge();
		if (minPasswordAge != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.minPasswordAge), minPasswordAge));
		}
		//
		Boolean enchancedControl = filter.getEnchancedControl();
		if (enchancedControl != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.enchancedControl), enchancedControl));
		}
		//
		Integer minRulesToFulfill = filter.getMinRulesToFulfill();
		if (minRulesToFulfill != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.minRulesToFulfill), minRulesToFulfill));
		}
		//
		IdmPasswordPolicyType type = filter.getType();
		if (type != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.type), type));
		}
		//
		Boolean defaultPolicy = filter.getDefaultPolicy();
		if (defaultPolicy != null) {
			predicates.add(builder.equal(root.get(IdmPasswordPolicy_.defaultPolicy), defaultPolicy));
		}
		//
		return predicates;
	}
}
