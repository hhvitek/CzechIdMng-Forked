package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.dto.SysAttributeDifferenceDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningArchive;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityTypeManager;
import eu.bcvsolutions.idm.acc.system.entity.SystemEntityTypeRegistrable;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
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
 * Archived provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/provisioning-archives")
@Tag(name = SysProvisioningArchiveController.TAG, description = "Archived provisioning operations (completed, canceled)")
public class SysProvisioningArchiveController extends AbstractReadWriteDtoController<SysProvisioningArchiveDto, SysProvisioningOperationFilter> {

	protected static final String TAG = "Provisioning - archive";
	
	@Autowired private SysSystemEntityTypeManager systemEntityManager;
	
	@Autowired
	public SysProvisioningArchiveController(SysProvisioningArchiveService service) {
		super(service);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PROVISIONING_ARCHIVE_READ + "')")
	@Operation(
        summary = "Search provisioning archive items (/search/quick alias)",
        operationId = "searchProvisioningArchives"
    )
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.PROVISIONING_ARCHIVE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.PROVISIONING_ARCHIVE_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PROVISIONING_ARCHIVE_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search provisioning archive items",
			operationId = "searchQuickProvisioningArchives",
			tags = { SysProvisioningArchiveController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.PROVISIONING_ARCHIVE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.PROVISIONING_ARCHIVE_READ })
    })
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
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PROVISIONING_ARCHIVE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			operationId = "countProvisioningArchives",
			tags = { SysProvisioningArchiveController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.PROVISIONING_ARCHIVE_COUNT }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.PROVISIONING_ARCHIVE_COUNT })
    })
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	/**
	 * Adds embedded objects.
	 */
	@Override
	public Page<SysProvisioningArchiveDto> find(SysProvisioningOperationFilter filter, Pageable pageable, BasePermission permission) {
		Page<SysProvisioningArchiveDto> results = super.find(filter, pageable, permission);
		// fill entity embedded for FE
		Map<UUID, BaseDto> loadedDtos = new HashMap<>();
		results
			.getContent()
			.stream()
			.filter(operation -> {
				return operation.getEntityIdentifier() != null;
			})
			.forEach(operation -> {
				if (!loadedDtos.containsKey(operation.getEntityIdentifier())) {
					SystemEntityTypeRegistrable systemEntityType = systemEntityManager.getSystemEntityByCode(operation.getEntityType());
					loadedDtos.put(operation.getEntityIdentifier(), getLookupService().lookupDto(systemEntityType.getEntityType(), operation.getEntityIdentifier()));
				}
				operation.getEmbedded().put("entity", loadedDtos.get(operation.getEntityIdentifier()));
			});
		return results;
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PROVISIONING_ARCHIVE_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "Provisioning archive item detail",
			operationId = "getProvisioningArchive",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysProvisioningArchive.class
                                    )
                            )
                    }
            ),
			tags = { SysProvisioningArchiveController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.PROVISIONING_ARCHIVE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.PROVISIONING_ARCHIVE_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Provisioning archive item's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PROVISIONING_ARCHIVE_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			operationId = "getPermissionsOnProvisioningArchive",
			tags = { SysProvisioningArchiveController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.PROVISIONING_ARCHIVE_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.PROVISIONING_ARCHIVE_READ})
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "Archive's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/difference-object", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.PROVISIONING_ARCHIVE_READ + "')")
	@Operation(
			summary = "Detail of the provisioning changes",
			operationId = "getProvisioningDetail",
			tags = { SysProvisioningArchiveController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.PROVISIONING_ARCHIVE_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.PROVISIONING_ARCHIVE_READ})
        }
    )
	public ResponseEntity<?> getDifferenceObject(
			 @Parameter(description = "Provisioning detail uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		SysProvisioningArchiveDto archive = getDto(backendId);
		if (archive == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		ProvisioningContext context = archive.getProvisioningContext();
		if (context == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		List<SysAttributeDifferenceDto> result = ((SysProvisioningArchiveService)getService()).evaluateProvisioningDifferences(context.getSystemConnectorObject(), context.getConnectorObject());
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@Override
	protected SysProvisioningOperationFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new SysProvisioningOperationFilter(parameters, getParameterConverter());
	}
}
