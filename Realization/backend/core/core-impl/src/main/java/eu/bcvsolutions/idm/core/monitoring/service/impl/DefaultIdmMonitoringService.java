package eu.bcvsolutions.idm.core.monitoring.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.monitoring.api.domain.MonitoringGroupPermission;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoring;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoring_;
import eu.bcvsolutions.idm.core.monitoring.repository.IdmMonitoringRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * CRUD for configured monitoring evaluators.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Service("monitoringService")
public class DefaultIdmMonitoringService 
		extends AbstractEventableDtoService<IdmMonitoringDto, IdmMonitoring, IdmMonitoringFilter> 
		implements IdmMonitoringService {
	
	private final IdmMonitoringRepository repository;
	
	@Autowired
	public DefaultIdmMonitoringService(
			IdmMonitoringRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		this.repository = repository;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(MonitoringGroupPermission.MONITORING, getEntityClass());
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmMonitoringDto getByCode(String code) {
		return toDto(repository.findOneByCode(code));
	}
	
	@Override
	@Transactional
	public IdmMonitoringDto saveInternal(IdmMonitoringDto dto) {
		// preset code by id by default
		if (StringUtils.isEmpty(dto.getCode())) {
			if (dto.getId() == null) {
				dto.setId(UUID.randomUUID());
			}
			dto.setCode(dto.getId().toString());
		}
		//
		return super.saveInternal(dto);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmMonitoring> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmMonitoringFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// "fulltext"
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmMonitoring_.code)), "%" + text + "%"),
					builder.like(builder.lower(root.get(IdmMonitoring_.evaluatorType)), "%" + text + "%"),
					builder.like(builder.lower(root.get(IdmMonitoring_.description)), "%" + text + "%")
			));
		}
		//
		String evaluatorType = filter.getEvaluatorType();
		if (StringUtils.isNotEmpty(evaluatorType)) {
			predicates.add(builder.equal(root.get(IdmMonitoring_.evaluatorType), evaluatorType));
		}
		//
		return predicates;
	}
}
