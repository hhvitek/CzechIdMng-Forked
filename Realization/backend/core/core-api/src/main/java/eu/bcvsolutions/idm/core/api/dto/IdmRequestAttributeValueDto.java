package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;

import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for attribute value with marked type of change
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "attributes")
@Schema(description = "Attribute value with marked type of change")
public class IdmRequestAttributeValueDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Object value;
	private Object oldValue;
	@Schema(required = false, description = "Type of value change")
	private RequestOperationType change;

	public IdmRequestAttributeValueDto() {
	}

	public IdmRequestAttributeValueDto(Object value, Object oldValue, RequestOperationType type) {
		this.value = value;
		this.oldValue = oldValue;
		this.change = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public RequestOperationType getChange() {
		return change;
	}

	public void setChange(RequestOperationType change) {
		this.change = change;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

}
