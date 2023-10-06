package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordSystemService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;


/**
 * Rest controller for standard CRUD operation for uniform password connection between
 * {@link AccUniformPasswordDto} and {@link SysSystemDto}.
 * Controller has same permission as controller {@link AccUniformPasswordController}
 * 
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/uniform-password-systems")
@Tag(
		name = AccUniformPasswordSystemController.TAG, 
		//tags = AccUniformPasswordSystemController.TAG,
		description = "Uniform password conection with systems"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class AccUniformPasswordSystemController extends AbstractReadWriteDtoController<AccUniformPasswordSystemDto, AccUniformPasswordSystemFilter> {
	
	protected static final String TAG = "Uniform password systems";
	
	@Autowired
	public AccUniformPasswordSystemController(AccUniformPasswordSystemService uniformPasswordSystemService) {
		super(uniformPasswordSystemService);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@Operation(
			summary = "Search definition for uniform password and system (/search/quick alias)"
			/*, nickname = "searchUniformPasswordSystems", */)
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            AccGroupPermission.UNIFORM_PASSWORD_READ }),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            AccGroupPermission.UNIFORM_PASSWORD_READ })
            }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search uniform password system",
			/* nickname = "searchQuickUniformPasswordSystems", */
			tags = { AccUniformPasswordSystemController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete uniform password systems (selectbox usage)",
			/* nickname = "autocompleteUniformPasswordSystems", */ 
			tags = { AccUniformPasswordSystemController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "Uniform password system detail",
			/* nickname = "getUniformPasswordSystem", */ 
			/* response = AccUniformPasswordSystemDto.class, */ 
			tags = { AccUniformPasswordSystemController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Uniform password system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@Operation(
			summary = "Create / update uniform password system",
			/* nickname = "postUniformPasswordSystem", */ 
			/* response = AccUniformPasswordSystemDto.class, */ 
			tags = { AccUniformPasswordSystemController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_CREATE,
						AccGroupPermission.UNIFORM_PASSWORD_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_CREATE,
						AccGroupPermission.UNIFORM_PASSWORD_UPDATE})
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull AccUniformPasswordSystemDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@Operation(
			summary = "Update uniform password system",
			/* nickname = "putUniformPassword", */ 
			/* response = AccUniformPasswordSystemDto.class, */ 
			tags = { AccUniformPasswordSystemController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "Uniform password system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccUniformPasswordSystemDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_UPDATE + "')")
	@Operation(
			summary = "Update uniform password system",
			/* nickname = "patchUniformPasswordSystem", */ 
			/* response = AccUniformPasswordSystemDto.class, */ 
			tags = { AccUniformPasswordSystemController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_UPDATE })
        }
    )
	public ResponseEntity<?> patch(
			@Parameter(name = "Uniform password system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete uniform password system",
			/* nickname = "deleteUniformPassword", */ 
			tags = { AccUniformPasswordSystemController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Uniform password system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			/* nickname = "getPermissionsOnUniformPasswordSystem", */ 
			tags = { AccUniformPasswordSystemController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.UNIFORM_PASSWORD_READ })
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Uniform password system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	
	@Override
	protected AccUniformPasswordSystemFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccUniformPasswordSystemFilter filter = new AccUniformPasswordSystemFilter(parameters, getParameterConverter());
		return filter;
	}
}
