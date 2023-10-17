package eu.bcvsolutions.idm.acc.rest.impl;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.utils.CertificateUtils;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for AD connector wizard.
 *
 * @author Vít Švanda
 * @since 10.8.0
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/connector-types/ad-connector-type")
@Tag(
		name = AdUserConnectorTypeController.TAG,
		
		description = "Controller for AD connector wizard."//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class AdUserConnectorTypeController {
	
	protected static final String TAG = "AD Wizard";

	@Autowired
	private AttachmentManager attachmentManager;
	
	@ResponseBody
	@RequestMapping(value = "/{attachmentId}/download", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@Operation(
			summary = "Download public certificate",
			/* nickname = "downloadCertificate", */
			tags = {AdUserConnectorTypeController.TAG})
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_READ})
        }
    )
	public ResponseEntity<InputStreamResource> downloadCertificate(
			 @Parameter(description = "Attachment uuid identifier.", required = true)
			@PathVariable @NotNull String attachmentId) {

		IdmAttachmentDto attachmentDto = attachmentManager.get(UUID.fromString(attachmentId), IdmBasePermission.READ);
		if (attachmentDto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", attachmentId));
		}
		try {
			InputStream is = attachmentManager.getAttachmentData(attachmentDto.getId(), IdmBasePermission.READ);
			// Convert attachment to the PEM format (certificate can be in the DER format too)
			X509Certificate cert509 = CertificateUtils.getCertificate509(is);
			InputStream pemIs = CertificateUtils.certificateToPem(cert509);
			//
			String name = attachmentDto.getName();
			return ResponseEntity.ok()
					.contentLength(pemIs.available())
					.contentType(new MediaType("application", "x-pem-file"))
					.header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s.%s\"", name, "pem"))
					.body(new InputStreamResource(pemIs));
		} catch (Exception ex) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, ex);
		}
	}
}
