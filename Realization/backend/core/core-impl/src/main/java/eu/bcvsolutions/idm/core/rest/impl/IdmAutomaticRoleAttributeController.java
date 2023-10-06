package eu.bcvsolutions.idm.core.rest.impl;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleRequestType;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleRequestService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Automatic role controller by attribute
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/automatic-role-attributes")
@Tag(
		name = IdmAutomaticRoleAttributeController.TAG,  

		description = "Automatic roles by attribute"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE

//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmAutomaticRoleAttributeController extends AbstractReadWriteDtoController<IdmAutomaticRoleAttributeDto, IdmAutomaticRoleFilter> {

	protected static final String TAG = "Automatic roles by attribute";
	@Autowired
	private IdmAutomaticRoleRequestService requestService;
	
	@Autowired
	public IdmAutomaticRoleAttributeController(IdmAutomaticRoleAttributeService entityService) {
		super(entityService);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ + "')")
	@Operation(
			summary = "Search automatic roles by attribute (/search/quick alias)", 
			/* nickname = "searchAutomaticRoleAttributes", */
			tags = { IdmAutomaticRoleAttributeController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ + "')")
	@Operation(
			summary = "Search automatic roles", 
			/* nickname = "searchQuickAutomaticRoleAttibutes", */
			tags = { IdmAutomaticRoleAttributeController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete automatic roles (selectbox usage)", 
			/* nickname = "autocompleteAutomaticRoleAttibutes", */
			tags = { IdmAutomaticRoleAttributeController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ + "')")
	@Operation(
			summary = "Automatic role detail", 
			/* nickname = "getAutomaticRoleAttributeRule", */
			/* response = IdmAutomaticRoleAttributeDto.class, */
			tags = { IdmAutomaticRoleAttributeController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Automatic role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/delete-via-request/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_DELETE + "')")
	@Operation(
			summary = "Delete automatic role by attribute. Uses request.", 
			/* nickname = "deleteAutomaticRoleAttributeViaRequest", */
			tags = { IdmAutomaticRoleAttributeController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_DELETE })
        }
    )
	public ResponseEntity<?> deleteViaRequest(
			@Parameter(name = "Automatic role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmAutomaticRoleAttributeDto automaticRole = this.getDto(backendId);
		Assert.notNull(automaticRole, "Automatic role is required.");
		requestService.deleteAutomaticRole(automaticRole, AutomaticRoleRequestType.ATTRIBUTE);
		throw new AcceptedException();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnAutomaticRole", */
			tags = { IdmAutomaticRoleAttributeController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_READ })
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Automatic role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	
	@Override
	public IdmAutomaticRoleAttributeDto postDto(IdmAutomaticRoleAttributeDto entity) {
		if (!getService().isNew(entity)) {
			throw new ResultCodeException(CoreResultCode.METHOD_NOT_ALLOWED, "Automatic role by attribute update is not supported");
		}
		return super.postDto(entity);
	}	
}