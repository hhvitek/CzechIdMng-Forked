package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordHistoryService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword_;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordRepository;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Service for working with password.
 * Now is password connect only to entity IdmIdentity.
 *
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
public class DefaultIdmPasswordService
		extends AbstractEventableDtoService<IdmPasswordDto, IdmPassword, IdmPasswordFilter>
		implements IdmPasswordService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmPasswordService.class);
	//
	private final IdmPasswordHistoryService passwordHistoryService;
	private final LookupService lookupService;
	//
	@Autowired private SecurityService securityService;

	@Autowired
	public DefaultIdmPasswordService(
			IdmPasswordRepository repository,
			IdmPasswordPolicyRepository policyRepository,
			IdmPasswordHistoryService passwordHistoryService,
			LookupService lookupService,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		this.passwordHistoryService = passwordHistoryService;
		this.lookupService = lookupService;
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmPassword> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmPasswordFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (filter.getIdentityDisabled() != null) {
			predicates.add(builder.equal(root.get(IdmPassword_.identity).get(IdmIdentity_.disabled), filter.getIdentityDisabled()));
		}
		//
		if (StringUtils.isNotEmpty(filter.getPassword())) {
			predicates.add(builder.equal(root.get(IdmPassword_.password), filter.getPassword()));
		}
		//
		if (filter.getMustChange() != null) {
			predicates.add(builder.equal(root.get(IdmPassword_.mustChange), filter.getMustChange()));
		}
		//
		if (filter.getIdentityId() != null) {
			predicates.add(builder.equal(root.get(IdmPassword_.identity).get(IdmIdentity_.id), filter.getIdentityId()));
		}
		//
		if (StringUtils.isNotEmpty(filter.getIdentityUsername())) {
			predicates.add(builder.equal(root.get(IdmPassword_.identity).get(IdmIdentity_.username), filter.getIdentityUsername()));
		}
		//
		if (filter.getValidFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(IdmPassword_.validFrom), filter.getValidFrom()));
		}
		//
		if (filter.getValidTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(IdmPassword_.validTill), filter.getValidTill()));
		}
		//
		if (StringUtils.isNotEmpty(filter.getText())) {
			throw new UnsupportedOperationException("Filter by text is not supported");
		}
		//
		return predicates;
	}
	
	@Override
	@Transactional
	public IdmPasswordDto save(IdmIdentityDto identity, PasswordChangeDto passwordChangeDto) {
		Assert.notNull(identity, "Identity is required.");
		Assert.notNull(passwordChangeDto, "Password change dto is required.");
		Assert.notNull(passwordChangeDto.getNewPassword(), "New password is required.");
		GuardedString newPassword = passwordChangeDto.getNewPassword();
		//
		IdmPasswordDto password = getPasswordByIdentity(identity.getId());
		//
		if (password == null) {
			// identity has no password yet
			password = new IdmPasswordDto();
			password.setIdentity(identity.getId());
		}
		//
		if (passwordChangeDto.getMaxPasswordAge() != null) {
			password.setValidTill(passwordChangeDto.getMaxPasswordAge().toLocalDate());
		} else {
			password.setValidTill(null);
		}
		//
		UUID ownerId = password.getIdentity();
		UUID currentId = securityService.getCurrentId();
		//
		// resolve password valid from, if password is saved by other logged identity
		if (!Objects.equals(currentId, password.getIdentity()) // currentId can be null => system
				&& !passwordChangeDto.isSkipResetValidFrom()) {
			password.setValidFrom(null);
			LOG.debug("Password owner [{}] is different than logged identity [{}], "
					+ "password will not be checked for minimum days, when password is changed next time by password owner",
					ownerId, currentId);
		} else { // set new password validity ~ creation date
			LocalDate now = LocalDate.now();
			password.setValidFrom(now);
			LOG.trace("Password (for password owner [{}])  valid from [{}] set.", ownerId, now);
		}
		//
		password.setPassword(this.generateHash(newPassword, getSalt()));
		//
		// set must change password to false
		password.setMustChange(false);
		//
		// reset unsuccessful attempts, after password is changed
		password.resetUnsuccessfulAttempts();
		//
		// Clear block loging date
		password.setBlockLoginDate(null);
		//
		// create new password history with currently changed password
		createPasswordHistory(password);
		//
		return save(password);
	}

	@Override
	@Transactional
	public void delete(IdmIdentityDto identity) {
		Assert.notNull(identity, "Identity is required.");
		//
		IdmPasswordDto passwordDto = getPasswordByIdentity(identity.getId());
		if (passwordDto != null) {
			this.delete(passwordDto);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public IdmPasswordDto findOneByIdentity(UUID identity) {
		return this.getPasswordByIdentity(identity);
	}

	@Override
	@Transactional(readOnly = true)
	public IdmPasswordDto findOneByIdentity(String username) {
		return this.getPasswordByIdentityUsername(username);
	}

	@Override
	public boolean checkPassword(GuardedString passwordToCheck, IdmPasswordDto password) {
		// with null password cannot be identity authenticate
		if (password.getPassword() == null) {
			return false;
		}
		// isn't possible compare null password
		if (passwordToCheck == null || passwordToCheck.asString() == null) {
			return false;
		}
		return BCrypt.checkpw(passwordToCheck.asString(), password.getPassword());
	}

	@Override
	public String generateHash(GuardedString password, String salt) {
		return BCrypt.hashpw(password.asString(), salt);
	}
	
	@Override
	public String getSalt() {
		return BCrypt.gensalt(12);
	}

	@Override
	public void increaseUnsuccessfulAttempts(String username) {
		IdmPasswordDto passwordDto = getPasswordByIdentityUsername(username);
		if (passwordDto != null) {
			passwordDto = increaseUnsuccessfulAttempts(passwordDto);
		}
	}

	@Override
	public void setLastSuccessfulLogin(String username) {
		IdmPasswordDto passwordDto = getPasswordByIdentityUsername(username);
		if (passwordDto != null) {
			passwordDto = setLastSuccessfulLogin(passwordDto);
		}
	}
	
	@Override
	public IdmPasswordDto increaseUnsuccessfulAttempts(IdmPasswordDto passwordDto) {
		Assert.notNull(passwordDto, "Password DTO cannot be null!");
		passwordDto.increaseUnsuccessfulAttempts();
		return save(passwordDto);
	}

	@Override
	public IdmPasswordDto setLastSuccessfulLogin(IdmPasswordDto passwordDto) {
		Assert.notNull(passwordDto, "Password DTO cannot be null!");
		passwordDto.setLastSuccessfulLogin(ZonedDateTime.now());
		passwordDto.resetUnsuccessfulAttempts();
		passwordDto.setBlockLoginDate(null);
		return save(passwordDto);
	}
	
	@Override
	@Transactional
	public IdmPasswordDto findOrCreateByIdentity(Serializable codeable) {
		IdmIdentityDto identityDto = lookupService.lookupDto(IdmIdentityDto.class, codeable);
		//
		if (identityDto == null) {
			return null;
		}
		//
		UUID identityId = identityDto.getId();
		IdmPasswordDto passwordDto = this.findOneByIdentity(identityId);
		//
		if (passwordDto != null) {
			return passwordDto;
		}
		//
		// TODO: two passwords can be created in multi thread access (lock by identity before the get)
		passwordDto = new IdmPasswordDto();
		passwordDto.setIdentity(identityId);
		passwordDto.setMustChange(false);
		passwordDto.setValidFrom(LocalDate.now());
		//
		return this.save(passwordDto);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.PASSWORD, getEntityClass());
	}

	/**
	 * Method get IdmIdentityPassword by identity.
	 *
	 * @param identityId
	 * @return Object IdmIdentityPassword when password for identity was founded otherwise null.
	 */
	private IdmPasswordDto getPasswordByIdentity(UUID identityId) {
		Assert.notNull(identityId, "Identity identifier is required.");
		//
		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setIdentityId(identityId);
		// Isn't possible found more than one password for identity, on table exists unique index
		return this.find(filter, null).getContent().stream().findFirst().orElse(null);
	}

	/**
	 * Method get IdmIdentityPassword by username.
	 *
	 * @param username
	 * @return Object IdmIdentityPassword when password for identity was founded otherwise null.
	 */
	private IdmPasswordDto getPasswordByIdentityUsername(String username) {
		Assert.notNull(username, "Username is required.");
		//
		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setIdentityUsername(username);
		// Isn't possible found more than one password for identity, on table exists unique index
		return this.find(filter, null).getContent().stream().findFirst().orElse(null);
	}
	
	/**
	 * Create new password history. This is done after success password change in IdM.
	 *
	 * @param passwordDto
	 */
	private void createPasswordHistory(IdmPasswordDto passwordDto) {
		IdmPasswordHistoryDto passwordHistory = new IdmPasswordHistoryDto();
		passwordHistory.setIdentity(passwordDto.getIdentity());
		passwordHistory.setPassword(passwordDto.getPassword());
		
		passwordHistoryService.save(passwordHistory);
	}
}
