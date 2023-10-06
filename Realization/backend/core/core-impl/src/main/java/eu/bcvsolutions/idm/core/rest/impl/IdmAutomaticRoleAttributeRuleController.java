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
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Controller of rules for automatic role attribute
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/automatic-role-attribute-rules")
@Tag(
		name = IdmAutomaticRoleAttributeRuleController.TAG,

		description = "Rules for automatic role attribute"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmAutomaticRoleAttributeRuleController extends AbstractReadWriteDtoController<IdmAutomaticRoleAttributeRuleDto, IdmAutomaticRoleAttributeRuleFilter>{

	protected static final String TAG = "Rules for automatic role attribute";
	
	@Autowired
	public IdmAutomaticRoleAttributeRuleController(
			IdmAutomaticRoleAttributeRuleService entityService) {
		super(entityService);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ + "')")
	@Operation(
			summary = "Search rules for automatic roles by attribute (/search/quick alias)", 
			/* nickname = "searchAutomaticRoleAttributeRules", */ 
			tags = { IdmAutomaticRoleAttributeRuleController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ + "')")
	@Operation(
			summary = "Search rules for automatic roles", 
			/* nickname = "searchQuickAutomaticRoleAttributeRules", */ 
			tags = { IdmAutomaticRoleAttributeRuleController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ + "')")
	@Operation(
			summary = "Rule detail", 
			/* nickname = "getAutomaticRoleAttributeRule", */ 
			/* response = IdmAutomaticRoleAttributeRuleDto.class, */
			tags = { IdmAutomaticRoleAttributeRuleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Rule's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnAutomaticRoleRules", */ 
			tags = { IdmIdentityController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ})
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Rule's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
}
