package eu.bcvsolutions.idm.core.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.PublicController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * "Naive" status
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/status")
@Tag(
		name = StatusController.TAG,  

		description = "Application status"//,
		


)
public class StatusController implements PublicController {

	public static final String OK_STATUS_PLAIN = "OK";
	public static final String OK_STATUS_HTML = "<html><head><title>CzechIdM API</title></head><body><h1>CzechIdM API is running</h1><p>If you can see this message, API is running</p></body></html>";
	protected static final String TAG = "Status";
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
	@Operation(
			summary = "Get status", 
			/* nickname = "getPlainStatus", */
			tags = { StatusController.TAG })
	public String getPlainStatus() {
		return OK_STATUS_PLAIN;
	}
	
	@Operation(
			summary = "Get status",
			/* nickname = "getPlainStatus", */
			tags = { StatusController.TAG })
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public String getHtmlStatus() {
		return OK_STATUS_HTML;
	}
}
