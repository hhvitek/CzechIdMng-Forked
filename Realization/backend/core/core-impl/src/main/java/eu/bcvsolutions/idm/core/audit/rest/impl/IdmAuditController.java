package eu.bcvsolutions.idm.core.audit.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditDiffDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditEntityDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmAuditFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmAuditService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.audit.entity.IdmAudit_;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * IdM audit endpoint.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/audits")
@Tag(name = IdmAuditController.TAG, description = "Read / search audit log")
@Transactional(readOnly = true)
public class IdmAuditController extends AbstractReadWriteDtoController<IdmAuditDto, IdmAuditFilter> {

	protected static final String TAG = "Audit";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmAuditController.class);
	//
	private final IdmAuditService auditService;
	//
	@Autowired private ModelMapper mapper;
	
	@Autowired
	public IdmAuditController(IdmAuditService auditService) {
		super(auditService);
		//
		this.auditService = auditService;
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search audit logs",
			operationId = "searchQuickAudits", 
			tags = { IdmAuditController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.AUDIT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.AUDIT_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return this.find(parameters, pageable);
	}
	
	@Override
	public Page<IdmAuditDto> find(IdmAuditFilter filter, Pageable pageable, BasePermission permission) {
		Page<IdmAuditDto> dtos = super.find(filter, pageable, permission);
		Map<UUID, BaseDto> loadedDtos = new HashMap<>();
		dtos.forEach(dto -> {
			loadEmbeddedEntity(loadedDtos, dto);
		});
		//
		return dtos;
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@RequestMapping(value= "/search/entity", method = RequestMethod.GET)
	@Operation(
			summary = "Search audit logs related to entity",
			operationId = "searchEntity", 
			tags = { IdmAuditController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.AUDIT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.AUDIT_READ })
    })
    @Parameters({
         @Parameter(name = "entity", schema = @Schema( implementation=String.class, type = "query"), description = "Entity class - find related audit log to this class"),
    })
	@PageableAsQueryParam
	public CollectionModel<?> findEntity(
			@RequestParam(required = false) String entityClass,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		//
		// Because backward compatibility there must be set entity class and other useless parameters
		IdmAuditFilter filter = toFilter(parameters);
		if (StringUtils.isEmpty(filter.getOwnerType())) {
			throw new ResultCodeException(CoreResultCode.AUDIT_ENTITY_CLASS_IS_NOT_FILLED);
		}

		// Backward compatibility
		if (StringUtils.isEmpty(filter.getOwnerCode()) && parameters.containsKey(IdmIdentity_.username.getName())) {
			Object identityUsername = parameters.getFirst(IdmIdentity_.username.getName());
			if (identityUsername != null) {
				filter.setOwnerCode(identityUsername.toString());
			}
		}
		//
		Page<IdmAuditDto> dtos = auditService.find(filter, pageable);
		Map<UUID, BaseDto> loadedDtos = new HashMap<>();
		dtos.forEach(dto -> {
			loadEmbeddedEntity(loadedDtos, dto);
		});
		return toCollectionModel(dtos, getDtoClass());

	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@RequestMapping(value= "/search/login", method = RequestMethod.GET)
	@Operation(
			summary = "Search audit logs for login identities",
			operationId = "searchLoginAudits", 
			tags = { IdmAuditController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.AUDIT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.AUDIT_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findLogin(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		// Password hasn't own rest controller -> audit is solved by audit controller.
		return this.toCollectionModel(this.auditService.findLogin(toFilter(parameters), pageable), getDtoClass());
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@RequestMapping(value= "/search/entities", method = RequestMethod.GET)
	@Operation(
			summary = "Search audited entity classes",
			operationId = "findAllAuditedEntities", 
			tags = { IdmAuditController.TAG },
			description = "Method return list of class simple name for which is audited."
					+ " Must at least one attribute withannotation {@value Audited}")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.AUDIT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.AUDIT_READ })
    })
	public ResponseEntity<?> findAuditedEntity() {
		List<String> entities = auditService.getAllAuditedEntitiesNames();
		return new ResponseEntity<>(toCollectionModel(entities, null), HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@Override
	@Operation(
			summary = "Audit log detail",
			operationId = "getAuditLog", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmAuditDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmAuditController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.AUDIT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.AUDIT_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Audit log's identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmAuditDto audit = auditService.get(backendId);
		
		// Map with all values
		Map<String, Object> revisionValues = null;
		
		Object revision = null;
		try {
			revision = auditService.findVersion(Class.forName(audit.getType()), audit.getEntityId(), Long.valueOf(audit.getId().toString()));
		} catch (NumberFormatException | ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of("audit", audit), e);
		}
		
		revisionValues = auditService.getValuesFromVersion(revision);
		
		// create DTO and fill with values from IdmAudit
		IdmAuditDto auditDto = new IdmAuditDto();
		mapper.map(audit, auditDto);
		auditDto.setRevisionValues(revisionValues);
		
		return new ResponseEntity<IdmAuditDto>(auditDto, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{revId}/diff/previous")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@Operation(
			summary = "Audit log detail",
			operationId = "getAuditLogPreviousVersion", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmAuditDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmAuditController.TAG }, 
						description = "Returns previous version for given audit log")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.AUDIT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.AUDIT_READ })
    })
	public ResponseEntity<?> previousVersion(
			 @Parameter(description = "Audit log's identifier.", required = true)
			@PathVariable @NotNull String revId) {
		IdmAuditDto currentAudit = auditService.get(revId);
		IdmAuditDto previousAudit;
		ResponseEntity<IdmAuditDto> resource = null;
		
		try {
			IdmAuditDto dto = null;
			previousAudit = auditService.findPreviousRevision(currentAudit.getId());
			//
			// previous version dost'n exist
			if (previousAudit != null) {
				dto = new IdmAuditDto();
				mapper.map(previousAudit, dto);
				dto.setRevisionValues(
						auditService.getValuesFromVersion(
								auditService.findPreviousVersion(
										Class.forName(previousAudit.getType()),
										previousAudit.getEntityId(),
										previousAudit.getId())));
				resource = new ResponseEntity<IdmAuditDto>(dto, HttpStatus.OK);
			} else {
				resource = new ResponseEntity<IdmAuditDto>(HttpStatus.NOT_FOUND);
			}
			
		} catch (ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("audit class", currentAudit.getType()), e);
		}
		
		return resource;
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{firstRevId}/diff/{secondRevId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@Operation(
			summary = "Audit log diff",
			operationId = "getAuditLogDiff", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmAuditDiffDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmAuditController.TAG }, 
						description = "Returns diff between given audit logs versions")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.AUDIT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.AUDIT_READ })
    })
	public ResponseEntity<?> diff(
			 @Parameter(description = "Audit log's identifier.", required = true)
			@PathVariable @NotNull String firstRevId, 
			 @Parameter(description = "Audit log's identifier.", required = true)
			@PathVariable String secondRevId) {
		IdmAuditDiffDto dto = new IdmAuditDiffDto();
		dto.setDiffValues(auditService.getDiffBetweenVersion(Long.valueOf(firstRevId), Long.valueOf(secondRevId)));
		dto.setIdFirstRevision(Long.valueOf(firstRevId));
		dto.setIdSecondRevision(Long.valueOf(secondRevId));
		
		return new ResponseEntity<IdmAuditDiffDto>(dto, HttpStatus.OK);
	}
	
	/**
	 * Fills referenced entity to dto - prevent to load entity for each row.
	 * 
	 * @param dto
	 */
	private void loadEmbeddedEntity(Map<UUID, BaseDto> loadedDtos, IdmAuditDto dto) {
		UUID entityId = dto.getEntityId();
		if (entityId == null || StringUtils.isEmpty(dto.getType())) {
			return; // just for sure - IdmAudit entity doesn't specify it as required (but it should be)
		}
		// set context - add additional common props
		DataFilter context = new DataFilter(null);
		context.set(IdmFormValueFilter.PARAMETER_ADD_OWNER_DTO, Boolean.TRUE);
		BaseDto revision = null;
		if (loadedDtos.containsKey(entityId)) {
			revision = loadedDtos.get(entityId);
		} else {
			try {
				BaseEntity revisionEntity = getLookupService().lookupEntity(dto.getType(), entityId);
				if (revisionEntity != null) {
					revision = getLookupService().toDto(revisionEntity, null, context);
				}
				loadedDtos.put(entityId, revision);
			} catch (IllegalArgumentException ex) {
				LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", dto.getType(), ex);
			} catch (Exception ex) {
				LOG.debug("Type [{}] cannot be mapped to dto.", dto.getType(), ex);
			}
		}
		dto.getEmbedded().put(IdmAudit_.entityId.getName(), revision); // nullable
		//
		// try to load last revision for deleted entity - main table only ~ subowner will not be solved
		if (revision == null) {
			dto.setDeleted(true);
			try {
				Object lastPersistedVersion = auditService.findLastPersistedVersion(Class.forName(dto.getType()), entityId);
				if (lastPersistedVersion != null) {
					dto.getEmbedded().put(
							IdmAudit_.entityId.getName(), 
							getLookupService().toDto((BaseEntity) lastPersistedVersion, null, context)
					);
				}
			} catch (IllegalArgumentException | ClassNotFoundException ex) {
				LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", dto.getType(), ex);
			} catch (Exception ex) {
				LOG.debug("Type [{}] cannot be mapped to dto.", dto.getType(), ex);
			}
		}
		//
		// For subowner, some entities doesn't support owner and subowner.
		if (dto.getSubOwnerId() != null) {
			try {
				UUID subOwnerId = UUID.fromString(dto.getSubOwnerId());
				if (!loadedDtos.containsKey(subOwnerId)) {
					loadedDtos.put(subOwnerId, getLookupService().lookupDto(dto.getSubOwnerType(), subOwnerId));
				}
				dto.getEmbedded().put(IdmAudit_.subOwnerId.getName(), loadedDtos.get(subOwnerId));
			} catch (IllegalArgumentException ex) {
				LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", dto.getSubOwnerType(), ex);
			} catch (Exception ex) {
				LOG.debug("Type [{}] cannot be mapped to dto.", dto.getSubOwnerId(), ex);
			}
		}
		// For owner, some entities doesn't support owner and subowner.
		if (dto.getOwnerId() != null) {
			try {
				UUID ownerId = UUID.fromString(dto.getOwnerId());
				if (!loadedDtos.containsKey(ownerId)) {
					loadedDtos.put(ownerId, getLookupService().lookupDto(dto.getOwnerType(), ownerId));
				}
				dto.getEmbedded().put(IdmAudit_.ownerId.getName(), loadedDtos.get(ownerId));
			} catch (IllegalArgumentException ex) {
				LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", dto.getSubOwnerType(), ex);
			} catch (Exception ex) {
				LOG.debug("Entity [{}] cannot be mapped to dto.", dto.getOwnerId(), ex);
			}
		}
		// Fill embedded contract for FE agenda (prevent to load contract for each row).
		if ((dto instanceof IdmAuditEntityDto) && dto.getType().equals(IdmIdentityRole.class.getCanonicalName())) {
			IdmAuditEntityDto auditEntity = (IdmAuditEntityDto) dto;
			if (auditEntity.getEntity().containsKey(IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT)
					&& !auditEntity.getEmbedded().containsKey(IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT)) {
				UUID contractId = DtoUtils.toUuid(auditEntity.getEntity().get(IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT));
				if (contractId != null) {
					if (!loadedDtos.containsKey(contractId)) {
						loadedDtos.put(contractId, getLookupService().lookupDto(IdmIdentityContractDto.class, contractId));
					}
					auditEntity.getEmbedded().put(IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT, loadedDtos.get(contractId));
				}				
			}
		}
	}

	@Override
	protected IdmAuditFilter toFilter(MultiValueMap<String, Object> parameters) {
		// We must check if map contains list of changed attributes, because mapped doesn't works with list and zero values.
		List<String> changedAttributesList = null;
		if (parameters.containsKey("changedAttributesList")) {
			List<Object> remove = parameters.remove("changedAttributesList");
			changedAttributesList = remove.stream().map(o -> Objects.toString(o.toString())).collect(Collectors.toList());
		}
		// entity id decorator
		String entityId = getParameterConverter().toString(parameters, "entityId");
		List<String> entityTypes = getParameterConverter().toStrings(parameters, "type");
		UUID entityUuid = null;
		if (CollectionUtils.isNotEmpty(entityTypes) && StringUtils.isNotEmpty(entityId)) {
			// try to find entity by Codeable identifier
			String entityType = entityTypes.get(0);
			AbstractDto entity = getLookupService().lookupDto(entityType, entityId);
			if (entity != null) {
				entityUuid = entity.getId();
				parameters.remove("entityId");
			} else {
				LOG.debug("Entity type [{}] with identifier [{}] does not found, raw entityId will be used as uuid.", 
						entityType, entityId);
				// Better exception for FE.
				try {
					DtoUtils.toUuid(entityId);
				} catch (ClassCastException ex) {
					throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", entityId), ex);
				}
			}
		}
		IdmAuditFilter filter = super.toFilter(parameters);
		filter.setTypes(entityTypes);
		filter.setChangedAttributesList(changedAttributesList);
		if (entityUuid != null) {
			filter.setEntityId(entityUuid);
		}
		//
		return filter;
	}
}
