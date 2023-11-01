package eu.bcvsolutions.idm.core.rest.impl;


import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordPolicyFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Default controller for password policy.
 * 
 * Lookout: all logged identities can read configured password policies => DefaultReadWriteDtoController usage is ok.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/password-policies")
@Tag(name = IdmPasswordPolicyController.TAG, description = "Operations with password policies")
public class IdmPasswordPolicyController extends DefaultReadWriteDtoController<IdmPasswordPolicyDto, IdmPasswordPolicyFilter> {
	
	protected static final String TAG = "Password policies";
	private final IdmPasswordPolicyService passwordPolicyService;
	
	@Autowired
	public IdmPasswordPolicyController(IdmPasswordPolicyService passwordPolicyService) {
		super(passwordPolicyService);
		this.passwordPolicyService = passwordPolicyService;
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PASSWORDPOLICY_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.PASSWORDPOLICY_UPDATE + "')")
	@Operation(
        summary = "Create / update password policy",
        operationId = "postPasswordPolicy",
        responses = @ApiResponse(
            responseCode = "200",
            content = {
                @Content(
                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                    schema = @Schema(
                        implementation = IdmPasswordPolicy.class
                    )
                )
            }
        )
	)
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.PASSWORDPOLICY_CREATE,
						CoreGroupPermission.PASSWORDPOLICY_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.PASSWORDPOLICY_CREATE,
						CoreGroupPermission.PASSWORDPOLICY_UPDATE})
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull IdmPasswordPolicyDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PASSWORDPOLICY_UPDATE + "')")
	@Operation(
			summary = "Update password policy",
			operationId = "putPasswordPolicy", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmPasswordPolicy.class
                                    )
                            )
                    }
            ), 
			tags = { IdmPasswordPolicyController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.PASSWORDPOLICY_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.PASSWORDPOLICY_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmPasswordPolicyDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PASSWORDPOLICY_UPDATE + "')")
	@Operation(
			summary = "Update password policy",
			operationId = "patchIdentity", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmPasswordPolicy.class
                                    )
                            )
                    }
            ), 
			tags = { IdmPasswordPolicyController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.PASSWORDPOLICY_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.PASSWORDPOLICY_UPDATE })
    })
	public ResponseEntity<?> patch(
			 @Parameter(description = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.PASSWORDPOLICY_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete password policy",
			operationId = "deletePasswordPolicy", 
			tags = { IdmPasswordPolicyController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.PASSWORDPOLICY_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.PASSWORDPOLICY_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	/**
	 * Return generate password by password policy.
	 * Check password policy type.
	 * 
	 * @param backendId
	 * @return string, new password
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/generate", method = RequestMethod.GET)
	@Operation(
			summary = "Generate password",
			operationId = "generatePassword", 
			tags = { IdmPasswordPolicyController.TAG },
			description = "Returns generated password by password policy.")
	public String generate(
			 @Parameter(description = "Policy's uuid identifier.", required = true)
			@PathVariable String backendId) {
		IdmPasswordPolicyDto entity = getPasswordPolicy(backendId);
		//
		return this.passwordPolicyService.generatePassword(entity);
	}
	
	/**
	 * Validate password by given password policy id
	 * 
	 * @param backendId
	 * @return
	 */
	@RequestMapping(value = "/{backendId}/validate", method = RequestMethod.POST)
	@Operation(
			summary = "Validate password",
			operationId = "validatePassword", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmPasswordValidationDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmPasswordPolicyController.TAG },
			description = "Validate password by password policy.")
	public EntityModel<IdmPasswordValidationDto> validate(
			 @Parameter(description = "Policy's uuid identifier.", required = true)
			@PathVariable String backendId,
			@Valid @RequestBody(required = true) IdmPasswordValidationDto password) {
		IdmPasswordPolicyDto passwordPolicy = getPasswordPolicy(backendId);
		//
		this.passwordPolicyService.validate(password, passwordPolicy);
		//
		password.setValid(true);
		//
		return new EntityModel<IdmPasswordValidationDto>(password);
	}
	
	/**
	 * Validate password by default validate policy
	 * 
	 * @return
	 */
	@RequestMapping(value = "/validate/default", method = RequestMethod.POST)
	@Operation(
			summary = "Validate password (by default policy)",
			operationId = "validatePasswordByDefault", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmPasswordValidationDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmPasswordPolicyController.TAG },
			description = "Validate password by default password policy.")
	public EntityModel<IdmPasswordValidationDto> validateByDefault(@Valid @RequestBody(required = true) IdmPasswordValidationDto password) {
		this.passwordPolicyService.validate(password);
		//
		password.setValid(true);
		//
		return new EntityModel<IdmPasswordValidationDto>(password);
	}
	
	
	/**
	 * Method generate password by default generate password policy.
	 * This policy is only one.
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/generate/default", method = RequestMethod.GET)
	@Operation(
			summary = "Generate password (by default policy)",
			operationId = "genaratePasswordByDefault", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmPasswordValidationDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmPasswordPolicyController.TAG },
			description = "Returns generated password by default password policy.")
	public EntityModel<String> generateByDefaultPolicy() {
		return new EntityModel<>(passwordPolicyService.generatePasswordByDefault());
	}
	
	/**
	 * Method return {@link IdmPasswordPolicyDto} for given backendId. Returned
	 * {@link IdmPasswordPolicyDto} must be VALIDATE type
	 * 
	 * @param backendId
	 * @return
	 */
	private IdmPasswordPolicyDto getPasswordPolicy(String backendId) {
		IdmPasswordPolicyDto entity = this.passwordPolicyService.get(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		} else if (entity.getType() == IdmPasswordPolicyType.VALIDATE) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_BAD_TYPE, ImmutableMap.of("type", entity.getType()));
		}
		return entity;
	}
	
	@Override
	protected IdmPasswordPolicyFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new IdmPasswordPolicyFilter(parameters, getParameterConverter());
	}
}
