package eu.bcvsolutions.idm.core.rest.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.domain.ApplicationConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.theme.ThemeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * Provides public configurations.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/public/configurations")
@Tag( 
        name = IdmConfigurationController.TAG,
		description = "Public configuration items"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class PublicIdmConfigurationController implements BaseController {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PublicIdmConfigurationController.class);
	//
	private final ConfigurationService configurationService;
	//
	@Autowired private ApplicationConfiguration applicationConfiguration;
	@Autowired private AttachmentManager attachmentManager;
	
	@Autowired
	public PublicIdmConfigurationController(ConfigurationService configurationService) {
		Assert.notNull(configurationService, "Service is required.");
		//
		this.configurationService = configurationService;
	}
	
	/**
	 * Returns all public configuration properties 
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	@Operation(
			summary = "Read public configuration items", 
			/* nickname = "findAllPublicConfigurations", */ 
			tags = { IdmConfigurationController.TAG })
	public List<IdmConfigurationDto> getAllPublicConfigurations() {
		// TODO: resource wrapper + assembler
		return configurationService.getAllPublicConfigurations();
	}
	
	/**
	 * Download application logo.
	 * 
	 * @return logo input stream
	 * @since 12.0.0
	 */
	@RequestMapping(name = "/application/logo", method = RequestMethod.GET)
	@Operation(
			summary = "Download application logo", 
			/* nickname = "downloadApplicationLogo", */ 
			tags = { IdmConfigurationController.TAG })
	public ResponseEntity<InputStreamResource> downloadApplicationLogo() {
		UUID uuid = applicationConfiguration.getApplicationLogoId();
		// 
		// not configured
		if (uuid == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		// configuration is wrong
		IdmAttachmentDto attachment = attachmentManager.get(uuid);
		if (attachment == null) {
			LOG.warn("Application logo  with attachment identifier [{}] not found, returning null. Change configuration [{}]", uuid, ApplicationConfiguration.PROPERTY_APPLICATION_LOGO);
			//
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		String mimetype = attachment.getMimetype();
		InputStream is = attachmentManager.getAttachmentData(attachment.getId());
		try {
			BodyBuilder response = ResponseEntity
					.ok()
					.contentLength(is.available())
					.header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"",
							attachmentManager.getValidFileName(attachment.getName())));
			// append media type, if it's filled
			if (StringUtils.isNotBlank(mimetype)) {
				response = response.contentType(MediaType.valueOf(attachment.getMimetype()));
			}
			//
			return response.body(new InputStreamResource(is));
		} catch (IOException e) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, e);
		}
	}
	
	/**
	 * Get configured application theme.
	 * 
	 * @return configured theme
	 * @since 12.0.0
	 */
	@ResponseBody
	@RequestMapping(value = "/application/theme", method = RequestMethod.GET)
	@Operation(
			summary = "Identity detail", 
			/* nickname = "getApplicationTheme", */ 
			/* response = ThemeDto.class, */ 
			tags = { IdmConfigurationController.TAG })
	public ResponseEntity<ThemeDto> getApplicationTheme(
			@Parameter(name = "Theme type.", required = false, example = "light")
			@RequestParam(name = "type", required = false) String themeType) {
		//
		// TODO: dark theme can be configured on BE - only for development on FE now only
		if (StringUtils.equalsIgnoreCase(themeType, "dark")) {
			LOG.warn("Theme [{}] cannot be configured on BE. Static configuration is on FE for development purpose only.", themeType);
			//
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		// not configured - ok, default will be used
		ThemeDto theme = applicationConfiguration.getApplicationTheme();
		if (theme == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		//
		return new ResponseEntity<>(theme, HttpStatus.OK);
	}
}
