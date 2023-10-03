package eu.bcvsolutions.idm.core.api.dto;

import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationEvaluator;

/**
 * Assign authorization evaluator to role.
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "authorizationPolicies")
public class IdmAuthorizationPolicyDto extends AbstractDto implements AuthorizationPolicy, Requestable, Disableable {

	private static final long serialVersionUID = 1515971437827128049L;

	@NotNull
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role;
	@NotNull
	private boolean disabled;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	@Min(Short.MIN_VALUE)
	@Max(Short.MAX_VALUE)
	private Short seq;
	@Size(max = DefaultFieldLengths.NAME)
	private String groupPermission;
	@Size(max = DefaultFieldLengths.NAME)
	private String authorizableType;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String evaluatorType;
	private ConfigurationMap evaluatorProperties;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String basePermissions;
	@Embedded(dtoClass = IdmRequestItemDto.class)
	private UUID requestItem; // Isn't persist in the entity

	public IdmAuthorizationPolicyDto() {
	}

	public IdmAuthorizationPolicyDto(UUID id) {
		super(id);
	}

	public UUID getRole() {
		return role;
	}

	public void setRole(UUID role) {
		this.role = role;
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Short getSeq() {
		return seq;
	}

	public void setSeq(Short seq) {
		this.seq = seq;
	}

	@Override
	public String getEvaluatorType() {
		return evaluatorType;
	}

	public void setEvaluatorType(String evaluatorType) {
		this.evaluatorType = evaluatorType;
	}

	public void setEvaluator(Class<? extends AuthorizationEvaluator<?>> evaluator) {
		if (evaluator == null) {
			this.evaluatorType = null;
		} else {
			this.evaluatorType = evaluator.getCanonicalName();
		}
	}

	public void setAuthorizableType(String authorizableType) {
		this.authorizableType = authorizableType;
	}

	@Override
	public String getAuthorizableType() {
		return authorizableType;
	}

	public void setEvaluatorProperties(ConfigurationMap evaluatorProperties) {
		this.evaluatorProperties = evaluatorProperties;
	}

	@Override
	public ConfigurationMap getEvaluatorProperties() {
		if (evaluatorProperties == null) {
			evaluatorProperties = new ConfigurationMap();
		}
		return evaluatorProperties;
	}

	@Override
	public String getBasePermissions() {
		return basePermissions;
	}

	public void setBasePermissions(String basePermissions) {
		this.basePermissions = basePermissions;
	}

	@JsonIgnore
	public void setPermissions(BasePermission... permissions) {
		if (permissions == null) {
			this.basePermissions = null;
		} else {
			this.basePermissions = StringUtils.join(permissions, ',');
		}
	}

	@JsonIgnore
	@Override
	public Set<String> getPermissions() {
		return AuthorizationPolicy.super.getPermissions();
	}

	@Override
	public String getGroupPermission() {
		return groupPermission;
	}

	public void setGroupPermission(String groupPermission) {
		this.groupPermission = groupPermission;
	}
	
	@Override
	public UUID getRequestItem() {
		return requestItem;
	}

	@Override
	public void setRequestItem(UUID requestItem) {
		this.requestItem = requestItem;
	}
}
