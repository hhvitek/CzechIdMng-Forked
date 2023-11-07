package eu.bcvsolutions.idm.core.api.rest;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.PermissionContext;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.FilterConverter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Read operations (get, find, autocomplete)
 * 
 * @param <DTO> dto type
 * @param <F> filter type - {@link DataFilter} is preferred.
 * @author Svanda
 * @author Radek Tomiška
 * @author Tomáš Doischer
 */
public abstract class AbstractReadDtoController<DTO extends BaseDto, F extends BaseFilter>
		implements BaseDtoController<DTO> {
	
	@Autowired private PagedResourcesAssembler<Object> pagedResourcesAssembler;
	@Autowired private ObjectMapper objectMapper;
	@Autowired private LookupService lookupService;
	@Autowired private BulkActionManager bulkActionManager;

	@Autowired(required = false) private List<PluggableFilterTranslator<F>> translators;
	//
	private FilterConverter filterConverter;
	private final ReadDtoService<DTO, F> service;

	public AbstractReadDtoController(ReadDtoService<DTO, F> service) {
		this.service = service;
	}

	/**
	 * Returns DTO service configured to current controller
	 * 
	 * @return
	 */
	protected ReadDtoService<DTO, F> getService() {
		Assert.notNull(service, "Service is required!");
		//
		return service;
	}

	/**
	 * Returns controlled DTO class.
	 * 
	 * @return
	 */
	protected Class<DTO> getDtoClass() {
		return getService().getDtoClass();
	}
	
	/**
	 * Returns controlled {@link BaseFilter} type class.
	 * 
	 * @return
	 */
	protected Class<F> getFilterClass() {
		return getService().getFilterClass();
	}

	/**
	 * Returns response DTO by given backendId
	 * 
	 * @param backendId
	 * @return
	 */
	@Operation(summary = "Read record")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
            @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
        }
    )
    public ResponseEntity<?> get(
			 @Parameter(description = "Record's uuid identifier or unique code, if record supports Codeable interface.", required = true)
			@PathVariable @NotNull String backendId) {
		DTO dto = getDto(backendId);
		if (dto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		RepresentationModel resource = toModel(dto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		//
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	/**
	 * Returns DTO by given backendId
	 * 
	 * @param backendId
	 * @return
	 */
	public DTO getDto(Serializable backendId) {
		DTO dto = null;
		// If service supports context, we need to call service.get method with context/filter.
		if (service.supportsToDtoWithFilter()) {
			// Create context for get method. We expect the logic (setting of the context) in the method toFilter.
			F context = getContext(new LinkedMultiValueMap<>());
			if (backendId instanceof UUID) {
				// BackendId is UUID, we try to load DTO by service.get method (with context).
				dto = service.get((UUID) backendId, context);
			} else {
				try {
					UUID id = DtoUtils.toUuid(backendId);
					// BackendId is UUID, we try to load DTO by service.get method (with context).
					dto = service.get(id, context);
					if (dto == null) {
						// DTO was not found by UUID. Theoretically is UUID not ID, but code (for example).
						// We try to use lookup service now.
						dto = lookupService.lookupDto(getDtoClass(), backendId);
						if (dto != null) {
							// DTO was found by lookup service. Now we need to call service.get with context.
							dto = service.get(dto.getId(), context);
						}
					}
				} catch (ClassCastException ex) {
					// Ok, backendId is not UUID, so we can try to lookupSerivce.
					dto = lookupService.lookupDto(getDtoClass(), backendId);
					if (dto != null) {
						// DTO was found by lookup service. Now we need to call service.get with context.
						dto = service.get(dto.getId(), context);
					}
				}
			}
		} else {
			dto = lookupService.lookupDto(getDtoClass(), backendId);
		}
		return checkAccess(dto, IdmBasePermission.READ);
	}

	/**
	 * Quick search - parameters will be transformed to filter object
	 * 
	 * @param parameters
	 * @param pageable
	 * @return
     * @see #toFilter(MultiValueMap)
	 */
	@Operation(summary = "Search records (/search/quick alias)")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	@PageableAsQueryParam
    //PagedModel<?>
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		BasePermission[] evaluatePermissions = evaluatePermissions(parameters, IdmBasePermission.READ);
		if (evaluatePermissions.length > 1) {
			return toCollectionModel(findWithOperator(toFilter(parameters), pageable, evaluatePermissions), getDtoClass());
		}
		//
		return toCollectionModel(find(toFilter(parameters), pageable, evaluatePermissions[0]), getDtoClass());
	}
	
	/**
	 * All endpoints will support find quick method.
	 * 
	 * @param parameters
	 * @param pageable
	 * @return
	 * @see #toFilter(MultiValueMap)
	 */
	@Operation(summary = "Search records")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
            @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return find(parameters, pageable);
	}
	
	/**
	 * Quick search for autocomplete (read data to select box etc.) - parameters will be transformed to filter object
	 * 
	 * @param parameters
	 * @param pageable
	 * @return
     * @see #toFilter(MultiValueMap)
	 */
	@Operation(summary = "Autocomplete records (selectbox usage)")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
            @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		BasePermission[] evaluatePermissions = evaluatePermissions(parameters, IdmBasePermission.AUTOCOMPLETE);
		if (evaluatePermissions.length > 1) {
			return toCollectionModel(findWithOperator(toFilter(parameters), pageable, evaluatePermissions), getDtoClass());
		}
		//
		return toCollectionModel(find(toFilter(parameters), pageable, evaluatePermissions[0]), getDtoClass());
	}
	
	/**
	 * The number of entities that match the filter - parameters will be transformed to filter object
	 * 
	 * @param parameters
	 * @return
     * @see #toFilter(MultiValueMap)
	 */
	@Operation(summary = "The number of entities that match the filter")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
            @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		BasePermission[] evaluatePermissions = evaluatePermissions(parameters, IdmBasePermission.COUNT);
		if (evaluatePermissions.length > 1) {
			return countWithOperator(toFilter(parameters), evaluatePermissions);
		}
		//
		return count(toFilter(parameters), evaluatePermissions[0]);
	}

	/**
	 * Quick search - finds DTOs by given filter and pageable
	 * 
	 * @param filter
	 * @param pageable
	 * @param permission base permission to evaluate
	 * @return
	 */
	public Page<DTO> find(F filter, Pageable pageable, BasePermission permission) {
		return findWithOperator(filter, pageable, permission);
	}
	
	/**
	 * Quick search - finds DTOs by given filter and pageable
	 * 
	 * @param filter
	 * @param pageable
	 * @param permission base permissions to evaluate (AND / OR by permission operation in filter)
	 * @return
	 * @since 11.1.0
	 */
	protected Page<DTO> findWithOperator(F filter, Pageable pageable, BasePermission... permission) {
		return getService().find(filter, pageable, permission);
	}
	
	/**
	 * The number of entities that match the filter
	 * 
	 * @param filter
	 * @param permission
	 * @return
	 */
	public long count(F filter, BasePermission permission) {
		return countWithOperator(filter, permission);
	}
	
	/**
	 * The number of entities that match the filter.
	 * 
	 * @param filter
	 * @param permission base permissions to evaluate (AND / OR by permission operation in filter)
	 * @return
	 * @since 11.1.0
	 */
	protected long countWithOperator(F filter, BasePermission... permission) {
		return getService().count(filter, permission);
	}
	
	/**
	 * Returns, what currently logged identity can do with given dto
	 * 
	 * @param backendId
	 * @return
	 */
	@Operation(summary = "What logged identity can do with given record")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
            @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "Record's uuid identifier or unique code, if record supports Codeable interface.", required = true)
			@PathVariable @NotNull String backendId) {
		DTO dto = getDto(backendId);
		if (dto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		return getService().getPermissions(dto.getId());
	}

	/**
	 * Converts DTO to ResourceSupport
	 * 
	 * @param dto
	 * @return
	 */
	public RepresentationModel toModel(DTO dto) {
		if (dto == null) { 
			return null;
		} 
		Link selfLink = WebMvcLinkBuilder.linkTo(this.getClass()).slash(dto.getId()).withSelfRel();
		EntityModel<DTO> resourceSupport = new EntityModel<DTO>(dto, selfLink);
		//
		return resourceSupport;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected CollectionModel<?> toCollectionModel(Iterable<?> source, Class<?> domainType) {
		if (source == null) {
			return new CollectionModel(Collections.emptyList());
		}
		Page<Object> page;
		if (source instanceof Page) {
			page = (Page<Object>) source;
		} else {
			// Iterable to Page
			List records = Lists.newArrayList(source);
			page = new PageImpl(records, PageRequest.of(0, records.size() > 0 ? records.size() : 10), records.size());
		}
		return pageToResources(page, domainType);
	}

    @SuppressWarnings("unchecked")
    protected CollectionModel<?> pageToResources(Page<Object> page, Class<?> domainType) {
        Assert.notNull(page, "EntityModel page (content) is required.");
        //
        if (page.getContent().isEmpty()) {
            return pagedResourcesAssembler.toEmptyModel(page, domainType);
        }
        //
        return pagedResourcesAssembler.toModel(page);
    }

	/**
	 * Transforms request parameters to:
	 * - {@link BaseFilter} using object mapper
	 * - {@link DataFilter} using reflection with constructor(parameters).
	 *
	 * @deprecated It is no longer advised to override this method. To supply logic for translating filters, please provide
	 * 				bean of type {@link PluggableFilterTranslator}
	 * @param parameters
	 * @return
	 */
	@Deprecated
	protected F toFilter(MultiValueMap<String, Object> parameters) {
		return paramsToFilter(parameters);
	}

	protected final F paramsToFilter(MultiValueMap<String, Object> parameters) {
		final List<PluggableFilterTranslator<F>> pluggableTranslators = getPluggableTranslators();
		if (pluggableTranslators.isEmpty()) {
			return getParameterConverter().toFilter(parameters, getService().getFilterClass());
		}
		F filter = null;
		for (PluggableFilterTranslator<F> translator : pluggableTranslators) {
			filter = translator.transform(Optional.ofNullable(filter), parameters);
		}
		return filter;
	}

	protected final List<PluggableFilterTranslator<F>> getPluggableTranslators() {
		if (this.translators == null) {
			return Collections.emptyList();
		}
		return translators;
	}
	
	/**
	 * Context for get method.
	 * 
	 * @param parameters additional parameters
	 * @return context
	 * @since 11.0.0
	 */
	protected F getContext(MultiValueMap<String, Object> parameters) {
		return toFilter(parameters);
	}

	/**
	 * Return parameter converter helper
	 * 
	 * @return
	 */
	protected FilterConverter getParameterConverter() {
		if (filterConverter == null) {
			filterConverter = new FilterConverter(lookupService, objectMapper);
		}
		return filterConverter;
	}
	
	protected LookupService getLookupService() {
		return lookupService;
	}
	
	protected ObjectMapper getMapper() {
		return objectMapper;
	}
	
	/**
	 * Evaluates authorization permission on given dto
	 * 
	 * @param dto
	 * @param permission
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	protected DTO checkAccess(DTO dto, BasePermission... permission) {
		return getService().checkAccess(dto, permission);
	}
	
	/**
	 * @deprecated @since 11.1.0 - multiple permissions are supported - use {@link #evaluatePermissions(MultiValueMap, BasePermission)}
	 */
	@Deprecated
	protected BasePermission evaluatePermission(MultiValueMap<String, Object> parameters, BasePermission originalPermission) {
		BasePermission[] evaluatePermissions = evaluatePermissions(parameters, originalPermission);
		//
		return evaluatePermissions[0];
	}
	
	/**
	 * Multiple permissions are supported.
	 * 
	 * @param parameters filter parameters
	 * @param originalPermission default permission
	 * @return evaluate permissions
	 * @since 11.1.0
	 */
	protected BasePermission[] evaluatePermissions(MultiValueMap<String, Object> parameters, BasePermission defaultPermission) {
		// We need to use raw parameters => data filter (~ PermissionContext instance) is not required now.
		List<String> rawPermissions = getParameterConverter().toStrings(parameters, PermissionContext.PARAMETER_EVALUATE_PERMISSION);
    	Collection<BasePermission> permissions = PermissionUtils.toPermissions(rawPermissions);
		//
		return permissions.isEmpty() ? new BasePermission[] { defaultPermission } : permissions.stream().toArray(BasePermission[]::new);
	}
	
	/**
	 * Returns available bulk actions
	 * 
	 * @return
	 */
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return bulkActionManager.getAvailableActions(getService().getEntityClass());
	}
	
	/**
	 * Process bulk action
	 * 
	 * @param bulkAction
	 * @return
	 */
	public ResponseEntity<IdmBulkActionDto> bulkAction(IdmBulkActionDto bulkAction) {
		initBulkAction(bulkAction);
		return new ResponseEntity<IdmBulkActionDto>(bulkActionManager.processAction(bulkAction), HttpStatus.CREATED);
	}
	
	/**
	 * Start prevalidation for given bulk action
	 * @param bulkAction
	 * @return
	 */
	public ResponseEntity<ResultModels> prevalidateBulkAction(IdmBulkActionDto bulkAction) {
		initBulkAction(bulkAction);
		ResultModels result = bulkActionManager.prevalidate(bulkAction);
		if(result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	/**
	 * Start preprocessing for a given bulk action.
	 * 
	 * @param bulkAction
	 * @return
	 * @since 12.1.0
	 */
	public ResponseEntity<IdmBulkActionDto> preprocessBulkAction(IdmBulkActionDto bulkAction) {
		initBulkAction(bulkAction);
		IdmBulkActionDto result = bulkActionManager.preprocessBulkAction(bulkAction);
		if(result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	/**
	 * Init bulk action
	 * @param bulkAction
	 */
	@SuppressWarnings("unchecked")
	protected void initBulkAction(IdmBulkActionDto bulkAction) {
		// TODO: use MultiValueMap in object if is possible?
		if (bulkAction.getFilter() != null) {
			MultiValueMap<String, Object> multivaluedMap = new LinkedMultiValueMap<>();
			Map<String, Object> properties = bulkAction.getFilter();
			
			for (Map.Entry<String, Object> entry : properties.entrySet()) {
				Object value = entry.getValue();
				if (value == null) {
					multivaluedMap.remove(entry.getKey());
				} else if(value instanceof List<?>) {
					multivaluedMap.put(entry.getKey(), (List<Object>) value);
				} else {
					multivaluedMap.add(entry.getKey(), entry.getValue());
				}
			}
			F filter = this.toFilter(multivaluedMap);
			bulkAction.setTransformedFilter(filter);
		}
		bulkAction.setEntityClass(getService().getEntityClass() != null ? getService().getEntityClass().getName() : null);
		bulkAction.setFilterClass(this.getFilterClass() != null ? this.getFilterClass().getName() : null);
	}
}
