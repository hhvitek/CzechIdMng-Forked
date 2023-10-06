package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.AccPasswordHistoryDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordHistoryFilter;
import eu.bcvsolutions.idm.acc.entity.AccPasswordHistory;
import eu.bcvsolutions.idm.acc.entity.AccPasswordHistory_;
import eu.bcvsolutions.idm.acc.repository.AccPasswordHistoryRepository;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordHistoryService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Default implementation of service that validate same account password trough history.
 *
 * @author Jirka Koula
 *
 */
@Service("accPasswordHistoryService")
public class DefaultAccPasswordHistoryService
		extends AbstractReadWriteDtoService<AccPasswordHistoryDto, AccPasswordHistory, AccPasswordHistoryFilter>
		implements AccPasswordHistoryService {

	@Autowired
	public DefaultAccPasswordHistoryService(AccPasswordHistoryRepository repository) {
		super(repository);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<AccPasswordHistory> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, AccPasswordHistoryFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// Identity id
		if (filter.getAccountId() != null) {
			predicates.add(builder.equal(root.get(AccPasswordHistory_.account).get(AbstractEntity_.id), filter.getAccountId()));
		}
		// From, the attribute is compared with created
		if (filter.getFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(AccPasswordHistory_.created), filter.getFrom()));
		}
		// Till, the attribute is compared with created
		if (filter.getTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(AccPasswordHistory_.created), filter.getTill()));
		}
		// Creator of history record username
		if (StringUtils.isNotEmpty(filter.getCreator())) {
			predicates.add(builder.equal(root.get(AccPasswordHistory_.creator), filter.getCreator()));
		}
		//
		return predicates;
	}

	@Override
	public boolean checkHistory(UUID accountId, int countOfIteration, GuardedString newPassword) {
		Assert.notNull(accountId, "Account id can't be null.");
		Assert.notNull(newPassword, "New password can't be null.");
		//
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
		//
		for (AccPasswordHistoryDto passwordHistory : getPasswordHistoryForAccount(accountId, countOfIteration)) {
			boolean matches = encoder.matches(newPassword.asString(), passwordHistory.getPassword());
			if (matches) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void deleteAllByAccount(UUID accountId) {
		Assert.notNull(accountId, "Account id can't be null.");
		//
		AccPasswordHistoryFilter filter = new AccPasswordHistoryFilter();
		filter.setAccountId(accountId);
		//
		for (AccPasswordHistoryDto passwordHistory : this.find(filter, null)) {
			this.delete(passwordHistory);
		}
	}

	/**
	 * Return list of {@link AccPasswordHistoryDto} for given account id.
	 *
	 * @param accountId
	 * @param count
	 * @return
	 */
	private List<AccPasswordHistoryDto> getPasswordHistoryForAccount(UUID accountId, int count) {
		AccPasswordHistoryFilter filter = new AccPasswordHistoryFilter();
		filter.setAccountId(accountId);
		return this.find(filter, PageRequest.of(0, count, new Sort(Direction.DESC, AccPasswordHistory_.created.getName()))).getContent();
	}

}
