package eu.bcvsolutions.idm.core.rest.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Module controler can enable / disable module etc.
 *
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/modules")
@Api(
		value = ModuleController.TAG,
		description = "Application modules configuration",
		tags = { ModuleController.TAG },
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class ModuleController {

	protected static final String TAG = "Modules";
	//
	@Autowired private ModuleService moduleService;
	@Autowired private ModelMapper mapper;
	@Autowired private LookupService lookupService;
	@Autowired private ObjectMapper objectMapper;
	//
	private ParameterConverter parameterConverter = null;


	/**
	 * Returns all installed modules
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_READ + "')")
	@ApiOperation(
			value = "Get all installed modules",
			nickname = "getInstalledModules",
			tags = { ModuleController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") })
				})
	public List<ModuleDescriptorDto> getInstalledModules() {
		return moduleService.getInstalledModules() //
				.stream() //
				.map(moduleDescriptor -> { //
					return toModel(moduleDescriptor);
				}) //
				.collect(Collectors.toList());
	}

	/**
	 * Returns selected module
	 *
	 * @param moduleId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{moduleId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_READ + "')")
	@ApiOperation(
			value = "Module detail",
			nickname = "getModule",
			tags = { ModuleController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") })
				})
	public ModuleDescriptorDto get(
			@ApiParam(value = "Module's identifier.", required = true)
			@PathVariable @NotNull String moduleId) {
		ModuleDescriptor moduleDescriptor = moduleService.getModule(moduleId);
		if (moduleDescriptor == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", moduleId));
		}
		return toModel(moduleDescriptor);

	}

	/**
	 * Supports enable / disable only
	 *
	 * @param moduleId
	 * @param nativeRequest
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{moduleId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_UPDATE + "')")
	@ApiOperation(
			value = "Update module properties",
			nickname = "putModule",
			tags = { ModuleController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") })
				},
			notes = "Supports enable / disable only")
	public ModuleDescriptorDto put(
			@ApiParam(value = "Module's identifier.", required = true)
			@PathVariable @NotNull String moduleId,
			@Valid @RequestBody ModuleDescriptorDto dto) {
		ModuleDescriptor updatedModuleDescriptor = moduleService.getModule(moduleId);
		if (updatedModuleDescriptor == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", moduleId));
		}
		//
		moduleService.setEnabled(moduleId, !dto.isDisabled());
		return get(moduleId);
	}

	/**
	 * Supports enable / disable only
	 *
	 * @param moduleId
	 * @param nativeRequest
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{moduleId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_UPDATE + "')")
	@ApiOperation(
			value = "Update module properties",
			nickname = "patchModule",
			tags = { ModuleController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") })
				},
			notes = "Supports enable / disable only")
	public ModuleDescriptorDto patch(
			@ApiParam(value = "Module's identifier.", required = true)
			@PathVariable @NotNull String moduleId,
			HttpServletRequest nativeRequest) {
		ModuleDescriptor updatedModuleDescriptor = moduleService.getModule(moduleId);
		if (updatedModuleDescriptor == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", moduleId));
		}
		//
		ServletServerHttpRequest request = new ServletServerHttpRequest(nativeRequest);
		try {
			ModuleDescriptorDto dto = objectMapper.readValue(request.getBody(), ModuleDescriptorDto.class);
			moduleService.setEnabled(moduleId, !dto.isDisabled());
		} catch (IOException ex) {
			throw new ResultCodeException(CoreResultCode.BAD_REQUEST, ex);
		}
		//
		return get(moduleId);
	}

	/**
	 * Enable module (supports FE and BE  module)
	 *
	 * @param moduleId
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{moduleId}/enable", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_UPDATE + "')")
	@ApiOperation(
			value = "Enable module",
			nickname = "enableModule",
			tags = { ModuleController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") })
				})
	public void enable(
			@ApiParam(value = "Module's identifier.", required = true)
			@PathVariable @NotNull String moduleId) {
		moduleService.setEnabled(moduleId, true);
	}

	/**
	 * Disable module (supports FE and BE  module)
	 *
	 * @param moduleId
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{moduleId}/disable", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_UPDATE + "')")
	@ApiOperation(
			value = "Disable module",
			nickname = "disableModule",
			tags = { ModuleController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") })
				})
	public void disable(
			@ApiParam(value = "Module's identifier.", required = true)
			@PathVariable @NotNull String moduleId) {
		moduleService.setEnabled(moduleId, false);
	}

	@ResponseBody
	@RequestMapping(value = "/{moduleId}/result-codes", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_READ + "')")
	@ApiOperation(
			value = "Get result codes",
			nickname = "resultCodes",
			tags = { ModuleController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") })
			})
	public List<DefaultResultModel> resultCodes(
			@ApiParam(value = "Module's identifier", required = true)
			@PathVariable @NotNull String moduleId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		List<DefaultResultModel> resultModelList = moduleService.getModule(moduleId).getResultCodes()
				.stream()
				.map(resultCode -> new DefaultResultModel(resultCode))
				.collect(Collectors.toList());
		// TODO: move filter into manager
		if (parameters.containsKey(DataFilter.PARAMETER_TEXT)) {
			String text = getParameterConverter().toString(parameters, DataFilter.PARAMETER_TEXT);
			resultModelList = resultModelList
					.stream()
					.filter(defaultResultModel -> defaultResultModel.getStatusEnum().toLowerCase().contains(text.toLowerCase()))
					.collect(Collectors.toList());
		}
		return resultModelList;
	}

	private ParameterConverter getParameterConverter() {
		if (parameterConverter == null) {
			parameterConverter = new ParameterConverter(lookupService);
		}
		return parameterConverter;
	}

	/**
	 * TODO: resource support + self link
	 *
	 * @param moduleDescriptor
	 * @return
	 */
	protected ModuleDescriptorDto toModel(ModuleDescriptor moduleDescriptor) {
		ModuleDescriptorDto dto = mapper.map(moduleDescriptor,  ModuleDescriptorDto.class);
		//
		dto.setId(moduleDescriptor.getId());
		dto.setDisabled(!moduleService.isEnabled(moduleDescriptor));
		return dto;
	}

}
