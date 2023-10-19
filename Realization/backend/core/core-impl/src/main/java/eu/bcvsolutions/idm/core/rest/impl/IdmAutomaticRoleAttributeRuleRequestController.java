package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.AbstractRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleRequestFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleRequestService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Automatic role rule request endpoint search all entities are available for roleRequestId
 * in filter has to be filled.
 * 
 * @author svandav
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/automatic-role-rule-requests")
@Tag(name = IdmAutomaticRoleAttributeRuleRequestController.TAG, description = "Operations with single roles in request")
public class IdmAutomaticRoleAttributeRuleRequestController extends
		AbstractReadWriteDtoController<IdmAutomaticRoleAttributeRuleRequestDto, IdmAutomaticRoleAttributeRuleRequestFilter> {

	protected static final String TAG = "Automatic role rule request";
	private final IdmAutomaticRoleRequestService automaticRoleRequestService;

	@Autowired
	public IdmAutomaticRoleAttributeRuleRequestController(IdmAutomaticRoleAttributeRuleRequestService service,
			IdmAutomaticRoleRequestService automaticRoleRequestService) {
		super(service);
		//
		Assert.notNull(automaticRoleRequestService, "Service is required.");
		//
		this.automaticRoleRequestService = automaticRoleRequestService;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ + "')")
	@Operation(summary = "Search rule request role requests (/search/quick alias)", operationId = "searchRule requestRoleRequests", tags = {
			IdmAutomaticRoleAttributeRuleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ,
							CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_ADMIN }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ,
							CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_ADMIN })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> find(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ + "')")
	@Operation(summary = "Search rule request role requests", operationId = "searchQuickRule requestRoleRequests", tags = {
			IdmAutomaticRoleAttributeRuleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ,
							CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_ADMIN }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ,
							CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_ADMIN })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}

	@Override
	public Page<IdmAutomaticRoleAttributeRuleRequestDto> find(IdmAutomaticRoleAttributeRuleRequestFilter filter,
			Pageable pageable, BasePermission permission) {
		// Check access
		// Beware, if has filter requestId filled, then we check permission via right on
		// the request.
		if (filter == null || filter.getRoleRequestId() == null) {
			return super.find(filter, pageable, permission);
		}
		AbstractRequestDto roleRequest = automaticRoleRequestService.get(filter.getRoleRequestId(), permission);
		if (roleRequest == null) {
			// return empty result (find method doesn't throw 404)
			return new PageImpl<>(new ArrayList<>());
		} else {
			return super.find(filter, pageable, null);
		}
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ + "')")
	@Operation(summary = "Rule request detail", operationId = "getRule requestRoleRequest",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmAutomaticRoleAttributeRuleRequestDto.class
                                    )
                            )
                    }
            ), tags = {	IdmAutomaticRoleAttributeRuleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Rule request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_CREATE + "')" + " or hasAuthority('"
			+ CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE + "')")
	@Operation(summary = "Create / update rule request", operationId = "postRule requestRoleRequest",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmAutomaticRoleAttributeRuleRequestDto.class
                                    )
                            )
                    }
            ), tags = {	IdmAutomaticRoleAttributeRuleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_CREATE,
							CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_CREATE,
							CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE })
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull IdmAutomaticRoleAttributeRuleRequestDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE + "')")
	@Operation(summary = "Update rule request", operationId = "putRule requestRoleRequest",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmAutomaticRoleAttributeRuleRequestDto.class
                                    )
                            )
                    }
            ), tags = {	IdmAutomaticRoleAttributeRuleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Rule request's uuid identifier.", required = true) @PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmAutomaticRoleAttributeRuleRequestDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_DELETE + "')")
	@Operation(summary = "Delete rule request", operationId = "delete Rule requestRoleRequest", tags = {
			IdmAutomaticRoleAttributeRuleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Rule request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ + "')")
	@Operation(summary = "What logged identity can do with given record", operationId = "getPermissionsOnAutomaticRoleRuleRequest", tags = {
			IdmAutomaticRoleAttributeRuleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.AUTOMATIC_ROLE_REQUEST_READ })
    })
	public Set<String> getPermissions(
			 @Parameter(description = "Rule request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@Override
	protected IdmAutomaticRoleAttributeRuleRequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmAutomaticRoleAttributeRuleRequestFilter filter = new IdmAutomaticRoleAttributeRuleRequestFilter(parameters);
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setRoleRequestId(getParameterConverter().toUuid(parameters, "roleRequestId"));
		filter.setRoleId(getParameterConverter().toUuid(parameters, "roleId"));
		filter.setAutomaticRoleId(getParameterConverter().toUuid(parameters, "automaticRole"));
		return filter;
	}
}
