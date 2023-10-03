package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;

/**
 * Configuration item.
 * 
 * @author Radek Tomiška 
 */
@JsonInclude(Include.NON_NULL) // public configurations without auditable
@Relation(collectionRelation = "configurations")
public class IdmConfigurationDto extends AbstractDto implements Codeable {

	private static final long serialVersionUID = 1L;
	@NotNull
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String name;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String value;
	private boolean secured;
	private boolean confidential;

	public IdmConfigurationDto() {
	}
	
	public IdmConfigurationDto(UUID id) {
		super(id);
	}

	public IdmConfigurationDto(String name, String value) {
		this(name, value, false, false);
	}
	
	public IdmConfigurationDto(String name, String value, boolean secured, boolean confidential) {
		this.name = name;
		this.value = value;
		this.secured = secured;
		this.confidential = confidential;
	}

	/**
	 * Configuration property key
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	@Override
	public String getCode() {
		return getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Configuration property value
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Secured property is not readable without permission. Not secured configuration property is readable without authentication 
	 * 
	 * @return
	 */
	public boolean isSecured() {
		return secured;
	}

	public void setSecured(boolean secured) {
		this.secured = secured;
	}
	
	/**
	 * Secured negate alias
     *
	 * @return
	 */
	public boolean isPublic() {
		return !secured;
	}

	public void setPublic(boolean notSecured) {
		this.secured = !notSecured;
	}
	
	/**
	 * Confidential property - wil be saved in confidential storage
	 * 
	 * @return
	 */
	public boolean isConfidential() {
		return confidential;
	}
	
	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
		if (confidential) {
			this.secured = true;
		}
	}

}
