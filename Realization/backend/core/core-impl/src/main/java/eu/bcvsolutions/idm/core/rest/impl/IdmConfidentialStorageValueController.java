package eu.bcvsolutions.idm.core.rest.impl;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmConfidentialStorageValueDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConfidentialStorageValueFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmConfidentialStorageValueService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Confidential storage value controller
 * 
 * @author Patrik Stloukal
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/confidential-storage-values")
@Tag(
		name = IdmConfidentialStorageValueController.TAG,
		description = "Confidential storage value"//,

		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmConfidentialStorageValueController
		extends AbstractReadDtoController<IdmConfidentialStorageValueDto, IdmConfidentialStorageValueFilter> {

	protected static final String TAG = "Confidential storage value";

	@Autowired
	public IdmConfidentialStorageValueController(IdmConfidentialStorageValueService confidentialService) {
		super(confidentialService);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIDENTIAL_STORAGE_VALUE_READ + "')")
	@Operation(summary = "Search confidential storage value items (/search/quick alias)", /* nickname = "searchConfidentialStorageValue", */ tags = {
			IdmConfidentialStorageValueController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONFIDENTIAL_STORAGE_VALUE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONFIDENTIAL_STORAGE_VALUE_READ })
        }
    )
	public CollectionModel<?> find(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIDENTIAL_STORAGE_VALUE_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@Operation(summary = "Search confidential storage value items", /* nickname = "searchConfidentailStorageValue", */ tags = {
			IdmConfidentialStorageValueController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONFIDENTIAL_STORAGE_VALUE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONFIDENTIAL_STORAGE_VALUE_READ })
        }
    )
	public CollectionModel<?> findQuick(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIDENTIAL_STORAGE_VALUE_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(summary = "Confidential storage value item detail", /* nickname = "getConfidentailStorageValue", */ /* response = IdmConfidentialStorageValueDto.class, */ tags = {
			IdmConfidentialStorageValueController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONFIDENTIAL_STORAGE_VALUE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONFIDENTIAL_STORAGE_VALUE_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Item's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
}
