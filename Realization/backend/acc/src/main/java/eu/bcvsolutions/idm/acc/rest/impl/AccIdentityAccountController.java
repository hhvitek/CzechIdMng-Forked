package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
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
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * Identity accounts on target system
 * 
 * @author Svanda
 * @author Radek Tomiška
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/identity-accounts")
@Tag(name = AccIdentityAccountController.TAG, description = "Assigned accounts on target system")
public class AccIdentityAccountController extends AbstractReadWriteDtoController<AccIdentityAccountDto, AccIdentityAccountFilter> {
	
	protected static final String TAG = "Identity accounts";
	
	@Autowired
	public AccIdentityAccountController(AccIdentityAccountService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_READ + "')")
	@Operation(
			summary = "Search identity accounts (/search/quick alias)",
			operationId = "searchIdentityAccounts",
			tags = { AccIdentityAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.IDENTITY_ACCOUNT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.IDENTITY_ACCOUNT_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_READ + "')")
	@Operation(
			summary = "Search identity accounts",
			operationId = "searchQuickIdentityAccounts",
			tags = { AccIdentityAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.IDENTITY_ACCOUNT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.IDENTITY_ACCOUNT_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_READ + "')")
	@Operation(
			summary = "Identity account detail",
			operationId = "getIdentityAccount",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AccIdentityAccountDto.class
                                    )
                            )
                    }
            ), 
			tags = { AccIdentityAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.IDENTITY_ACCOUNT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.IDENTITY_ACCOUNT_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Identity account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_UPDATE + "')")
	@Operation(
			summary = "Create / update identity account",
			operationId = "postIdentity",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AccIdentityAccountDto.class
                                    )
                            )
                    }
            ), 
			tags = { AccIdentityAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.IDENTITY_ACCOUNT_CREATE,
						AccGroupPermission.IDENTITY_ACCOUNT_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.IDENTITY_ACCOUNT_CREATE,
						AccGroupPermission.IDENTITY_ACCOUNT_UPDATE})
        }
    )
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> post(@RequestBody @NotNull AccIdentityAccountDto dto){
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@Operation(
			summary = "Update identity account",
			operationId = "putIdentityAccount",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AccIdentityAccountDto.class
                                    )
                            )
                    }
            ), 
			tags = { AccIdentityAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.IDENTITY_ACCOUNT_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.IDENTITY_ACCOUNT_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Identity account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccIdentityAccountDto dto){
		return super.put(backendId,dto);
	}	
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete identity account",
			operationId = "deleteIdentityAccount",
			tags = { AccIdentityAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.IDENTITY_ACCOUNT_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.IDENTITY_ACCOUNT_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Identity account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.IDENTITY_ACCOUNT_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			operationId = "getPermissionsOnIdentityAccount",
			tags = { AccIdentityAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.IDENTITY_ACCOUNT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.IDENTITY_ACCOUNT_READ })
    })
	public Set<String> getPermissions(
			 @Parameter(description = "Identity account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	protected AccIdentityAccountFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setAccountId(getParameterConverter().toUuid(parameters, "accountId"));
		filter.setIdentityId(getParameterConverter().toEntityUuid(parameters, AccIdentityAccountFilter.PARAMETER_IDENTITY_ID, IdmIdentityDto.class));
		filter.setRoleId(getParameterConverter().toUuid(parameters, "roleId"));
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		filter.setOwnership(getParameterConverter().toBoolean(parameters, "ownership"));
		return filter;
	}
}
