package eu.bcvsolutions.idm.vs.dto;

import java.util.List;

import javax.validation.constraints.Size;

import javax.validation.constraints.NotEmpty;
import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.acc.dto.SysAttributeDifferenceDto;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO for account object in virtual system with marked changes (against specific VS request)
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "objects")
@ApiModel(description = "Account object in virtual system with marked changes (against specific VS request)")
public class VsConnectorObjectDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = true, notes = "Unique account identifier. UID on system and for connector.")
	private String uid;
	@ApiModelProperty(required = false, notes = "Object attributes with mark changes")
	private List<SysAttributeDifferenceDto> attributes;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public List<SysAttributeDifferenceDto> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<SysAttributeDifferenceDto> attributes) {
		this.attributes = attributes;
	}
}
