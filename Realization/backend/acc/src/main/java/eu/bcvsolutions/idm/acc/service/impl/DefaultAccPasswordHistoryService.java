package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.AccPasswordHistoryDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordHistoryFilter;
import eu.bcvsolutions.idm.acc.entity.AccPasswordHistory;
import eu.bcvsolutions.idm.acc.entity.AccPasswordHistory_;
import eu.bcvsolutions.idm.acc.repository.AccPasswordHistoryRepository;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordHistoryService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractIdmPasswordHistoryService;

/**
 * Default implementation of service that validate same account password trough history.
 *
 * @author Jirka Koula
 *
 */
@Service("accPasswordHistoryService")
public class DefaultAccPasswordHistoryService
		extends AbstractIdmPasswordHistoryService<AccPasswordHistoryDto, AccPasswordHistory, AccPasswordHistory_, AccPasswordHistoryFilter>
		implements AccPasswordHistoryService {

	@Autowired
	public DefaultAccPasswordHistoryService(AccPasswordHistoryRepository repository) {
		super(repository, new AccPasswordHistoryFilter());
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
}
