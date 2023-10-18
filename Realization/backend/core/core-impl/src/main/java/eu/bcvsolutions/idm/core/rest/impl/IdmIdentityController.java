package eu.bcvsolutions.idm.core.rest.impl;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.audit.service.IdmAuditService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmProfileFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmIncompatibleRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.service.IdmProfileService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.rest.impl.AbstractFormableDtoController;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.WorkPositionDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.service.api.CheckLongPollingResult;
import eu.bcvsolutions.idm.core.model.service.api.LongPollingManager;
import eu.bcvsolutions.idm.core.rest.DeferredResultWrapper;
import eu.bcvsolutions.idm.core.rest.LongPollingSubscriber;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.GrantedAuthoritiesFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Rest methods for IdmIdentity resource
 * 
 * @author Radek Tomiška
 * @author Petr Hanák
 * @author Tomáš Doischer
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/identities") //I have to remove this (username cannot have "@.com" in user name)
@Tag(
		name = IdmIdentityController.TAG,  
		 
		description = "Operations with identities"//,
		


)
public class IdmIdentityController extends AbstractFormableDtoController<IdmIdentityDto, IdmIdentityFilter> {

	protected static final String TAG = "Identities";
	//
	@Autowired private GrantedAuthoritiesFactory grantedAuthoritiesFactory;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmAuditService auditService;
	@Autowired private IdmTreeNodeService treeNodeService;
	@Autowired private IdmProfileService profileService;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private IdmIncompatibleRoleService incompatibleRoleService;
	@Autowired private IdmFormDefinitionController formDefinitionController;
	@Autowired private IdmProfileController profileController;
	@Autowired private FormService formService;
	@Autowired private IdmPasswordService passwordService;
	@Autowired private IdmPasswordController passwordController;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private LongPollingManager longPollingManager;
	//
	private final IdmIdentityService identityService;

	@Autowired
	public IdmIdentityController(IdmIdentityService identityService) {
		super(identityService);
		//
		this.identityService = identityService;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Search identities (/search/quick alias)", 
			/* nickname = "searchIdentities", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_READ })
        }
    )
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Search identities", 
			/* nickname = "searchQuickIdentities", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete identities (selectbox usage)", 
			/* nickname = "autocompleteIdentities", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_AUTOCOMPLETE })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countIdentities", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Identity detail", 
			/* nickname = "getIdentity", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmIdentityDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmIdentityController.TAG })
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
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')")
	@Operation(
			summary = "Create / update identity", 
			/* nickname = "postIdentity", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmIdentityDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmIdentityController.TAG })
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
	public ResponseEntity<?> post(@Valid @RequestBody IdmIdentityDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')")
	@Operation(
			summary = "Update identity", 
			/* nickname = "putIdentity", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmIdentityDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmIdentityDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')")
	@Operation(
			summary = "Update identity", 
			/* nickname = "patchIdentity", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmIdentityDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_UPDATE })
        }
    )
	public ResponseEntity<?> patch(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	/**
	 * @since 7.6.0
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_MANUALLYENABLE + "')")
	@RequestMapping(value = "/{backendId}/enable", method = RequestMethod.PATCH)
	@Operation(
			summary = "Activate identity", 
			/* nickname = "activateIdentity", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmIdentityDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmIdentityController.TAG }, 
						description = "Enable manually disabled identity. Identity will have automatically recounted state assigned by their contract state." )
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.IDENTITY_MANUALLYENABLE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.IDENTITY_MANUALLYENABLE })
        }
    )
	public ResponseEntity<?> enable(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmIdentityDto identity = getDto(backendId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		return new ResponseEntity<>(toModel(identityService.enable(identity.getId(), IdentityBasePermission.MANUALLYENABLE)), HttpStatus.OK);
	}
	
	/**
	 * @since 7.6.0
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_MANUALLYDISABLE + "')")
	@RequestMapping(value = "/{backendId}/disable", method = RequestMethod.PATCH)
	@Operation(
			summary = "Disable identity", 
			/* nickname = "disableIdentity", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmIdentityDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmIdentityController.TAG },
			description = "Disable identity manually. This identity will be disabled even with valid contracts."
					+ " Identity can be enabled manually again only. See 'enable' method." )
    @SecurityRequirements(
        value = {
            @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                    CoreGroupPermission.IDENTITY_MANUALLYDISABLE }),
            @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                    CoreGroupPermission.IDENTITY_MANUALLYDISABLE })
        }
    )
	public ResponseEntity<?> disable(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmIdentityDto identity = getDto(backendId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		return new ResponseEntity<>(toModel(identityService.disable(identity.getId(), IdentityBasePermission.MANUALLYDISABLE)), HttpStatus.OK);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_DELETE + "')")
	@Operation(
			summary = "Delete identity", 
			/* nickname = "deleteIdentity", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.IDENTITY_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnIdentity", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.IDENTITY_READ,
						CoreGroupPermission.IDENTITY_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.IDENTITY_READ,
						CoreGroupPermission.IDENTITY_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	/**
	 * Get available bulk actions for identity
	 *
	 * @return
	 */
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Get available bulk actions", 
			/* nickname = "availableBulkAction", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_READ })
        }
    )
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	/**
	 * Process bulk action for identities
	 *
	 * @param bulkAction
	 * @return
	 */
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Process bulk action for identity", 
			/* nickname = "bulkAction", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmBulkActionDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	/**
	 * Prevalidate bulk action for identities
	 *
	 * @param bulkAction
	 * @return
	 */
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action for identities", 
			/* nickname = "prevalidateBulkAction", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmBulkActionDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/preprocess", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Preprocess bulk action for identities", 
			/* nickname = "preprocessBulkAction", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmBulkActionDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> preprocessBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.preprocessBulkAction(bulkAction);
	}

	/**
	 * Returns given identity's granted authorities
	 * 
	 * @param backendId
	 * @return list of granted authorities
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/authorities", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Identity granted authorities", 
			/* nickname = "getIdentityAuthorities", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_READ })
        }
    )
	public List<? extends GrantedAuthority> getGrantedAuthotrities(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable String backendId) {
		IdmIdentityDto identity = getDto(backendId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(identity, IdmBasePermission.READ);
		//
		return grantedAuthoritiesFactory.getGrantedAuthorities(identity.getUsername());
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/incompatible-roles", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Incompatible roles assigned to identity", 
			/* nickname = "getIdentityIncompatibleRoles", */ 
			tags = { IdmIdentityController.TAG }, 
						description = "Incompatible roles are resolved from assigned identity roles, which can logged used read.")
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.IDENTITY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.IDENTITY_READ })
        }
    )
	public CollectionModel<?> getIncompatibleRoles(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable String backendId) {	
		IdmIdentityDto identity = getDto(backendId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(identity.getId());		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null, IdmBasePermission.READ).getContent();
		//
		Set<ResolvedIncompatibleRoleDto> incompatibleRoles = incompatibleRoleService.resolveIncompatibleRoles(
				identityRoles
					.stream()
					.map(ir -> {
						IdmRoleDto role = DtoUtils.getEmbedded(ir, IdmIdentityRole_.role);
						//
						return role;
					})
					.collect(Collectors.toList())
				);
		//
		return toCollectionModel(incompatibleRoles, ResolvedIncompatibleRoleDto.class);
	}
	
	/**
	 * Get given identity's prime position in organization.
	 * 
	 * @param backendId
	 * @return Positions from root to closest parent
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/work-position", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Identity prime position in organization.", 
			/* nickname = "getIdentityPosition", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_READ })
        }
    )
	public ResponseEntity<?> organizationPosition(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable String backendId) {
		IdmIdentityDto identity = getDto(backendId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmIdentityContractDto primeContract = identityContractService.getPrimeContract(identity.getId());
		if (primeContract == null) {
			return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
		}
		WorkPositionDto position = new WorkPositionDto(identity, primeContract);
		if (primeContract.getWorkPosition() != null) {
			IdmTreeNodeDto contractPosition = treeNodeService.get(primeContract.getWorkPosition());
			position.getPath().addAll(treeNodeService.findAllParents(contractPosition.getId(), Sort.by(Direction.ASC, "forestIndex.lft")));
			position.getPath().add(contractPosition);
		}
		return new ResponseEntity<WorkPositionDto>(position, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/revisions/{revId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Identity audit - read revision detail", 
			/* nickname = "getIdentityRevision", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_READ })
        }
    )
	public ResponseEntity<?> findRevision(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable("backendId") String backendId, 
			 @Parameter(description = "Revision identifier.", required = true)
			@PathVariable("revId") Long revId) {
		IdmIdentityDto originalEntity = getDto(backendId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmIdentity revisionIdentity;
		try {
			revisionIdentity = this.auditService.findRevision(IdmIdentity.class, originalEntity.getId(), revId);
		} catch (RevisionDoesNotExistException ex) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", revId), ex);
		}
		// FIXME: to dto
		return new ResponseEntity<>(revisionIdentity, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/revisions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Identity audit - read all revisions", 
			/* nickname = "getIdentityRevisions", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_READ })
        }
    )
	public CollectionModel<?> findRevisions(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable("backendId") String backendId, 
			Pageable pageable) {
		IdmIdentityDto originalEntity = getDto(backendId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", backendId));
		}
		// get original entity id
		Page<IdmAuditDto> results = this.auditService.findRevisionsForEntity(IdmIdentity.class.getSimpleName(), originalEntity.getId(), pageable);
		return toCollectionModel(results, IdmAuditDto.class);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Identity extended attributes form definitions", 
			/* nickname = "getIdentityFormDefinitions", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.IDENTITY_READ,
						CoreGroupPermission.FORM_DEFINITION_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.IDENTITY_READ,
						CoreGroupPermission.FORM_DEFINITION_AUTOCOMPLETE})
        }
    )
	public ResponseEntity<?> getFormDefinitions(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getFormDefinitions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/form-values/prepare", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Identity form definition - prepare available values", 
			/* nickname = "prepareIdentityFormValues", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_READ})
        }
    )
	public EntityModel<?> prepareFormValues(
			 @Parameter(description = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, required = false) String definitionCode) {
		return super.prepareFormValues(definitionCode);
	}
	
	/**
	 * Returns filled form values
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Identity form definition - read values", 
			/* nickname = "getIdentityFormValues", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITY_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITY_READ})
        }
    )
	public EntityModel<?> getFormValues(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId, 
			 @Parameter(description = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, required = false) String definitionCode) {
		IdmIdentityDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(
				IdmIdentity.class, 
				definitionCode, 
				IdmBasePermission.AUTOCOMPLETE);
		//
		return formDefinitionController.getFormValues(dto, formDefinition, IdmBasePermission.READ);
	}
	
	/**
	 * Save form values.
	 * 
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')"
			+ "or hasAuthority('" + CoreGroupPermission.FORM_VALUE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = { RequestMethod.POST, RequestMethod.PATCH })
	@Operation(
			summary = "Identity form definition - save values", 
			/* nickname = "postIdentityFormValues", */ 
			tags = { IdmIdentityController.TAG }, 
			description = "Only given form attributes by the given values will be saved.")
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.IDENTITY_UPDATE,
						CoreGroupPermission.FORM_VALUE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.IDENTITY_UPDATE,
						CoreGroupPermission.FORM_VALUE_UPDATE})
        }
    )
	public EntityModel<?> saveFormValues(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			 @Parameter(description = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, required = false) String definitionCode,
			 @Parameter(description = "Filled form data.", required = true)
			@RequestBody @Valid List<IdmFormValueDto> formValues) {		
		IdmIdentityDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(
				IdmIdentity.class, 
				definitionCode, 
				IdmBasePermission.AUTOCOMPLETE);
		//
		return formDefinitionController.saveFormValues(dto, formDefinition, formValues, IdmBasePermission.UPDATE);
	}
	
	/**
	 * Save entity's form value
	 * 
	 * @param backendId
	 * @param formValues
	 * @return
	 * @since 9.4.0
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')"
			+ "or hasAuthority('" + CoreGroupPermission.FORM_VALUE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-value", method = { RequestMethod.POST } )
	@Operation(
			summary = "Identity form definition - save value", 
			/* nickname = "postRoleFormValue", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {
 
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							CoreGroupPermission.IDENTITY_UPDATE,
							CoreGroupPermission.FORM_VALUE_UPDATE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							CoreGroupPermission.IDENTITY_UPDATE,
							CoreGroupPermission.FORM_VALUE_UPDATE})
        }
    )
	public EntityModel<?> saveFormValue(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid IdmFormValueDto formValue) {		
		IdmIdentityDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		return formDefinitionController.saveFormValue(dto, formValue, IdmBasePermission.UPDATE);
	}
	
	/**
	 * Returns input stream to attachment saved in given form value.
	 * 
	 * @param backendId
	 * @param formValueId
	 * @return
	 * @since 9.4.0
	 */
	@RequestMapping(value = "/{backendId}/form-values/{formValueId}/download", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Download form value attachment", 
			/* nickname = "downloadFormValue", */
			tags = { IdmIdentityController.TAG },
			description = "Returns input stream to attachment saved in given form value.")
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.IDENTITY_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.IDENTITY_READ })
        }
    )
	public ResponseEntity<InputStreamResource> downloadFormValue(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable String backendId,
			 @Parameter(description = "Form value identifier.", required = true)
			@PathVariable String formValueId) {
		IdmIdentityDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormValueDto value = formService.getValue(dto, DtoUtils.toUuid(formValueId), IdmBasePermission.READ);
		if (value == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, formValueId);
		}
		return formDefinitionController.downloadAttachment(value);
	}
	
	/**
	 * Returns input stream to attachment saved in given form value.
	 * 
	 * @param backendId
	 * @param formValueId
	 * @return
	 * @since 9.4.0
	 */
	@RequestMapping(value = "/{backendId}/form-values/{formValueId}/preview", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Download form value attachment preview", 
			/* nickname = "downloadFormValue", */
			tags = { IdmIdentityController.TAG },
			description = "Returns input stream to attachment preview saved in given form value. Preview is supported for the png, jpg and jpeg mime types only")
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.IDENTITY_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.IDENTITY_READ })
        }
    )
	public ResponseEntity<InputStreamResource> previewFormValue(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable String backendId,
			 @Parameter(description = "Form value identifier.", required = true)
			@PathVariable String formValueId) {
		IdmIdentityDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormValueDto value = formService.getValue(dto, DtoUtils.toUuid(formValueId), IdmBasePermission.READ);
		if (value == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, formValueId);
		}
		return formDefinitionController.previewAttachment(value);
	}
	
	/**
	 * Returns profile image attachment from identity
	 * 
	 * @return 
	 * @since 9.0.0
	 */
	@RequestMapping(value = "/{backendId}/profile", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PROFILE_READ + "')")
	@Operation(
			summary = "Profile", 
			/* nickname = "getProfile", */
			tags = { IdmIdentityController.TAG },
			description = "Returns identity profile."
			/*response = IdmProfileDto.class*/)
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.PROFILE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.PROFILE_READ })
        }
    )
	public ResponseEntity<?> getProfile(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable String backendId) {
		IdmProfileDto profile = profileService.findOneByIdentity(backendId, IdmBasePermission.READ);
		if (profile == null) {
			return new ResponseEntity<InputStreamResource>(HttpStatus.NO_CONTENT);
		}
		return profileController.get(profile.getId().toString());
	}
	
	/**
	 * Update identity profile.
	 * 
	 * @return 
	 * @since 9.0.0
	 */
	@RequestMapping(value = "/{backendId}/profile", method = RequestMethod.PATCH)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PROFILE_UPDATE + "')")
	@Operation(
			summary = "Save profile (create + patch)", 
			/* nickname = "patchProfile", */
			tags = { IdmIdentityController.TAG },
			description = "Save identity profile. Profile is created, when no profile is found, then is updated (patch)."
			/*response = IdmProfileDto.class*/)
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.PROFILE_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.PROFILE_UPDATE })
        }
    )
	public ResponseEntity<?> patchProfile(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable String backendId,
			HttpServletRequest nativeRequest) throws HttpMessageNotReadableException {
		IdmProfileDto profile = profileService.findOrCreateByIdentity(backendId, IdmBasePermission.UPDATE);
		//
		return profileController.patch(profile.getId().toString(), nativeRequest);
	}
	
	/**
	 * Returns profile image attachment from identity
	 * 
	 * @return 
	 * @since 9.0.0
	 */
	@RequestMapping(value = "/{backendId}/profile/image", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PROFILE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Profile image", 
			/* nickname = "getProfileImage", */
			tags = { IdmIdentityController.TAG },
			description = "Returns input stream to identity profile image.")
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.PROFILE_AUTOCOMPLETE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.PROFILE_AUTOCOMPLETE })
        }
    )
	public ResponseEntity<InputStreamResource> getProfileImage(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable String backendId) {
		IdmProfileDto profile = profileService.findOneByIdentity(backendId, IdmBasePermission.AUTOCOMPLETE);
		if (profile == null) {
			return new ResponseEntity<InputStreamResource>(HttpStatus.NO_CONTENT);
		}
		if (profile.getImage() == null) {
			return new ResponseEntity<InputStreamResource>(HttpStatus.NO_CONTENT);
		}
		IdmAttachmentDto attachment = attachmentManager.get(profile.getImage());
		String mimetype = attachment.getMimetype();
		InputStream is = attachmentManager.getAttachmentData(attachment.getId());
		try {
			BodyBuilder response = ResponseEntity
					.ok()
					.contentLength(is.available())
					.header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", attachment.getName()));
			// append media type, if it's filled
			if (StringUtils.isNotBlank(mimetype)) {
				response = response.contentType(MediaType.valueOf(attachment.getMimetype()));
			}
			//
			return response.body(new InputStreamResource(is));
		} catch (IOException e) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, e);
		}
	}
	
	/**
	 * Upload new profile picture
	 *
	 * @param backendId
	 * @param fileName
	 * @param data
	 * @return
	 * @throws IOException
	 * @since 9.0.0
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/profile/image", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PROFILE_UPDATE + "')")
	@Operation(
			summary = "Update profile picture", 
			/* nickname = "postProfilePicture", */ 
			tags = {
			IdmProfileController.TAG }, 
			description = "Upload new profile image")
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.PROFILE_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.PROFILE_UPDATE })
        }
    )
	public ResponseEntity<?> uploadProfileImage(
			 @Parameter(description = "Identity's uuid identifier or username.", required = false) 
			@PathVariable String backendId,
			@RequestParam(required = true, name = "fileName") @NotNull String fileName,
			@RequestParam(required = true, name = "data") MultipartFile data) {
		IdmProfileDto profile = profileService.findOrCreateByIdentity(backendId, IdmBasePermission.READ, IdmBasePermission.CREATE);
		//
		profile = profileService.uploadImage(profile, data, fileName, IdmBasePermission.UPDATE);
		// refresh with permissions are needed
		IdmProfileFilter context = new IdmProfileFilter();
		context.setAddPermissions(true);
		profile = profileController.getService().get(profile, context, IdmBasePermission.READ);
		//
		return new ResponseEntity<>(profileController.toModel(profile), HttpStatus.OK);
	}
	
	/**
	 * Deletes image attachment from identity
	 * 
	 * @return 
	 * @since 9.0.0
	 */
	@RequestMapping(value = "/{backendId}/profile/image", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PROFILE_UPDATE + "')")
	@Operation(
			summary = "Profile picture", 
			/* nickname = "deleteProfilePicure", */
			tags = { IdmIdentityController.TAG },
			description = "Deletes profile picture from identity.")
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.PROFILE_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.PROFILE_UPDATE })
        }
    )
	public ResponseEntity<?> deleteProfileImage(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable String backendId) {
		IdmProfileDto profile = profileService.findOneByIdentity(backendId, IdmBasePermission.READ, IdmBasePermission.UPDATE);
		//
		profile = profileService.deleteImage(profile, IdmBasePermission.UPDATE);
		// refresh with permissions are needed
		IdmProfileFilter context = new IdmProfileFilter();
		context.setAddPermissions(true);
		profile = profileController.getService().get(profile, context, IdmBasePermission.READ);
		//
		return new ResponseEntity<>(profileController.toModel(profile), HttpStatus.OK);
	}
	
	/**
	 * Collapse panel on frontend - persist updated identity profile setting. 
	 * 
	 * @param backendId identity codeable identifier
	 * @param panelId panel identitfier ~ uiKey
	 * @return updated profile
	 * @since 11.2.0
	 */
	@RequestMapping(value = "/{backendId}/profile/panels/{panelId}/collapse", method = RequestMethod.PATCH)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PROFILE_UPDATE + "')")
	@Operation(
			summary = "Collapse panel", 
			/* nickname = "collapsePanel", */
			tags = { IdmIdentityController.TAG },
			description = "Collapse panel - persist updated identity profile setting.")
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.PROFILE_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.PROFILE_UPDATE })
        }
    )
	public ResponseEntity<?> collapsePanel(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			 @Parameter(description = "Panel identifier - uiKey.", required = true)
			@PathVariable @NotNull String panelId) {
		IdmIdentityDto identity = getDto(backendId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmProfileDto profile = profileService.collapsePanel(backendId, panelId, IdmBasePermission.UPDATE);
		//
		// refresh with permissions are needed
		IdmProfileFilter context = new IdmProfileFilter();
		context.setAddPermissions(true);
		profile = profileController.getService().get(profile, context, IdmBasePermission.READ);
		//
		return new ResponseEntity<>(profileController.toModel(profile), HttpStatus.OK);
	}
	
	/**
	 * Expand panel on frontend - persist updated identity profile setting. 
	 * 
	 * @param backendId identity codeable identifier
	 * @param panelId panel identitfier ~ uiKey
	 * @return updated profile
	 * @since 11.2.0
	 */
	@RequestMapping(value = "/{backendId}/profile/panels/{panelId}/expand", method = RequestMethod.PATCH)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PROFILE_UPDATE + "')")
	@Operation(
			summary = "Expand panel", 
			/* nickname = "expandPanel", */
			tags = { IdmIdentityController.TAG },
			description = "Expand panel - persist updated identity profile setting.")
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.PROFILE_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.PROFILE_UPDATE })
        }
    )
	public ResponseEntity<?> expandPanel(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			 @Parameter(description = "Panel identifier - uiKey.", required = true)
			@PathVariable @NotNull String panelId) {
		IdmIdentityDto identity = getDto(backendId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmProfileDto profile = profileService.expandPanel(backendId, panelId, IdmBasePermission.UPDATE);
		//
		// refresh with permissions are needed
		IdmProfileFilter context = new IdmProfileFilter();
		context.setAddPermissions(true);
		profile = profileController.getService().get(profile, context, IdmBasePermission.READ);
		//
		return new ResponseEntity<>(profileController.toModel(profile), HttpStatus.OK);
	}
	
	/**
	 * Returns profile permissions
	 * 
	 * @return 
	 * @since 9.0.0
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/profile/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PROFILE_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.PROFILE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged identity can do with identity profile", 
			/* nickname = "getPermissionsOnIdentityProfile", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.PROFILE_READ,
						CoreGroupPermission.PROFILE_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.PROFILE_READ,
						CoreGroupPermission.PROFILE_AUTOCOMPLETE})
        }
    )
	public Set<String> getProfilePermissions(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmProfileDto profile = profileService.findOneByIdentity(backendId);
		if (profile == null) {
			IdmIdentityDto identity = (IdmIdentityDto) getLookupService().lookupDto(IdmIdentityDto.class, backendId);
			if (identity == null) {
				throw new EntityNotFoundException(IdmIdentity.class, backendId);
			}
			profile = new IdmProfileDto();
			profile.setIdentity(identity.getId());
		}
		//
		// profile can be null (create)
		return profileController.getService().getPermissions(profile);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PASSWORD_READ + "')")
	@RequestMapping(value = "/{backendId}/password", method = RequestMethod.GET)
	@Operation(
			summary = "Get password by identity", 
			/* nickname = "getIdentityPassword", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmPasswordDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmPasswordController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.PASSWORD_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.PASSWORD_READ })
        }
    )
	public ResponseEntity<?> getPassword(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmIdentityDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(dto.getId());
		if (passwordDto == null) {
			return new ResponseEntity<InputStreamResource>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(passwordController.toModel(passwordDto), HttpStatus.OK);
	}
	
	
	/**
	 * Long polling for check unresolved identity role-requests
	 *  
	 * @param backendId - applicant ID
	 * 
	 * @return DeferredResult<OperationResultDto>, where:
	 * 
	 * - EXECUTED = All requests for this identity are resolved,
	 * - RUNNING = Requests are not resolved, but some request was changed (since previous check).
	 * - NOT_EXECUTED = Deferred-result expired
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "{backendId}/check-unresolved-request", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')"
			+ " and hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@Operation(
			summary = "Check changes of unresloved requests for the identity (Long-polling request).", 
			/* nickname = "checkUnresolvedRequests", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLE_REQUEST_READ,
						CoreGroupPermission.IDENTITY_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLE_REQUEST_READ,
						CoreGroupPermission.IDENTITY_READ})
        }
    )
	public DeferredResult<OperationResultDto> checkUnresolvedRequests(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true) @PathVariable @NotNull String backendId) {
		
		IdmIdentityDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		
		UUID identityId = dto.getId();
		DeferredResultWrapper result = new DeferredResultWrapper( //
				identityId, //
				dto.getClass(),//
				new DeferredResult<OperationResultDto>( //
						30000l, new OperationResultDto(OperationState.NOT_EXECUTED)) //
		); //

		result.onCheckResultCallback(new CheckLongPollingResult() {

			@Override
			public void checkDeferredResult(DeferredResult<OperationResultDto> result,
					LongPollingSubscriber subscriber) {
				checkDeferredRequest(result, subscriber);
			}
		});

		// If isn't long polling enabled, then Blocked response will be sent.
		if (!longPollingManager.isLongPollingEnabled()) {
			result.getResult().setResult(new OperationResultDto(OperationState.BLOCKED));
			return result.getResult();
		}

		longPollingManager.addSuspendedResult(result);
		
		return result.getResult();
	}
	
	@Scheduled(fixedDelay = 2000)
	public synchronized void checkDeferredRequests() {
		longPollingManager.checkDeferredRequests(IdmIdentityDto.class);
	}

	/**
	 * Check deferred result - using default implementation from long-polling-manager.
	 * 
	 * @param deferredResult
	 * @param subscriber
	 */
	private void checkDeferredRequest(DeferredResult<OperationResultDto> deferredResult, LongPollingSubscriber subscriber) {
		Assert.notNull(deferredResult, "Deferred result is required.");
		Assert.notNull(subscriber.getEntityId(), "Subscriber identifier is required.");
				
		IdmRoleRequestFilter filter = new IdmRoleRequestFilter();
		filter.setApplicantId(subscriber.getEntityId());
		
		longPollingManager.baseCheckDeferredResult(deferredResult, subscriber, filter, roleRequestService, true);
	}
	
	@Override
	protected IdmIdentityDto validateDto(IdmIdentityDto dto) {
		dto = super.validateDto(dto);
		//
		// state is read only
		if (!getService().isNew(dto)) {
			IdmIdentityDto previous = getDto(dto.getId());
			dto.setState(previous.getState());
		} else {
			dto.setState(IdentityState.CREATED);
		}
		return dto;
	}
	
	@Override
	public RepresentationModel toModel(IdmIdentityDto dto) {
		RepresentationModel resource = super.toModel(dto);
		//
		// add additional links to enable / disable identity
		resource.add(
				WebMvcLinkBuilder.linkTo(this.getClass()).slash(dto.getId()).slash("profile").withRel("profile"),
				WebMvcLinkBuilder.linkTo(this.getClass()).slash(dto.getId()).slash("form-values").withRel("form-values")
		);
		//
		return resource;
	}
	
	@Override
	protected IdmIdentityFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmIdentityFilter filter = new IdmIdentityFilter(parameters, getParameterConverter());
		// to entity decorators
		filter.setSubordinatesFor(getParameterConverter().toEntityUuid(parameters, IdmIdentityFilter.PARAMETER_SUBORDINATES_FOR, IdmIdentityDto.class));
		filter.setSubordinatesByTreeType(getParameterConverter().toEntityUuid(parameters, IdmIdentityFilter.PARAMETER_SUBORDINATES_BY_TREE_TYPE, IdmTreeTypeDto.class));
		filter.setManagersFor(getParameterConverter().toEntityUuid(parameters, IdmIdentityFilter.PARAMETER_MANAGERS_FOR, IdmIdentityDto.class));
		filter.setManagersByTreeType(getParameterConverter().toEntityUuid(parameters, IdmIdentityFilter.PARAMETER_MANAGERS_BY_TREE_TYPE, IdmTreeTypeDto.class));
		filter.setFormProjection(getParameterConverter().toEntityUuid(parameters, IdmIdentityFilter.PARAMETER_FORM_PROJECTION, IdmFormProjectionDto.class));
		// OR is supported only
		if (parameters.containsKey(IdmIdentityFilter.PARAMETER_ROLE)) {
			for (Object role : parameters.get(IdmIdentityFilter.PARAMETER_ROLE)) {
				if (role != null) {
					filter.getRoles().add(getParameterConverter().toEntityUuid((String) role, IdmRoleDto.class));
				}
			}
		}
		// different default than in filter ... i don't know why, but change is to dangerous
		filter.setIncludeGuarantees(getParameterConverter().toBoolean(parameters, IdmIdentityFilter.PARAMETER_INCLUDE_GUARANTEES, false));
		//
		return filter;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @since 11.0.0
	 */
	@Override
	protected IdmIdentityFilter getContext(MultiValueMap<String, Object> parameters) {
		IdmIdentityFilter context = new IdmIdentityFilter(parameters, getParameterConverter());
		// metadata about password are needed for identity detail
		context.setAddPasswordMetadata(true);
		context.setAddBasicFields(true);
		//
		return context;
	}
	
}
