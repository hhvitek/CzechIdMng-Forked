package eu.bcvsolutions.idm.core.security.rest.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.dto.BasePermissionDto;
import eu.bcvsolutions.idm.core.security.dto.GroupPermissionDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Provides autorities configurable for idm roles
 * 
 * @author Radek Tomiška 
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/authorities")
@Tag(name = IdmAuthorityController.TAG, description = "Role authorities" )
public class IdmAuthorityController implements BaseController {
	
	protected static final String TAG = "Authorities";	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmAuthorityController.class);
	//
	@Autowired private ModuleService moduleService;
	
	/**
	 * Provides configurable authorities for roles
	 * 
	 * @return
	 */
	@Operation(
			summary = "Provides available authorities for roles.", 
			description = "Returns authorities from all enabled modules.",
			/* nickname = "getAvailableAuthorities", */
			tags={ IdmAuthorityController.TAG })
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	@RequestMapping(path = "/search/available", method = RequestMethod.GET)
	public List<GroupPermissionDto> getAvailableAuthorities() {
		LOG.debug("Loading all available authorities");
		Map<String, GroupPermissionDto> distinctAuthorities = disctinctAuthorities(moduleService.getAvailablePermissions());
		LOG.debug("Loaded all available authorities [groups:{}]", distinctAuthorities.size());
		return new ArrayList<>(distinctAuthorities.values());
	}
	
	/**
	 * Provides configurable authorities for roles
	 * 
	 * @return
	 */
	@Operation(
			summary = "Provides configurable authorities for roles",
			description = "Returns authorities from all instaled modules. All authorities are needed in security cofiguration. Module can be disabled, but configured security has to remain.",
			/* nickname = "getAllAuthorities", */
			tags={ IdmAuthorityController.TAG })
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	@RequestMapping(path = "/search/all", method = RequestMethod.GET)
	public List<GroupPermissionDto> getAllAuthorities() {
		LOG.debug("Loading all authorities");
		Map<String, GroupPermissionDto> distinctAuthorities = disctinctAuthorities(moduleService.getAllPermissions());
		LOG.debug("Loaded all authorities [groups:{}]", distinctAuthorities.size());
		return new ArrayList<>(distinctAuthorities.values());
	}
	
	/**
	 * Trims redundant permissions
	 * 
	 * @param permissions
	 * @return
	 */
	private Map<String, GroupPermissionDto> disctinctAuthorities(List<GroupPermission> permissions) {
		Map<String, GroupPermissionDto> distinctAuthorities = new HashMap<>();
		permissions.forEach(groupPermission -> {
			if (!distinctAuthorities.containsKey(groupPermission.getName())) {
				distinctAuthorities.put(groupPermission.getName(), new GroupPermissionDto(groupPermission));
			}
			groupPermission.getPermissions().forEach(basePermission -> {
				distinctAuthorities.get(groupPermission.getName()).getPermissions().add(new BasePermissionDto(basePermission));
			});
		});
		return distinctAuthorities;
	}

}
