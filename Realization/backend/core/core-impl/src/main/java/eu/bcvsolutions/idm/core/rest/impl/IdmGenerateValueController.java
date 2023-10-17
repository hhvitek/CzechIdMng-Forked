package eu.bcvsolutions.idm.core.rest.impl;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.ValueGeneratorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmGenerateValueFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmGenerateValueService;
import eu.bcvsolutions.idm.core.api.service.ValueGeneratorManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Generate values controller
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/generate-values")
@Tag(
		name = IdmGenerateValueController.TAG, 
		description = "Operations with generate values"//,

		//produces = BaseController.APPLICATION_HAL_JSON_VALUE

//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmGenerateValueController extends AbstractReadWriteDtoController<IdmGenerateValueDto, IdmGenerateValueFilter> {

	protected static final String TAG = "Generate values";

	private final ValueGeneratorManager valueGeneratorManager;

	@Autowired
	public IdmGenerateValueController(IdmGenerateValueService entityService, ValueGeneratorManager valueGeneratorManager) {
		super(entityService);
		//
		Assert.notNull(valueGeneratorManager, "Manager is required.");
		//
		this.valueGeneratorManager = valueGeneratorManager;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATE_VALUE_READ + "')")
	@Operation(
			summary = "Search generate values (/search/quick alias)", 
			/* nickname = "searchGenerateValues", */ 
			tags = { IdmGenerateValueController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATE_VALUE_READ + "')")
	@Operation(
			summary = "Search generate values", 
			/* nickname = "searchQuickGenerateValues", */ 
			tags = { IdmGenerateValueController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATE_VALUE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countGenerateValues", */ 
			tags = { IdmGenerateValueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATE_VALUE_READ + "')")
	@Operation(
			summary = "Generate value detail", 
			/* nickname = "getGenerateValues", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmGenerateValueDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmGenerateValueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_READ })
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "Generate value uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATE_VALUE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.GENERATE_VALUE_UPDATE + "')")
	@Operation(
			summary = "Create / update generate value", 
			/* nickname = "postGenerateValue", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmGenerateValueDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmGenerateValueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_CREATE,
						CoreGroupPermission.GENERATE_VALUE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_CREATE,
						CoreGroupPermission.GENERATE_VALUE_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmGenerateValueDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATE_VALUE_UPDATE + "')")
	@Operation(
			summary = "Update generate value", 
			/* nickname = "putGenerateValues", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmGenerateValueDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmGenerateValueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			 @Parameter(description = "Generate value uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmGenerateValueDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATE_VALUE_DELETE + "')")
	@Operation(
			summary = "Delete generate value", 
			/* nickname = "deleteGenerateValue", */ 
			tags = { IdmGenerateValueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			 @Parameter(description = "Generate value uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATE_VALUE_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnGenerateValue", */ 
			tags = { IdmGenerateValueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_READ })
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "Generate value uuid.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	/**
	 * Returns all registered entities for generate values
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported-types")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATE_VALUE_READ + "')")
	@Operation(
			summary = "Get all supported dto types", 
			/* nickname = "getSupportedTypes", */ 
			tags = { IdmGenerateValueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_READ })
        }
    )
	public ResponseEntity<?> getSupportedTypes() {
		return new ResponseEntity<>(toCollectionModel(valueGeneratorManager.getSupportedTypes(), null), HttpStatus.OK);
	}
	
	/**
	 * Returns all registered and enabled generators
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/available-generators")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.GENERATE_VALUE_READ + "')")
	@Operation(
			summary = "Get all supported generator", 
			/* nickname = "getGenerators", */ 
			tags = { IdmAuthorizationPolicyController.TAG }, 
						description = "Returns all registered and enabled generators.")
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.GENERATE_VALUE_READ })
        }
    )
	public CollectionModel<ValueGeneratorDto> getAvailableGenerators() {
		return new CollectionModel<>(valueGeneratorManager.getAvailableGenerators(null));
	}
	
	@Override
	public void deleteDto(IdmGenerateValueDto dto) {
		// generator flagged as system can't be deleted from controller
		if (dto.isUnmodifiable()) {
			throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_DELETE_FAILED, ImmutableMap.of("record", dto.getGeneratorType()));
		}
		super.deleteDto(dto);
	}
	
	@Override
	protected IdmGenerateValueDto validateDto(IdmGenerateValueDto dto) {
		if (dto.getId() == null || getService().isNew(dto)) {
			return super.validateDto(dto);
		}
		//
		IdmGenerateValueDto previousDto = getDto(dto.getId());
		if (previousDto != null && previousDto.isUnmodifiable()) {
			// check explicit attributes that can't be changed
			if (previousDto.getSeq() != dto.getSeq()) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "seq", "class", dto.getClass().getSimpleName()));
			}
			if (!previousDto.getDtoType().equals(dto.getDtoType())) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "dtoType", "class", dto.getClass().getSimpleName()));
			}
			if (!previousDto.getGeneratorType().equals(dto.getGeneratorType())) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "getGeneratorType", "class", dto.getClass().getSimpleName()));
			}
			if (previousDto.isUnmodifiable() != dto.isUnmodifiable()) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "unmodifiable", "class", dto.getClass().getSimpleName()));
			}
		}
		//
		return super.validateDto(dto);
	}
}
