package eu.bcvsolutions.idm.core.rest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.RequestManager;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.rest.impl.IdmRequestController;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * CRUD operations for requests
 * 
 * @author svandav
 */
public abstract class AbstractRequestDtoController<DTO extends Requestable, F extends BaseFilter>
		extends AbstractReadWriteDtoController<DTO, F> {

	@Autowired
	private RequestManager requestManager;

	public AbstractRequestDtoController(ReadWriteDtoService<DTO, F> entityService) {
		super(entityService);
	}
	
	public DTO getDto(String requestId, String backendId) {
		return (DTO) requestManager.get(UUID.fromString(requestId), UUID.fromString(backendId),
				getService().getDtoClass(), IdmBasePermission.READ);
	}

	/**
	 * Post DTO and convert to response
	 * 
	 * @param dto
	 * @param requestId
	 * @return
	 */
	@Operation(summary = "Create / update record")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	public ResponseEntity<?> post( @Parameter(description = "Request ID", required = true) String requestId, //
			 @Parameter(description = "Record (dto).", required = true) DTO dto) { //
		Requestable resultDto = requestManager.post(requestId, dto, IdmBasePermission.CREATE);
		@SuppressWarnings("unchecked")
		RepresentationModel resource = toModel(requestId, (DTO) resultDto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(resource, HttpStatus.CREATED);
	}

	/**
	 * Update DTO by given backendId and convert to response
	 * 
	 * @param requestId
	 * @param backendId
	 * @param dto
	 * @return
	 */
	@Operation(summary = "Update record")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	public ResponseEntity<?> put( //
			 @Parameter(description = "Request ID", required = true) String requestId, //
			 @Parameter(description = "Record's uuid identifier or unique code", required = true) String backendId, //
			 @Parameter(description = "Record (dto).", required = true) DTO dto) { //
		DTO updatedDto = getDto(requestId, backendId);
		if (updatedDto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}

		Requestable resultDto = requestManager.post(requestId, dto, IdmBasePermission.UPDATE);
		@SuppressWarnings("unchecked")
		RepresentationModel resource = toModel(requestId, (DTO) resultDto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	/**
	 * Deletes DTO by given id
	 * 
	 * @param requestId
	 * @param backendId
	 * @return
	 */
	@Operation(summary = "Delete record")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	public ResponseEntity<?> delete( @Parameter(description = "Request ID", required = true) String requestId, //
			 @Parameter(description = "Record's uuid identifier or unique code.", required = true) String backendId) { //
		DTO dto = getDto(requestId, backendId);
		if (dto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		Requestable resultDto = requestManager.delete(requestId, dto, IdmBasePermission.DELETE);
		@SuppressWarnings("unchecked")
		RepresentationModel resource = toModel(requestId, (DTO) resultDto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	/**
	 * Returns response DTO by given backendId
	 * 
	 * @param backendId
	 * @param requestId
	 * @return
	 */
	@Operation(summary = "Read record")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	public ResponseEntity<?> get(@PathVariable @NotNull String requestId,
			 @Parameter(description = "Record's uuid identifier or unique code, if record supports Codeable interface.", required = true) //
			@PathVariable @NotNull String backendId) { //

		DTO dto = getDto(requestId, backendId);
		if (dto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		
		RepresentationModel resource = toModel(requestId, dto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		//
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	@Operation(summary = "Create request for DTO")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	public ResponseEntity<?> createRequest( @Parameter(description = "Record (dto).", required = true) DTO dto) {
		IdmRequestDto request = requestManager.createRequest(dto, IdmBasePermission.CREATE);
		Link selfLink = WebMvcLinkBuilder.linkTo(IdmRequestController.class).slash(request.getId()).withSelfRel();
		EntityModel<IdmRequestDto> resource = new EntityModel<IdmRequestDto>(request, selfLink);
		return new ResponseEntity<>(resource, HttpStatus.CREATED);
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
	public CollectionModel<?> find(
			 @Parameter(description = "Request ID", required = true) String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		Page<DTO> page = (Page<DTO>) requestManager.find(getDtoClass(), requestId, toFilter(parameters), pageable,
				IdmBasePermission.READ);

		return toCollectionModel(page, getDtoClass());
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
	public CollectionModel<?> findQuick( @Parameter(description = "Request ID", required = true) String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return find(requestId, parameters, pageable);
	}

	/**
	 * Quick search for autocomplete (read data to select box etc.) - parameters
	 * will be transformed to filter object
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
			 @Parameter(description = "Request ID", required = true) String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		Page<DTO> page = (Page<DTO>) requestManager.find(getDtoClass(), requestId, toFilter(parameters), pageable,
				IdmBasePermission.AUTOCOMPLETE);
		return toCollectionModel(page, getDtoClass());
	}

	/**
	 * Returns, what currently logged identity can do with given DTO
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
			 @Parameter(description = "Request ID", required = true) String requestId,
			 @Parameter(description = "Record's uuid identifier or unique code, if record supports Codeable interface.", required = true)
			@PathVariable @NotNull String backendId) {
		DTO dto = getDto(requestId, backendId);
		if (dto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		return getService().getPermissions((DTO) dto);
	}

	// TODO: Support of count !
	/**
	 * The number of entities that match the filter - parameters will be transformed
	 * to filter object
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
	public long count( @Parameter(description = "Request ID", required = true) String requestId,
			@RequestParam(required = false)
			MultiValueMap<String, Object> parameters) {
		return count(toFilter(parameters), IdmBasePermission.COUNT);
	}
	
	public EntityModel<?> saveFormValues(String requestId, DTO dto, IdmFormDefinitionDto formDefinition,
			List<IdmFormValueDto> formValues, BasePermission... permission) {
		Assert.notNull(dto, "DTO is required.");
		Assert.notNull(requestId, "Request identifier is required.");

		IdmFormInstanceDto formInstance = requestManager.saveFormInstance(UUID.fromString(requestId), dto,
				formDefinition, formValues, permission);
		return new EntityModel<>(formInstance);
	}

	public EntityModel<IdmFormInstanceDto> getFormValues(String requestId, Identifiable owner,
			IdmFormDefinitionDto formDefinition, BasePermission... permission) {
		Assert.notNull(owner, "Owner is required.");
		Assert.notNull(requestId, "Request identifier is required.");

		@SuppressWarnings("unchecked")
		IdmFormInstanceDto formInstance = requestManager.getFormInstance(UUID.fromString(requestId), (DTO) owner,
				formDefinition, permission);
		return new EntityModel<>(formInstance);
	}
	
	/**
	 * Converts DTO to ResourceSupport
	 * 
	 * @param dto
	 * @return
	 */
	protected RepresentationModel toModel(String requestId, DTO dto) {
		if (dto == null) {
			return null;
		}
		Link selfLink = WebMvcLinkBuilder.linkTo(this.getClass()) //
				.slash(requestId) //
				.slash(this.getRequestSubPath()) //
				.slash(dto.getId()).withSelfRel(); //
		EntityModel<DTO> resourceSupport = new EntityModel<DTO>(dto, selfLink);
		return resourceSupport;
	}

	public abstract String getRequestSubPath();
	
	
	/**
	 * Request controllers are every allowed 
	 */
	@Override
	protected boolean isRequestModeEnabled() {
		return false;
	}
}
