package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ScriptAuthorityType;

/**
 * DTO for script authority
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "scriptAuthorities")
public class IdmScriptAuthorityDto extends AbstractDto {
	
	private static final long serialVersionUID = 1L;
	
	private String service;
	private String className;
	@Embedded(dtoClass = IdmScriptDto.class)
	private UUID script;
	private ScriptAuthorityType type = ScriptAuthorityType.SERVICE;

	public String getService() {
		return service;
	}
	
	public String getClassName() {
		return className;
	}
	
	public UUID getScript() {
		return script;
	}
	
	public ScriptAuthorityType getType() {
		return type;
	}

	public void setService(String service) {
		this.service = service;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public void setScript(UUID script) {
		this.script = script;
	}
	
	public void setType(ScriptAuthorityType type) {
		this.type = type;
	}

}
