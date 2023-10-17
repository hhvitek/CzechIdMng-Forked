package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.SystemEntityTypeRegistrableDto;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityTypeManager;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/system-entity-types")
@Tag(
		name = SysSystemEntityTypeController.TAG,
		description = "System entity types"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class SysSystemEntityTypeController implements BaseController {

	protected static final String TAG = "System entity types";
	
	@Autowired
	private SysSystemEntityTypeManager systemEntityTypeManager;
	
	@ResponseBody
	@GetMapping(value = "/search/supported")
	@Operation(
        summary = "Get supported system entity types",
        /* nickname = "getSupportedSystemEntityTypes", */
        responses = @ApiResponse(
                responseCode = "200",
                content = {
                        @Content(
                                mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                schema = @Schema(
                                        implementation = SystemEntityTypeRegistrableDto.class
                                )
                        )
                }
        )
    )
	public PagedModel<SystemEntityTypeRegistrableDto> getSupportedEntityTypes() {
		List<SystemEntityTypeRegistrableDto> systemEntityTypes = systemEntityTypeManager.getSupportedEntityTypes();
		return new PagedModel<>(systemEntityTypes,
				new PageMetadata(systemEntityTypes.size(), 0, systemEntityTypes.size(), 1));
	}

	@ResponseBody
	@GetMapping(value = "/{backendId}")
	@Operation(
			summary = "System entity type detail",
			/* nickname = "getSupportedSystemEntityType", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SystemEntityTypeRegistrableDto.class
                                    )
                            )
                    }
            ),
			tags = { SysSystemEntityTypeController.TAG })
	public ResponseEntity<?> get(
			 @Parameter(description = "System entity type code.", required = true)
			@PathVariable @NotNull String backendId) {
		SystemEntityTypeRegistrableDto systemEntityType = systemEntityTypeManager.getSystemEntityDtoByCode(backendId);
		return new ResponseEntity<>(toModel(systemEntityType), HttpStatus.OK);
	}

	@ResponseBody
	@GetMapping(value = "/{backendId}/{systemMappingId}")
	@Operation(
			summary = "System entity type detail by mapping",
			/* nickname = "getSupportedSystemEntityTypeByMapping", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SystemEntityTypeRegistrableDto.class
                                    )
                            )
                    }
            ), 
			tags = { SysSystemEntityTypeController.TAG })
	public ResponseEntity<?> get(
			 @Parameter(description = "System entity type code.", required = true)
			@PathVariable @NotNull String backendId,
			 @Parameter(description = "System mapping id", required = true)
			@PathVariable @NotNull String systemMappingId) {
		SystemEntityTypeRegistrableDto systemEntityType = systemEntityTypeManager.getSystemEntityDtoByCode(backendId, systemMappingId);
		return new ResponseEntity<>(toModel(systemEntityType), HttpStatus.OK);
	}
	
	/**
	 * Converts DTO to ResourceSupport
	 * 
	 * @param dto
	 * @return
	 */
	public RepresentationModel toModel(SystemEntityTypeRegistrableDto dto) {
		if (dto == null) { 
			return null;
		} 
		Link selfLink = WebMvcLinkBuilder.linkTo(this.getClass()).slash(dto.getId()).withSelfRel();
		EntityModel<SystemEntityTypeRegistrableDto> resourceSupport = new EntityModel<SystemEntityTypeRegistrableDto>(dto, selfLink);
		//
		return resourceSupport;
	}
}
