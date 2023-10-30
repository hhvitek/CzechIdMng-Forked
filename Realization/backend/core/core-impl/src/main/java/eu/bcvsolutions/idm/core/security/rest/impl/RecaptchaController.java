package eu.bcvsolutions.idm.core.security.rest.impl;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.security.api.dto.RecaptchaRequest;
import eu.bcvsolutions.idm.core.security.api.dto.RecaptchaResponse;
import eu.bcvsolutions.idm.core.security.api.service.RecaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for checking ReCaptcha.
 * 
 * @author Filip Mestanek
 */
@Controller
@RequestMapping(value = BaseController.BASE_PATH + RecaptchaController.URL_PATH)
@Tag(name = RecaptchaController.TAG, description = "Operation with reCAPTCHA protection")
public class RecaptchaController implements BaseController {
	
	protected static final String TAG = "Recaptcha";
	public static final String URL_PATH = "/public/recaptcha";
	//
	@Autowired private RecaptchaService recaptchaService;
	
	/**
	 * ReCaptcha confirmation.
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@Operation(
			summary = "Check reCAPTCHA protection"
			/* nickname = "confirmRecaptcha", */ 
			
			/*response = RecaptchaResponse.class*/)
	public ResponseEntity<RecaptchaResponse> confirmRecaptcha(
			 @Parameter(description = "Request to check.", required = true)
			@RequestBody @Valid RecaptchaRequest request) {
		RecaptchaResponse response = recaptchaService.checkRecaptcha(request);
		//
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
