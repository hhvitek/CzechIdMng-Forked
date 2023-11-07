package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.entity.OperationResult_;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.repository.filter.MonitoringIgnorableFilterBuilder;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmProcessedTaskItemFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask_;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmProcessedTaskItem;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmProcessedTaskItem_;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmScheduledTask_;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmProcessedTaskItemRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation of processed items service.
 * 
 * @author Jan Helbich
 *
 */
public class DefaultIdmProcessedTaskItemService
	extends AbstractReadWriteDtoService<IdmProcessedTaskItemDto, IdmProcessedTaskItem, IdmProcessedTaskItemFilter>
	implements IdmProcessedTaskItemService {
	
	private final IdmProcessedTaskItemRepository repository;
	private final MonitoringIgnorableFilterBuilder monitoringIgnorableFilterBuilder;

	@Autowired
	public DefaultIdmProcessedTaskItemService(IdmProcessedTaskItemRepository repository, MonitoringIgnorableFilterBuilder monitoringIgnorableFilterBuilder) {
		super(repository);
		//
		this.repository = repository;
		this.monitoringIgnorableFilterBuilder = monitoringIgnorableFilterBuilder;
	}
	
	@Override
	@Transactional
	public IdmProcessedTaskItemDto saveInternal(IdmProcessedTaskItemDto dto) {
		Assert.notNull(dto, "DTO is required.");
		//
		if (dto.getLongRunningTask() != null && dto.getScheduledTaskQueueOwner() != null) {
			throw new CoreException("Item cannot be in both scheduled task queue and long running task log.");
		}
		if (dto.getLongRunningTask() == null && dto.getScheduledTaskQueueOwner() == null) {
			throw new CoreException("Item must have either queue (IdmScheduledTask) or log (IdmLongRunningTask) association.");
		}
		return super.saveInternal(dto);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.SCHEDULER, getEntityClass());
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmProcessedTaskItem> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmProcessedTaskItemFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// items queue filter
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.like(builder.lower(root.get(IdmProcessedTaskItem_.referencedDtoType)), "%" + filter.getText().toLowerCase() + "%"));
		}
		if (filter.getScheduledTaskId() != null) {
			predicates.add(builder.equal(root.get(IdmProcessedTaskItem_.scheduledTaskQueueOwner).get(IdmScheduledTask_.id), filter.getScheduledTaskId()));
		}
		if (filter.getFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(IdmProcessedTaskItem_.created), filter.getFrom()));
		}
		if (filter.getTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(IdmProcessedTaskItem_.created), filter.getTill().plusDays(1)));
		}
		if (filter.getReferencedEntityId() != null) {
			predicates.add(builder.equal(root.get(IdmProcessedTaskItem_.referencedEntityId), filter.getReferencedEntityId()));
		}
		String referencedEntityType = filter.getReferencedEntityType();
		if (StringUtils.isNotEmpty(referencedEntityType)) {
			predicates.add(builder.equal(root.get(IdmProcessedTaskItem_.referencedDtoType), referencedEntityType));
		}
		
		if (filter.getLongRunningTaskId() != null) {
			predicates.add(builder.equal(root.get(
					IdmProcessedTaskItem_.longRunningTask)
					.get(IdmLongRunningTask_.id),
					filter.getLongRunningTaskId()));
		}

		if (filter.getTaskMonitoringIgnored() != null) {
			IdmLongRunningTaskFilter taskFilter = new IdmLongRunningTaskFilter();
			taskFilter.setMonitoringIgnored(true);

			final Subquery<IdmLongRunningTask> subquery = query.subquery(IdmLongRunningTask.class);
			final Root<IdmLongRunningTask> subroot = subquery.from(IdmLongRunningTask.class);
			subquery.select(subroot)
					.where(builder.and(
							monitoringIgnorableFilterBuilder.getPredicate(subroot, subquery, builder, taskFilter),
							builder.equal(root.get(IdmProcessedTaskItem_.LONG_RUNNING_TASK),subroot.get(IdmLongRunningTask_.id))
					));

			predicates.add(
					filter.getTaskMonitoringIgnored() ? builder.exists(subquery) : builder.not(builder.exists(subquery))
			);
		}

		if (filter.getOperationState() != null) {
			predicates.add(builder.equal(root.get(IdmProcessedTaskItem_.operationResult).get(OperationResult_.state), filter.getOperationState()));
		}
		return predicates;
	}

	@Override
	@Transactional
	public void deleteAllByLongRunningTask(IdmLongRunningTaskDto dto) {
		Assert.notNull(dto, "DTO is required.");
		//
		repository.deleteAllByLongRunningTaskId(dto.getId());
	}

	@Override
	@Transactional
	public void deleteAllByScheduledTask(IdmScheduledTaskDto dto) {
		Assert.notNull(dto, "DTO is required.");
		//
		repository.deleteAllByScheduledTaskId(dto.getId());
	}
	
	@Override
	@Transactional
	public void deleteItem(UUID scheduledTaskId, UUID referencedEntityId) {
		Assert.notNull(scheduledTaskId, "Scheduled task identifier is required.");
		Assert.notNull(referencedEntityId, "Referenced entity identifier is required.");
		//
		repository.deleteItem(scheduledTaskId, referencedEntityId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<UUID> findAllRefEntityIdsInQueueByScheduledTaskId(UUID scheduledTaskId) {
		Assert.notNull(scheduledTaskId, "Scheduled task identifier is required.");
		//
		return repository.findAllRefEntityIdsByScheduledTaskId(scheduledTaskId);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmProcessedTaskItemDto> findQueueItems(IdmScheduledTaskDto scheduledTask, Pageable pageable) {
		Assert.notNull(scheduledTask, "Scheduled task is required.");
		//
		IdmProcessedTaskItemFilter f = new IdmProcessedTaskItemFilter();
		f.setScheduledTaskId(scheduledTask.getId());
		return this.find(f, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmProcessedTaskItemDto> findLogItems(IdmLongRunningTaskDto longRunningTask, Pageable pageable) {
		Assert.notNull(longRunningTask, "Task is required.");
		//
		IdmProcessedTaskItemFilter f = new IdmProcessedTaskItemFilter();
		f.setLongRunningTaskId(longRunningTask.getId());
		return this.find(f, pageable);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public <E extends AbstractDto> IdmProcessedTaskItemDto createLogItem(
			E processedItem, 
			OperationResult result, 
			UUID longRunningTaskId) {
		Assert.notNull(processedItem, "Item is required.");
		Assert.notNull(result, "Result is required.");
		Assert.notNull(longRunningTaskId, "Task task is required.");
		//
		IdmProcessedTaskItemDto item = createProcessedItemDto(processedItem, result);
		item.setLongRunningTask(longRunningTaskId);
		//
		return this.saveInternal(item);
	}

	@Override
	@Transactional
	public <E extends AbstractDto> IdmProcessedTaskItemDto createQueueItem(E processedItem, OperationResult result, UUID scheduledTaskId) {
		Assert.notNull(processedItem, "Item is required.");
		Assert.notNull(result, "Result is required.");
		Assert.notNull(scheduledTaskId, "Scheduled task identifier is required.");
		//
		IdmProcessedTaskItemDto item = createProcessedItemDto(processedItem, result);
		item.setScheduledTaskQueueOwner(scheduledTaskId);
		//
		return this.saveInternal(item);
	}
	
	private <E extends AbstractDto> IdmProcessedTaskItemDto createProcessedItemDto(E dto, OperationResult opResult) {
		IdmProcessedTaskItemDto item = new IdmProcessedTaskItemDto();
		item.setReferencedEntityId(dto.getId());
		item.setReferencedDtoType(dto.getClass().getCanonicalName());
		item.setOperationResult(opResult);
		return item;
	}
	
}
