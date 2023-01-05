package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.event.SchemaAttributeEvent;
import eu.bcvsolutions.idm.acc.event.SchemaAttributeEvent.SchemaAttributeEventType;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.AccSchemaFormAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Default schema attributes
 * 
 * @author svandav
 * @author Tomáš Doischer
 *
 */
@Service
public class DefaultSysSchemaAttributeService extends AbstractReadWriteDtoService<SysSchemaAttributeDto, SysSchemaAttribute, SysSchemaAttributeFilter>
		implements SysSchemaAttributeService {

	private final SysSchemaAttributeRepository repository;
	private final EntityEventManager entityEventManager;
	
	@Autowired
	@Lazy
	private AccSchemaFormAttributeService schemaFormAttributeService;
	
	@Autowired
	public DefaultSysSchemaAttributeService(
			SysSchemaAttributeRepository repository,
			EntityEventManager entityEventManager) {
		super(repository);
		//
		Assert.notNull(entityEventManager, "Manager is required.");
		//
		this.repository = repository;
		this.entityEventManager = entityEventManager;
	}
	
	@Override
	protected Page<SysSchemaAttribute> findEntities(SysSchemaAttributeFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}
	
	@Override
	@Transactional
	public void delete(SysSchemaAttributeDto schemaAttribute, BasePermission... permission) {
		Assert.notNull(schemaAttribute, "Schema attribute is required.");
		//
		checkAccess(this.getEntity(schemaAttribute.getId()), permission);
		//
		entityEventManager.process(new SchemaAttributeEvent(SchemaAttributeEventType.DELETE, schemaAttribute));
	}
	
	@Override
	public SysSchemaAttributeDto clone(UUID id) {
		SysSchemaAttributeDto original = this.get(id);
		Assert.notNull(original, "Schema attribute must be found!");

		original.setId(null);
		EntityUtils.clearAuditFields(original);
		return original;
	}
	
	@Override
	@Transactional
	public SysSchemaAttributeDto save(SysSchemaAttributeDto dto, BasePermission... permission) {
		schemaFormAttributeService.createSchemaFormAttribute(dto);
		
		return super.save(dto, permission);
	}
	

	@Override
	public Iterable<SysSchemaAttributeDto> saveAll(Iterable<SysSchemaAttributeDto> dtos, BasePermission... permission) {
		dtos.forEach(dto -> schemaFormAttributeService.createSchemaFormAttribute(dto));
		
		return super.saveAll(dtos, permission);
	}
}
