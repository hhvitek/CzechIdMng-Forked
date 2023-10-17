package eu.bcvsolutions.idm.core.workflow.rest;

import java.io.IOException;
import java.io.InputStream;

import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Rest controller for workflow historic instance processes
 * 
 * TODO: secure endpoints
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/workflow-history-processes")
@Tag(
		name = WorkflowHistoricProcessInstanceController.TAG,  

		description = "Read WF audit"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE

//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class WorkflowHistoricProcessInstanceController extends AbstractReadDtoController<WorkflowHistoricProcessInstanceDto, WorkflowFilterDto> {

	protected static final String TAG = "Workflow - process instances history";

	private WorkflowHistoricProcessInstanceService workflowHistoricProcessInstanceService;

	@Autowired
	public WorkflowHistoricProcessInstanceController(
			WorkflowHistoricProcessInstanceService service) {
		super(service);
		//
		this.workflowHistoricProcessInstanceService = service;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/search/quick")
	@Operation(
			summary = "Search historic process instances",
			/* nickname = "searchQuickHistoricProcessInstances", */ 
			tags = { WorkflowHistoricProcessInstanceController.TAG })
	@PageableAsQueryParam
	public CollectionModel<?> searchQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return find(parameters, pageable);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
	@Operation(
			summary = "Historic process instance detail",
			/* nickname = "getHistoricProcessInstance", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = WorkflowHistoricProcessInstanceDto.class
                                    )
                            )
                    }
            ),
			tags = { WorkflowHistoricProcessInstanceController.TAG })
	public ResponseEntity<?> get(
			 @Parameter(description = "Historic process instance id.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	/**
	 * Generate process instance diagram image
	 * 
	 * @param historicProcessInstanceId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/diagram", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	@Operation(
			summary = "Historic process instance diagram",
			/* nickname = "getHistoricProcessInstanceDiagram", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = WorkflowHistoricProcessInstanceDto.class
                                    )
                            )
                    }
            ),
			tags = { WorkflowHistoricProcessInstanceController.TAG })
	public ResponseEntity<InputStreamResource> getDiagram(
			 @Parameter(description = "Historic process instance id.", required = true)
			@PathVariable @NotNull String backendId) {
		// check rights
		WorkflowHistoricProcessInstanceDto result = workflowHistoricProcessInstanceService.get(backendId);
		if (result == null) {
			throw new ResultCodeException(CoreResultCode.FORBIDDEN);
		}
		InputStream is = workflowHistoricProcessInstanceService.getDiagram(backendId);
		try {
			return ResponseEntity.ok().contentLength(is.available()).contentType(MediaType.IMAGE_PNG)
					.body(new InputStreamResource(is));
		} catch (IOException e) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, e);
		}
	}

}
