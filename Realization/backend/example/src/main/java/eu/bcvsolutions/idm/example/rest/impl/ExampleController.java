package eu.bcvsolutions.idm.example.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;
import eu.bcvsolutions.idm.example.domain.ExampleResultCode;
import eu.bcvsolutions.idm.example.dto.Pong;
import eu.bcvsolutions.idm.example.service.api.ExampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Example controller
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Enabled(ExampleModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseController.BASE_PATH + "/examples")
@Tag(name = ExampleController.TAG, description = "Example operations")

public class ExampleController {
	
	protected static final String TAG = "Examples";
	@Autowired private ExampleService service;

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, path = "/ping")
	@Operation(
			summary = "Ping - Pong operation", 
			description= "Returns message with additional informations",
			/*, nickname = "ping", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = Pong.class
                                    )
                            )
                    }
            ))
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	public ResponseEntity<Pong> ping(
			 @Parameter(description = "In / out message", example = "hello")
			@RequestParam(required = false, defaultValue = "hello") String message
			) {
		return new ResponseEntity<>(service.ping(message), HttpStatus.OK); 
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, path = "/private-name")
	@Operation(
			summary = "Read private summary", 
			description= "Returns configuration property - private value.",
			/* nickname = "getPrivateValue", */ 
			tags={ ExampleController.TAG })
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	public String getPrivateValue() {
		return service.getPrivateValue();
	}
	
	@ResponseBody
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(method = RequestMethod.GET, path = "/notification")
	@Operation(
			summary = "Send notification", 
			description= "Sending given message to currently logged identity (example topic is used).",
			/* nickname = "sendNotification", */ 
			tags={ ExampleController.TAG })
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	public void sendNotification(
			 @Parameter(description = "Notification message", example = "hello")
			@RequestParam(required = false, defaultValue = "hello") String message) {
		service.sendNotification(message);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, path = "/client-error")
	@Operation(
			summary = "Example client error", 
			description= "Example client error with given parameter.",
			/* nickname = "exampleClientError", */ 
			tags={ ExampleController.TAG })
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	public void clientError(
			 @Parameter(description = "Error parameter", example = "parameter")
			@RequestParam(required = false, defaultValue = "parameter") String parameter) {
		// lookout - ImmutableMap parameter values cannot be {@code null}
		throw new ResultCodeException(ExampleResultCode.EXAMPLE_CLIENT_ERROR, ImmutableMap.of("parameter", String.valueOf(parameter)));
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, path = "/server-error")
	@Operation(
			summary = "Example server error", 
			description= "Example server error with given parameter.",
			/* nickname = "exampleServerError", */ 
			tags={ ExampleController.TAG })
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	public void serverError(
            @Parameter(description = "Error parameter", example = "parameter")
			@RequestParam(required = false, defaultValue = "parameter") String parameter) {
		// lookout - ImmutableMap parameter values cannot be {@code null}
		throw new ResultCodeException(ExampleResultCode.EXAMPLE_SERVER_ERROR, ImmutableMap.of("parameter", String.valueOf(parameter)));
	}
}
