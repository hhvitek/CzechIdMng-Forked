package eu.bcvsolutions.idm.core.eav.api.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import org.springframework.hateoas.server.core.Relation;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Niceable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.UnmodifiableEntity;

/**
 * Form definition dto
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "formDefinitions")
public class IdmFormDefinitionDto extends AbstractDto implements UnmodifiableEntity, Niceable {

	private static final long serialVersionUID = 1L;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String type; // for entity / object type
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	private String code;
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	private String name;
	@NotNull
	private boolean main;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	@NotNull
	private boolean unmodifiable = false;
	@Size(max = DefaultFieldLengths.NAME)
	private String module;
	@Min(Short.MIN_VALUE)
	@Max(Short.MAX_VALUE)
	private Short seq;
	//
	// attribute definitions cache
	private List<IdmFormAttributeDto> formAttributes;
	private transient Map<UUID, IdmFormAttributeDto> mappedAttributes;
	private transient Map<String, Serializable> mappedKeys;

	public IdmFormDefinitionDto() {
	}
	
	public IdmFormDefinitionDto(UUID id) {
		super(id);
	}
	
	public IdmFormDefinitionDto(Auditable auditable) {
		super(auditable);
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMain() {
		return main;
	}

	public void setMain(boolean main) {
		this.main = main;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean isUnmodifiable() {
		return unmodifiable;
	}

	@Override
	public void setUnmodifiable(boolean unmodifiable) {
		this.unmodifiable = unmodifiable;
	}
	
	/**
	 * Returns defined form attributes. Returns empty list, when no attribute is defined.
	 * 
	 * @return
	 */
	public List<IdmFormAttributeDto> getFormAttributes() {
		if (formAttributes == null) {
			formAttributes = Lists.newArrayList();
		}
		return formAttributes;
	}

	public void setFormAttributes(List<IdmFormAttributeDto> formAttributes) {
		this.formAttributes = Lists.newArrayList(formAttributes);
		mappedAttributes = null; // refresh is needed
	}
	
	public void addFormAttribute(IdmFormAttributeDto formAttribute) {
		getFormAttributes().add(formAttribute);
		mappedAttributes = null; // refresh is needed
	}
	
	/**
	 * Remove form attribute from loaded attributes
	 * 
	 * @param formAttributeId
	 * @return
	 * @since 8.2.0
	 */
	@Beta
	public IdmFormAttributeDto removeFormAttribute(UUID formAttributeId) {
		IdmFormAttributeDto attribute = getMappedAttribute(formAttributeId);
		if (attribute != null) {
			formAttributes.remove(attribute);
			mappedAttributes.remove(formAttributeId);
			mappedKeys.remove(attribute.getCode());
		}
		return attribute;
	}
	

	/**
	 * Returns defined attributes as map
	 * 
	 * @return
	 */
	private Map<UUID, IdmFormAttributeDto> getMappedAttributes() {
		if (mappedAttributes == null || mappedKeys == null) {
			List<IdmFormAttributeDto> attributes = getFormAttributes();
			mappedAttributes = new HashMap<>(attributes.size());
			mappedKeys = new HashMap<>(attributes.size());
			for (IdmFormAttributeDto attribute : attributes) {
				mappedAttributes.put(attribute.getId(), attribute);
				mappedKeys.put(attribute.getCode(), attribute.getId());
			}
		}
		return mappedAttributes;
	}

	/**
	 * Return defined attributes by <name, id>
	 * 
	 * @return
	 */
	private Map<String, Serializable> getMappedNames() {
		if (mappedAttributes == null || mappedKeys == null) {
			getMappedAttributes();
		}
		return mappedKeys;
	}

	/**
	 * Returns attribute definition by identifier
	 *
	 * @param formAttributeId
	 * @return
	 */
	public IdmFormAttributeDto getMappedAttribute(UUID formAttributeId) {
		return getMappedAttributes().get(formAttributeId);
	}
	
	/**
	 * Returns attribute definition by code
	 *
	 * @param attributeCode
	 * @return
	 */
	public IdmFormAttributeDto getMappedAttributeByCode(String attributeCode) {
		if (!getMappedNames().containsKey(attributeCode)) {
			return null;
		}
		return getMappedAttributes().get(getMappedNames().get(attributeCode));
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}
	
	@Override
	public String getNiceLabel() {
		return getCode();
	}
	
	/**
	 * Order on FE form.
	 * 
	 * @return order
	 * @since 11.1.0
	 */
	public Short getSeq() {
		return seq;
	}

	/**
	 * Order on FE form.
	 * 
	 * @param seq order
	 * @since 11.1.0
	 */
	public void setSeq(Short seq) {
		this.seq = seq;
	}
}
