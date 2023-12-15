package eu.bcvsolutions.idm.document.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.document.DocumentModuleDescriptor;
import eu.bcvsolutions.idm.document.domain.DocumentResultCode;
import eu.bcvsolutions.idm.document.service.api.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Document controller
 *
 */
@RestController
@Enabled(DocumentModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseController.BASE_PATH + "/doc")
@Tag(name = DocumentController.TAG, description = "Document operations")

public class DocumentController {
	
	protected static final String TAG = "Documents";
	@Autowired private DocumentService service;

//	@ResponseBody
//	@RequestMapping(method = RequestMethod.GET, path = "/ping")
//	@Operation(
//			summary = "Ping - Pong operation",
//			description= "Returns message with additional informations",
//			/*, nickname = "ping", */
//            responses = @ApiResponse(
//                    responseCode = "200",
//                    content = {
//                            @Content(
//                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
//                                    schema = @Schema(
//                                            implementation = Pong.class
//                                    )
//                            )
//                    }
//            ))
//    @SecurityRequirements({
//        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
//                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
//            }
//    )
//	public ResponseEntity<Pong> ping(
//			 @Parameter(description = "In / out message", example = "hello")
//			@RequestParam(required = false, defaultValue = "hello") String message
//			) {
//		return new ResponseEntity<>(service.ping(message), HttpStatus.OK);
//	}
//
//
//	@ResponseBody
//	@ResponseStatus(code = HttpStatus.NO_CONTENT)
//	@RequestMapping(method = RequestMethod.GET, path = "/notification")
//	@Operation(
//			summary = "Send notification",
//			description= "Sending given message to currently logged identity (example topic is used).",
//			operationId = "sendNotification",
//			tags={ DocumentController.TAG })
//    @SecurityRequirements({
//        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
//                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
//            }
//    )
//	public void sendNotification(
//			 @Parameter(description = "Notification message", example = "hello")
//			@RequestParam(required = false, defaultValue = "hello") String message) {
//		service.sendNotification(message);
//	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, path = "/client-error")
	@Operation(
			summary = "Example client error", 
			description= "Example client error with given parameter.",
			operationId = "exampleClientError",
			tags={ DocumentController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	public void clientError(
			 @Parameter(description = "Error parameter", example = "parameter")
			@RequestParam(required = false, defaultValue = "parameter") String parameter) {
		// lookout - ImmutableMap parameter values cannot be {@code null}
		throw new ResultCodeException(DocumentResultCode.DOCUMENT_CLIENT_ERROR, ImmutableMap.of("parameter", String.valueOf(parameter)));
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, path = "/server-error")
	@Operation(
			summary = "Example server error", 
			description= "Example server error with given parameter.",
			operationId = "exampleServerError",
			tags={ DocumentController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST)
            }
    )
	public void serverError(
            @Parameter(description = "Error parameter", example = "parameter")
			@RequestParam(required = false, defaultValue = "parameter") String parameter) {
		// lookout - ImmutableMap parameter values cannot be {@code null}
		throw new ResultCodeException(DocumentResultCode.DOCUMENT_SERVER_ERROR, ImmutableMap.of("parameter", String.valueOf(parameter)));
	}
}
