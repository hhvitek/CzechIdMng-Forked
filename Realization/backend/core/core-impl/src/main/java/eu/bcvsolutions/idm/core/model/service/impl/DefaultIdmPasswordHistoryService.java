package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordHistoryFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordHistoryService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordHistory;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordHistory_;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordHistoryRepository;

/**
 * Default implementation of service that validate same password trough history.
 *
 * @author Ondrej Kopr
 * @author Jirka Koula
 *
 */
public class DefaultIdmPasswordHistoryService
		extends AbstractIdmPasswordHistoryService<IdmPasswordHistoryDto, IdmPasswordHistory, IdmPasswordHistory_, IdmPasswordHistoryFilter>
        implements IdmPasswordHistoryService {

	@Autowired
	public DefaultIdmPasswordHistoryService(IdmPasswordHistoryRepository repository) {
		super(repository, new IdmPasswordHistoryFilter());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmPasswordHistory> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmPasswordHistoryFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// Identity id
		if (filter.getIdentityId() != null) {
			predicates.add(builder.equal(root.get(IdmPasswordHistory_.identity).get(AbstractEntity_.id), filter.getIdentityId()));
		}
		// From, the attribute is compared with created
		if (filter.getFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(IdmPasswordHistory_.created), filter.getFrom()));
		}
		// Till, the attribute is compared with created
		if (filter.getTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(IdmPasswordHistory_.created), filter.getTill()));
		}
		// Identity username
		if (StringUtils.isNotEmpty(filter.getIdentityUsername())) {
			predicates.add(builder.equal(root.get(IdmPasswordHistory_.identity).get(IdmIdentity_.username), filter.getIdentityUsername()));
		}
		// Creator of history record username
		if (StringUtils.isNotEmpty(filter.getCreator())) {
			predicates.add(builder.equal(root.get(IdmPasswordHistory_.creator), filter.getCreator()));
		}
		//
		return predicates;
	}

}
