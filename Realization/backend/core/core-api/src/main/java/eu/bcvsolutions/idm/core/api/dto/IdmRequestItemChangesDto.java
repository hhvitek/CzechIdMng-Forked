package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for request item with marked changed values
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "items")
@Schema(description = "Request item with marked changed values")
public class IdmRequestItemChangesDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Request item")
	private IdmRequestItemDto requestItem;
	@Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Object attributes with mark changes")
	private List<IdmRequestItemAttributeDto> attributes;

	public IdmRequestItemDto getRequestItem() {
		return requestItem;
	}

	public void setRequestItem(IdmRequestItemDto requestItem) {
		this.requestItem = requestItem;
	}

	public List<IdmRequestItemAttributeDto> getAttributes() {
		if(attributes == null) {
			attributes = new ArrayList<>();
		}
		return attributes;
	}

	public void setAttributes(List<IdmRequestItemAttributeDto> attributes) {
		this.attributes = attributes;
	}
}
