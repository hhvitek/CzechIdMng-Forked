package eu.bcvsolutions.idm.core.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.bcvsolutions.idm.core.api.dto.ApplicantImplDto.Converter;
import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;

/**
 * Dto for role request
 *
 * @author svandav
 */
@Relation(collectionRelation = "roleRequests")
public class IdmRoleRequestDto extends AbstractDto implements Loggable {

    private static final long serialVersionUID = 1L;
    public static final String WF_PROCESS_FIELD = "wfProcessId";
    public static final String APPLICANT_INFO_FIELD = "applicantInfo";

    private UUID applicant;

    @JsonDeserialize(converter = Converter.class)
    private ApplicantDto applicantInfo;
    private RoleRequestState state;
    private RoleRequestedByType requestedByType;
    //In embedded map, is under wfProcessId key actual task - WorkflowProcessInstanceDto.class
    private String wfProcessId;
    private String originalRequest;
    private List<AbstractConceptRoleRequestDto> conceptRoles;
    private boolean executeImmediately = false;
    @Embedded(dtoClass = IdmRoleRequestDto.class)
    private UUID duplicatedToRequest;
    private String log;
    @Size(max = DefaultFieldLengths.DESCRIPTION)
    private String description;
    private OperationResultDto systemState;
    private Set<IdmIdentityDto> approvers;

    public RoleRequestState getState() {
        return state;
    }

    public void setState(RoleRequestState state) {
        this.state = state;
    }

    public String getWfProcessId() {
        return wfProcessId;
    }

    public void setWfProcessId(String wfProcessId) {
        this.wfProcessId = wfProcessId;
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    public void setOriginalRequest(String originalRequest) {
        this.originalRequest = originalRequest;
    }

    public UUID getApplicant() {
        return applicant;
    }

    public void setApplicant(UUID applicant) {
        this.applicant = applicant;
    }

    public ApplicantDto getApplicantInfo() {
        if (applicantInfo == null && applicant != null) {
            // fallback for backwards compatibility
            applicantInfo = new ApplicantImplDto(applicant, IdmIdentityDto.class.getCanonicalName());
        }
        return applicantInfo;
    }

    public void setApplicantInfo(ApplicantDto applicantInfo) {
        this.applicantInfo = applicantInfo;
    }

    public List<AbstractConceptRoleRequestDto> getConceptRoles() {
        if (conceptRoles == null) {
            conceptRoles = new ArrayList<>();
        }
        return conceptRoles;
    }

    public void setConceptRoles(List<AbstractConceptRoleRequestDto> conceptRoles) {
        this.conceptRoles = conceptRoles;
    }

    public UUID getDuplicatedToRequest() {
        return duplicatedToRequest;
    }

    public void setDuplicatedToRequest(UUID duplicatedToRequest) {
        this.duplicatedToRequest = duplicatedToRequest;
    }

    public boolean isExecuteImmediately() {
        return executeImmediately;
    }

    public void setExecuteImmediately(boolean executeImmediately) {
        this.executeImmediately = executeImmediately;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public RoleRequestedByType getRequestedByType() {
        return requestedByType;
    }

    public void setRequestedByType(RoleRequestedByType requestedByType) {
        this.requestedByType = requestedByType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public OperationResultDto getSystemState() {
		return systemState;
	}

	public void setSystemState(OperationResultDto systemState) {
		this.systemState = systemState;
	}

	@Override
    public String addToLog(String text) {
        if (text != null) {
            StringBuilder builder = new StringBuilder();
            if (this.log != null) {
                builder.append(this.log);
                builder.append("\n" + LOG_SEPARATOR + "\n");
            }
            builder.append(text);
            this.setLog(builder.toString());
        }
        return this.getLog();
    }

	public Set<IdmIdentityDto> getApprovers() {
		return approvers;
	}

	public void setApprovers(Set<IdmIdentityDto> approvers) {
		this.approvers = approvers;
	}
}