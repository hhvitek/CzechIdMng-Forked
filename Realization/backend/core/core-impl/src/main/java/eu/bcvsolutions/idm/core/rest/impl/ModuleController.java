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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Module controler can enable / disable module etc.
 *
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/modules")
@Tag(name = ModuleController.TAG, description = "Application modules configuration")
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
	@Operation(
			summary = "Get all installed modules",
			operationId = "getInstalledModules",
			tags = { ModuleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_READ })
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
	@Operation(
			summary = "Module detail",
			operationId = "getModule",
			tags = { ModuleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_READ })
    })
	public ModuleDescriptorDto get(
			 @Parameter(description = "Module's identifier.", required = true)
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
	@Operation(
			summary = "Update module properties",
			operationId = "putModule",
			tags = { ModuleController.TAG },
						description = "Supports enable / disable only")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_UPDATE })
    })
	public ModuleDescriptorDto put(
			 @Parameter(description = "Module's identifier.", required = true)
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
	@Operation(
			summary = "Update module properties",
			operationId = "patchModule",
			tags = { ModuleController.TAG },
						description = "Supports enable / disable only")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_UPDATE })
    })
	public ModuleDescriptorDto patch(
			 @Parameter(description = "Module's identifier.", required = true)
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
	@Operation(
			summary = "Enable module",
			operationId = "enableModule",
			tags = { ModuleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_UPDATE })
    })
	public void enable(
			 @Parameter(description = "Module's identifier.", required = true)
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
	@Operation(
			summary = "Disable module",
			operationId = "disableModule",
			tags = { ModuleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_UPDATE })
    })
	public void disable(
			 @Parameter(description = "Module's identifier.", required = true)
			@PathVariable @NotNull String moduleId) {
		moduleService.setEnabled(moduleId, false);
	}

	@ResponseBody
	@RequestMapping(value = "/{moduleId}/result-codes", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_READ + "')")
	@Operation(
			summary = "Get result codes",
			operationId = "resultCodes",
			tags = { ModuleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_READ })
    })
	public List<DefaultResultModel> resultCodes(
			 @Parameter(description = "Module's identifier", required = true)
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
