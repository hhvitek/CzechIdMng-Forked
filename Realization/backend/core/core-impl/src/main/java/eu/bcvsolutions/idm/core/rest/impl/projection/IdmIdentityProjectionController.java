package eu.bcvsolutions.idm.core.rest.impl.projection;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.projection.IdmIdentityProjectionDto;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.event.IdentityProjectionEvent;
import eu.bcvsolutions.idm.core.eav.api.event.IdentityProjectionEvent.IdentityProjectionEventType;
import eu.bcvsolutions.idm.core.eav.api.service.IdentityProjectionManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Projection controller - get & post is supported only.
 * 
 * TODO: generate (password generator?) / validate endpoint + support
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/identity-projection")
@Tag(
		name = IdmIdentityProjectionController.TAG,  
		 
		description = "Operations with identity projection"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmIdentityProjectionController implements BaseDtoController<IdmIdentityProjectionDto> {

	protected static final String TAG = "Identity projection";
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdentityProjectionManager identityProjectionManager;

	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Identity projection detail", 
			/* nickname = "getIdentityProjection", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmIdentityProjectionDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmIdentityProjectionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.IDENTITY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.IDENTITY_READ })
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmIdentityProjectionDto dto = getDto(backendId);
		if (dto == null) {
			throw new EntityNotFoundException(identityService.getEntityClass(), backendId);
		}
		RepresentationModel resource = toModel(dto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		//
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')")
	@Operation(
			summary = "Create / update identity projection", 
			/* nickname = "postIdentity", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmIdentityProjectionDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmIdentityProjectionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.IDENTITY_CREATE,
						CoreGroupPermission.IDENTITY_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.IDENTITY_CREATE,
						CoreGroupPermission.IDENTITY_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmIdentityProjectionDto dto) {
		RepresentationModel resource = toModel(postDto(dto));
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(resource, HttpStatus.CREATED);
	}
	
	protected IdmIdentityProjectionDto getDto(Serializable backendId) {
		return identityProjectionManager.get(backendId, IdmBasePermission.READ);
	}
	
	protected IdmIdentityProjectionDto postDto(IdmIdentityProjectionDto dto) {
		boolean isNew = identityService.isNew(dto.getIdentity());
		IdentityProjectionEvent event;
		// create
		if (isNew) {
			event = new IdentityProjectionEvent(IdentityProjectionEventType.CREATE, dto);
		} else {
			// update
			event = new IdentityProjectionEvent(IdentityProjectionEventType.UPDATE, dto);
		}
		event.setPriority(PriorityType.HIGH);
		//
		dto = identityProjectionManager.publish(event, isNew ? IdmBasePermission.CREATE : IdmBasePermission.UPDATE).getContent();
		// => load eav and permission is needed
		return getDto(dto);
	}
	
	protected RepresentationModel toModel(IdmIdentityProjectionDto dto) {
		if(dto == null) { 
			return null;
		} 
		Link selfLink = WebMvcLinkBuilder.linkTo(this.getClass()).slash(dto.getId()).withSelfRel();
		//
		return new EntityModel<IdmIdentityProjectionDto>(dto, selfLink);
	}
}
