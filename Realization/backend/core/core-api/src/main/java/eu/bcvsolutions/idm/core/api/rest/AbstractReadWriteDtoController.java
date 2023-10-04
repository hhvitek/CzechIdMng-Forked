package eu.bcvsolutions.idm.core.api.rest;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidatorFactory;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.domain.RequestConfiguration;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

/**
 * CRUD operations for DTO.
 * 
 * @see DataFilter
 * @param <DTO> dto type
 * @param <F> filter type
 * @author Svanda
 * @author Radek Tomiška
 */
public abstract class AbstractReadWriteDtoController<DTO extends BaseDto, F extends BaseFilter>
		extends AbstractReadDtoController<DTO, F> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractReadWriteDtoController.class);
	//
	@Autowired(required = false) // optional dependency for support automatic JSR303 validations
	private ValidatorFactory validatorFactory;
	@Autowired
	private RequestConfiguration requestConfiguration;
	@Autowired
	private ModelMapper modelMapper;

	public AbstractReadWriteDtoController(ReadWriteDtoService<DTO, F> entityService) {
		super(entityService);
	}
	
	/**
	 * Invokes JSR 303 validations programmatically (its needed for patch method).
	 * Its called before save method. 
	 * Validations are invoked in service layer too, but wee want to shorten processing.
	 *  
	 * @param dto
	 * @return
	 */
	protected DTO validateDto(DTO dto) {
		if (validatorFactory == null) {
			LOG.debug("JSR303 Validation are disabled. Configure validation factory properly.");
			return dto;
		}
		Set<ConstraintViolation<DTO>> errors = validatorFactory.getValidator().validate(dto);
		if(!errors.isEmpty()) {
			throw new ConstraintViolationException("Validation failed for dto [" + getDtoClass().getSimpleName() +"]", errors);
		}
		return dto;
	}
	
	/**
	 * Save (Creates / updates) given DTO - are called by putDto / postDto / patchDto methods
	 * 
	 * @param dto
	 * @return
	 */
	public DTO saveDto(DTO dto, BasePermission... permission) {
		Assert.notNull(dto, "DTO is required.");
		//
		if (this.isRequestModeEnabled()) {
			throw new ResultCodeException(CoreResultCode.REQUEST_CUD_OPERATIONS_NOT_ALLOWED,
					ImmutableMap.of("controller", this.getClass().getSimpleName()));
		}
		//
		return getService().save(validateDto(dto), permission);
	}

	/**
	 * Post DTO and convert to response
	 * 
	 * @param dto
	 * @return
	 */
	@ApiOperation(value = "Create / update record", authorizations = { 
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public ResponseEntity<?> post(@ApiParam(value = "Record (dto).", required = true) DTO dto) {
		RepresentationModel resource = toModel(postDto(dto));
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(resource, HttpStatus.CREATED);
	}

	/**
	 * Creates / updates given DTO.
	 * 
	 * @param dto
	 * @return
	 */
	public DTO postDto(DTO dto) {
		return saveDto(dto, getService().isNew(dto) ? IdmBasePermission.CREATE : IdmBasePermission.UPDATE);
	}

	/**
	 * Update DTO by given backendId and convert to response
	 * 
	 * @param backendId
	 * @param dto
	 * @return
	 */
	@ApiOperation(value = "Update record", authorizations = { 
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public ResponseEntity<?> put(
			@ApiParam(value = "Record's uuid identifier or unique code, if record supports <pre>Codeable</pre> interface.", required = true) 
			String backendId,
			@ApiParam(value = "Record (dto).", required = true) DTO dto) {
		DTO updateDto = getDto(backendId);
		if (updateDto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		DTO updatedDto = putDto(dto);
		return new ResponseEntity<>(toModel(updatedDto), HttpStatus.OK);
	}

	/**
	 * Updates given DTO
	 * 
	 * @param dto
	 * @return
	 */
	public DTO putDto(DTO dto) {		
		return saveDto(dto, IdmBasePermission.UPDATE);
	}

	/**
	 * Patch is not implemented yet
	 * 
	 * @param backendId
	 * @param nativeRequest
	 * @return
	 * @throws HttpMessageNotReadableException
	 */
	public ResponseEntity<?> patch(String backendId, HttpServletRequest nativeRequest) 
			throws HttpMessageNotReadableException {
		DTO updateDto = getDto(backendId);
		if (updateDto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		//
		ServletServerHttpRequest request = new ServletServerHttpRequest(nativeRequest);
		try {
			modelMapper.map(getMapper().readerForUpdating(updateDto).readValue(request.getBody()), updateDto);
		} catch (IOException ex) {
			throw new ResultCodeException(CoreResultCode.BAD_REQUEST, ex);
		}
		updateDto = patchDto(updateDto);
		return new ResponseEntity<>(toModel(updateDto), HttpStatus.OK);
	}
	
	/**
	 * Patch given DTO
	 * 
	 * @param dto
	 * @return
	 */
	public DTO patchDto(DTO dto) {
		return saveDto(dto, IdmBasePermission.UPDATE);
	}

	/**
	 * Deletes DTO by given id
	 * 
	 * @param backendId
	 * @return
	 */
	@ApiOperation(value = "Delete record", authorizations = { 
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Record's uuid identifier or unique code, if record supports <pre>Codeable</pre> interface.", required = true)
			String backendId) {
		DTO dto = getDto(backendId);
		if (dto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		deleteDto(dto);
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Deletes given DTO
	 * 
	 * @param backendId
	 * @return
	 */
	public void deleteDto(DTO dto) {
		Assert.notNull(dto, "DTO is required.");
		//
		if (this.isRequestModeEnabled()) {
			throw new ResultCodeException(CoreResultCode.REQUEST_CUD_OPERATIONS_NOT_ALLOWED,
					ImmutableMap.of("controller", this.getClass().getSimpleName()));
		}
		//
		getService().delete(dto, IdmBasePermission.DELETE);
	}

	/**
	 * Returns DTO service configured to current controller
	 */
	@Override
	protected ReadWriteDtoService<DTO, F> getService() {
		return (ReadWriteDtoService<DTO, F>) super.getService();
	}
	
	@Override
	protected DTO checkAccess(DTO dto, BasePermission... permission) {
		// If controller supports request, then only READ operation is allowed
		if (this.isRequestModeEnabled()) {
			if(permission != null && permission.length == 1 && IdmBasePermission.READ == permission[0]) {
				return super.checkAccess(dto, permission);
			}
			throw new ResultCodeException(CoreResultCode.REQUEST_CUD_OPERATIONS_NOT_ALLOWED,
					ImmutableMap.of("controller", this.getClass().getSimpleName()));
		}
		return super.checkAccess(dto, permission);
	}
	
	/**
	 * If return true, then controller supports requests and cannot be used for CUD
	 * operations. For CUD operations should be using the request controller.
	 * 
	 * @return
	 */
	protected boolean isRequestModeEnabled() {
		return requestConfiguration.isRequestModeEnabled(this.getDtoClass());
	}
	
}
