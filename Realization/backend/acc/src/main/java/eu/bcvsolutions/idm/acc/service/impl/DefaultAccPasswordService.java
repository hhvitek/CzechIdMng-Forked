package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordHistoryDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccPassword;
import eu.bcvsolutions.idm.acc.entity.AccPassword_;
import eu.bcvsolutions.idm.acc.repository.AccPasswordRepository;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordHistoryService;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordService;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Service for working with account password.
 *
 * @author Jirka Koula
 */
@Service("accPasswordService")
public class DefaultAccPasswordService
		extends AbstractEventableDtoService<AccPasswordDto, AccPassword, AccPasswordFilter>
		implements AccPasswordService {

	private final AccPasswordHistoryService passwordHistoryService;
	private final LookupService lookupService;

	@Autowired
	public DefaultAccPasswordService(
			AccPasswordRepository repository,
			AccPasswordHistoryService passwordHistoryService,
			LookupService lookupService,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		this.passwordHistoryService = passwordHistoryService;
		this.lookupService = lookupService;
	}

	@Override
	protected List<Predicate> toPredicates(Root<AccPassword> root, CriteriaQuery<?> query, CriteriaBuilder builder,
										   AccPasswordFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (StringUtils.isNotEmpty(filter.getPassword())) {
			predicates.add(builder.equal(root.get(AccPassword_.password), filter.getPassword()));
		}
		//
		if (filter.getAccountId() != null) {
			predicates.add(builder.equal(root.get(AccPassword_.account).get(AccAccount_.id), filter.getAccountId()));
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
	public AccPasswordDto save(AccAccountDto account, PasswordChangeDto passwordChangeDto) {
		Assert.notNull(account, "Account is required.");
		Assert.notNull(passwordChangeDto, "Password change dto is required.");
		Assert.notNull(passwordChangeDto.getNewPassword(), "New password is required.");
		GuardedString newPassword = passwordChangeDto.getNewPassword();
		//
		AccPasswordDto password = getPasswordByAccount(account.getId());
		//
		if (password == null) {
			// account has no password yet
			password = new AccPasswordDto();
			password.setAccount(account.getId());
		}
		//
		password.setPassword(this.generateHash(newPassword, getSalt()));
		//
		// create new password history with currently changed password
		createPasswordHistory(password);
		//
		return save(password);
	}

	@Override
	@Transactional
	public void delete(AccAccountDto account) {
		Assert.notNull(account, "Account is required.");
		//
		AccPasswordDto passwordDto = getPasswordByAccount(account.getId());
		if (passwordDto != null) {
			this.delete(passwordDto);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public AccPasswordDto findOneByAccount(UUID account) {
		return this.getPasswordByAccount(account);
	}

	@Override
	public boolean checkPassword(GuardedString passwordToCheck, AccPasswordDto password) {
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
	public AccPasswordDto findOneByEntity(UUID entityId) {
		return findOneByAccount(entityId);
	}

	@Override
	@Transactional
	public AccPasswordDto findOrCreateByAccount(Serializable codeable) {
		AccAccountDto accountDto = lookupService.lookupDto(AccAccountDto.class, codeable);
		//
		if (accountDto == null) {
			return null;
		}
		//
		UUID accountId = accountDto.getId();
		AccPasswordDto passwordDto = this.findOneByAccount(accountId);
		//
		if (passwordDto != null) {
			return passwordDto;
		}
		//
		// TODO: two passwords can be created in multi thread access (lock by account before the get)
		passwordDto = new AccPasswordDto();
		passwordDto.setAccount(accountId);
		//
		return this.save(passwordDto);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.PASSWORD, getEntityClass());
	}

	/**
	 * Method get AccPasswordDto by account.
	 *
	 * @param accountId
	 * @return Object AccPasswordDto when password for account was founded otherwise null.
	 */
	private AccPasswordDto getPasswordByAccount(UUID accountId) {
		Assert.notNull(accountId, "Account identifier is required.");
		//
		AccPasswordFilter filter = new AccPasswordFilter();
		filter.setAccountId(accountId);
		// Isn't possible found more than one password for account, on table exists unique index
		return this.find(filter, null).getContent().stream().findFirst().orElse(null);
	}

	/**
	 * Create new password history. This is done after success password change in IdM.
	 *
	 * @param passwordDto
	 */
	private void createPasswordHistory(AccPasswordDto passwordDto) {
		AccPasswordHistoryDto passwordHistory = new AccPasswordHistoryDto();
		passwordHistory.setAccount(passwordDto.getAccount());
		passwordHistory.setPassword(passwordDto.getPassword());
		
		passwordHistoryService.save(passwordHistory);
	}
}
