package eu.bcvsolutions.idm.core.api.dto;

import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;

/**
 * DTO for script.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "scripts")
public class IdmScriptDto extends AbstractDto implements RecoverableDto {

	private static final long serialVersionUID = 1L;
	
	private String code;
	private String name;
	private String script;
	private IdmScriptCategory category = IdmScriptCategory.DEFAULT;
	private String description;
	private String template;
	
	public String getName() {
		return name;
	}
	
	public String getScript() {
		return script;
	}
	
	public IdmScriptCategory getCategory() {
		return category;
	}
	
	public String getDescription() {
		return description;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setScript(String script) {
		this.script = script;
	}
	
	public void setCategory(IdmScriptCategory category) {
		this.category = category;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}
}
