package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;

/**
 * Entities on target system.
 * 
 * @author Radek Tomiška
 *
 */
@Service("sysSystemEntityService")
public class DefaultSysSystemEntityService
		extends AbstractReadWriteDtoService<SysSystemEntityDto, SysSystemEntity, SysSystemEntityFilter>
		implements SysSystemEntityService {

	private final SysSystemEntityRepository repository;
	//
	// TODO: after transformation to events can be this removed
	@Autowired @Lazy private SysProvisioningOperationService provisioningOperationService;
	@Autowired @Lazy private AccAccountService accountService;
	@Autowired @Lazy private SysSchemaObjectClassService schemaService;
	@Autowired private SysProvisioningBatchService batchService;
	@Autowired private SysSystemService systemService;
	@Autowired private LookupService lookupService;
	@Autowired private ConnectorManager connectorManager;

	@Autowired
	public DefaultSysSystemEntityService(SysSystemEntityRepository systemEntityRepository) {
		super(systemEntityRepository);
		//
		this.repository = systemEntityRepository;
	}

	@Override
	@Transactional
	public void deleteInternal(SysSystemEntityDto systemEntity) {
		Assert.notNull(systemEntity, "System entity is required.");
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(systemEntity.getSystem());
		filter.setEntityType(systemEntity.getEntityType());
		filter.setSystemEntity(systemEntity.getId());
		// TODO: transform this behavior to events
		if (provisioningOperationService.count(filter) > 0) {
			SysSystemDto system = DtoUtils.getEmbedded(systemEntity, SysSystemEntity_.system);
			throw new ResultCodeException(AccResultCode.SYSTEM_ENTITY_DELETE_FAILED_HAS_OPERATIONS,
					ImmutableMap.of("uid", systemEntity.getUid(), "system", system.getName()));
		}
		//
		// clear accounts - only link, can be rebuild
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setSystemEntityId(systemEntity.getId());
		accountService
			.find(accountFilter, null)
			.forEach(account -> {
				account.setSystemEntity(null);
				accountService.save(account);
			});
		//
		// clear batches
		SysProvisioningBatchDto batch = batchService.findBatch(systemEntity.getId());
		if (batch != null) {
			batchService.delete(batch);
		}
		//
		super.deleteInternal(systemEntity);
	}

	@Override
	@Transactional(readOnly = true)
	public SysSystemEntityDto getBySystemAndEntityTypeAndUid(
			SysSystemDto system, 
			String entityType,
			String uid) {
		return toDto(repository.findOneBySystem_IdAndEntityTypeAndUid(system.getId(), entityType, uid));
	}

	@Override
	@Transactional(readOnly = true)
	public SysSystemEntityDto getByProvisioningOperation(ProvisioningOperation operation) {
		if (operation instanceof SysProvisioningOperationDto) {
			return this.get(((SysProvisioningOperationDto) operation).getSystemEntity());
		}
		if (operation instanceof SysProvisioningArchiveDto) {
			return toDto(repository.findOneBySystem_IdAndEntityTypeAndUid(operation.getSystem(),
					operation.getEntityType(), ((SysProvisioningArchiveDto) operation).getSystemEntityUid()));
		}
		return null;
	}

	@Override
	public IcConnectorObject getConnectorObject(SysSystemEntityDto systemEntity, BasePermission... permissions) {
		Assert.notNull(systemEntity, "System entity cannot be null!");
		this.checkAccess(systemEntity, permissions);
		// Find connector-type.
		SysSystemDto systemDto = lookupService.lookupEmbeddedDto(systemEntity, SysSystemEntity_.system);
		ConnectorType connectorType = connectorManager.findConnectorTypeBySystem(systemDto);
		// Find first mapping for entity type and system, from the account and return his object class.
		IcObjectClass icObjectClass = schemaService.findByAccount(systemEntity.getSystem(), systemEntity.getEntityType());

		return this.systemService
				.readConnectorObject(systemEntity.getSystem(),
						systemEntity.getUid(),
						icObjectClass,
						connectorType);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<SysSystemEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder, SysSystemEntityFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			List<Predicate> textPredicates = new ArrayList<>(2);
			//
			textPredicates.add(builder.like(builder.lower(root.get(SysSystemEntity_.uid)), "%" + text + "%"));
			//
			predicates.add(builder.or(textPredicates.toArray(new Predicate[textPredicates.size()])));
		}
		//
		UUID systemId = filter.getSystemId();
		if (systemId != null) {
			predicates.add(builder.equal(root.get(SysSystemEntity_.system).get(SysSystem_.id), systemId));
		}
		String uid = filter.getUid();
		if (StringUtils.isNotEmpty(uid)) {
			predicates.add(builder.equal(root.get(SysSystemEntity_.uid), uid));
		}
		String entityType = filter.getEntityType();
		if (entityType != null) {
			predicates.add(builder.equal(root.get(SysSystemEntity_.entityType), entityType));
		}
		//
		return predicates;
	}
}
