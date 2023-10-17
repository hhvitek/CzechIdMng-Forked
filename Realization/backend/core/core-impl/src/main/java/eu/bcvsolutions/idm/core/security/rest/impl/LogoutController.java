package eu.bcvsolutions.idm.core.security.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Logout
 * 
 * @author Radek Tomiška 
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/logout")
@Tag(name = LogoutController.TAG, description = "Logout endpoint")
public class LogoutController implements BaseController {
	
	protected static final String TAG = "Logout";
	//
	@Autowired private AuthenticationManager authenticationManager;
	
	@Operation(
			summary = "Logout",
			description= "Logout currently logged identity and disable currently used token.",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = LoginDto.class
                                    )
                            )
                    }
            ),
			tags = { LogoutController.TAG })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RequestMapping(method = RequestMethod.DELETE)
	public void logout() {
		authenticationManager.logout();
	}
}
