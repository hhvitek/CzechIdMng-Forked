package eu.bcvsolutions.idm.core.workflow.rest;

import java.io.IOException;
import java.io.InputStream;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.workflow.api.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.core.workflow.api.service.WorkflowDeploymentService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Rest controller for workflow task instance
 * 
 * @author svandav
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/workflow-definitions")
@Tag(name = WorkflowDefinitionController.TAG, description = "WF definition administration")
public class WorkflowDefinitionController extends AbstractReadDtoController<WorkflowProcessDefinitionDto, WorkflowFilterDto> {

	protected static final String TAG = "Workflow - definitions";
	//
	private final WorkflowDeploymentService deploymentService;
	private final WorkflowProcessDefinitionService definitionService;
	
	@Autowired
	public WorkflowDefinitionController(WorkflowProcessDefinitionService service, WorkflowDeploymentService deploymentService) {
		super(service);
		//
		Assert.notNull(deploymentService, "Service is required.");
		//
		this.definitionService = service;
		this.deploymentService = deploymentService;
	}

	/**
	 * Search all last version and active process definitions
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_DEFINITION_READ + "')")
	@Operation(
			summary = "Get all definitions", 
			operationId = "findAllWorkflowDefinitions",
			tags = { WorkflowDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.IDENTITY_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.IDENTITY_READ })
    })
	public CollectionModel<?> findAllProcessDefinitions() {
		return toCollectionModel(definitionService.findAllProcessDefinitions(), getDtoClass());
	}

	/**
	 * Search last version and active process definitions. Use quick search api.
	 *
	 * @param size
	 * @param page
	 * @param sort
	 * @param text
	 *            - category
	 * @return
	 */
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_DEFINITION_READ + "')")
	@Operation(
			summary = "Search definitions",
			operationId = "searchQuickWorkflowDefinitions",
			tags = { WorkflowDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.IDENTITY_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.IDENTITY_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

    /**
     * Search last version process by key
     *
     * @param definitionKey
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
    @PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_DEFINITION_READ + "')")
    @Operation(
            summary = "Workflow definition detail",
            operationId = "getWorkflowDefinition",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = WorkflowProcessDefinitionDto.class
                                    )
                            )
                    }
            ),
            tags = { WorkflowDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.WORKFLOW_DEFINITION_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.WORKFLOW_DEFINITION_READ })
    })
    public ResponseEntity<WorkflowProcessDefinitionDto> get(
            @Parameter(description = "Workflow definition key.", required = true)
            @PathVariable String backendId) {
        String definitionId = definitionService.getProcessDefinitionId(backendId);
        return (ResponseEntity<WorkflowProcessDefinitionDto>) super.get(definitionId);
    }

	/**
	 * Upload new deployment to Activiti engine
	 *
	 * @param name
	 * @param fileName
	 * @param data
	 * @return
	 * @throws IOException
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_DEFINITION_CREATE + "') or hasAuthority('" + CoreGroupPermission.WORKFLOW_DEFINITION_UPDATE + "')")
	@Operation(
			summary = "Create / update workflow definition",
			operationId = "postWorkflowDefinition",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = WorkflowDeploymentDto.class
                                    )
                            )
                    }
            ),
			tags = { WorkflowDefinitionController.TAG },
			description = "Upload new deployment to Activiti engine."
					+ " If definition with iven key exists, new deployment version is added."
					+ " All running task instances process with their deployment version."
					+ " Newly added version will be used for new instances.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                    CoreGroupPermission.WORKFLOW_DEFINITION_CREATE,
                    CoreGroupPermission.WORKFLOW_DEFINITION_UPDATE}),
            @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                    CoreGroupPermission.WORKFLOW_DEFINITION_CREATE,
                    CoreGroupPermission.WORKFLOW_DEFINITION_UPDATE})
        }
    )
	public EntityModel<WorkflowDeploymentDto> post(String name, String fileName, MultipartFile data)
			throws IOException {
		WorkflowDeploymentDto deployment = deploymentService.create(name, fileName, data.getInputStream());
		Link selfLink = WebMvcLinkBuilder.linkTo(this.getClass()).slash(deployment.getId()).withSelfRel();
		return new EntityModel<WorkflowDeploymentDto>(deployment, selfLink);
	}

	/**
     * Download deployment to Activiti engine
	 *
	 * @return ResponseEntity<InputStreamResource>
	 */
	@ResponseBody
    @RequestMapping(value = "/{backendId}/definition", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_DEFINITION_READ + "')")
	@Operation(
			summary = "Download workflow definition",
			operationId = "getWorkflowDefinition",
			tags = {WorkflowDefinitionController.TAG},
						description = "Return XML file with definition.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.WORKFLOW_DEFINITION_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.WORKFLOW_DEFINITION_READ})
        }
    )
    public ResponseEntity<InputStreamResource> getProcessDefinition(@PathVariable String backendId) {
		WorkflowProcessDefinitionDto result = definitionService.getByName(backendId);
		if (result == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}

		try {
            InputStream inputXMLStream = definitionService.getBpmnDefinition(backendId);
			return ResponseEntity
					.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", result.getResourceName()))
					.contentLength(inputXMLStream.available())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(new InputStreamResource(inputXMLStream));
		} catch (Exception e) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, e);
		}
	}

	/**
	 * Generate process definition diagram image
	 *
	 * @param definitionKey
	 * @return
	 */
	@RequestMapping(value = "/{backendId}/diagram", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_DEFINITION_READ + "')")
	@Operation(
			summary = "Workflow definition diagram",
			operationId = "getWorkflowDefinitionDiagram",
			tags = { WorkflowDefinitionController.TAG },
						description = "Returns input stream to definition's diagram.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.WORKFLOW_DEFINITION_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.WORKFLOW_DEFINITION_READ })
    })
	public ResponseEntity<InputStreamResource> getDiagram(
			 @Parameter(description = "Workflow definition key.", required = true)
			@PathVariable String backendId) {
		// check rights
		WorkflowProcessDefinitionDto result = definitionService.getByName(backendId);
		if (result == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		InputStream is = definitionService.getDiagramByKey(backendId);
		try {
			return ResponseEntity.ok().contentLength(is.available()).contentType(MediaType.IMAGE_PNG)
					.body(new InputStreamResource(is));
		} catch (IOException e) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, e);
		}
	}

}
