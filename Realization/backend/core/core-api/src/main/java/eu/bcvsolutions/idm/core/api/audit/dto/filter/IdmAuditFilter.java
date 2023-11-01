package eu.bcvsolutions.idm.core.api.audit.dto.filter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;

/**
 * Filter for audit.
 * Filter must have only interface base filter is not
 * {@link DataFilter} because audit has long ID.
 * 
 * TODO: If DataFilter will be used, add ownerIdType final string property (used on FE only).
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdmAuditFilter implements BaseFilter {

    private Long id;
    private String text;
    private List<String> types; // entity class canonical name
    private ZonedDateTime from;
    private ZonedDateTime till;
    private String modification;
    private String modifier;
    private List<String> changedAttributesList;
    private UUID entityId;
    private Boolean withVersion;
    private String ownerId;
    private List<String> ownerIds;
	private String ownerCode;
	private String ownerType;
	private String subOwnerId;
	private String subOwnerCode;
	private String subOwnerType;
	private UUID transactionId;
	private UUID relatedOwnerId; // entityId + ownerId + subOwnerId

	public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getModification() {
        return modification;
    }

    public void setModification(String modification) {
        this.modification = modification;
    }

    /**
     * Entity class canonical name.
     * 
     * @return class canonical name
     */
    public String getType() {
    	if (CollectionUtils.isEmpty(types)) {
    		return null;
    	}
        return types.get(0);
    }

    /**
     * Entity class canonical name.
     * 
     * @param type class canonical name.
     */
    public void setType(String type) {
    	if (StringUtils.isEmpty(type)) {
    		types = null;
    	}
        types = Lists.newArrayList(type);
    }
    
    /**
     * Entity class canonical name (OR).
     * 
     * @param types Entity class canonical name OR
     * @since 12.0.0
     */
    public void setTypes(List<String> types) {
		this.types = types;
	}
    
    /**
     * Entity class canonical name (OR).
     * 
     * @return Entity class canonical name OR
     * @since 12.0.0
     */
    public List<String> getTypes() {
		return types;
	}

    public ZonedDateTime getFrom() {
        return from;
    }

    public void setFrom(ZonedDateTime from) {
        this.from = from;
    }

    public ZonedDateTime getTill() {
        return till;
    }

    public void setTill(ZonedDateTime till) {
        this.till = till;
    }

    /**
     * @since 9.5.0
     * @return
     */
    public List<String> getChangedAttributesList() {
		return changedAttributesList;
	}

    /**
     * @since 9.5.0
     * @param changedAttributesList
     */
	public void setChangedAttributesList(List<String> changedAttributesList) {
		this.changedAttributesList = changedAttributesList;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerCode() {
		return ownerCode;
	}

	public void setOwnerCode(String ownerCode) {
		this.ownerCode = ownerCode;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public String getSubOwnerId() {
		return subOwnerId;
	}

	public void setSubOwnerId(String subOwnerId) {
		this.subOwnerId = subOwnerId;
	}

	public String getSubOwnerCode() {
		return subOwnerCode;
	}

	public void setSubOwnerCode(String subOwnerCode) {
		this.subOwnerCode = subOwnerCode;
	}

	public String getSubOwnerType() {
		return subOwnerType;
	}

	public void setSubOwnerType(String subOwnerType) {
		this.subOwnerType = subOwnerType;
	}

	public List<String> getOwnerIds() {
		return ownerIds;
	}

	public void setOwnerIds(List<String> ownerIds) {
		this.ownerIds = ownerIds;
	}

	public Boolean getWithVersion() {
		return withVersion;
	}

	public void setWithVersion(Boolean withVersion) {
		this.withVersion = withVersion;
	}
	
	/**
	 * Returns batch transaction id (entity was created or modified in given transaction).
	 * 
	 * @return
	 * @since 9.7.0
	 */
	public UUID getTransactionId() {
		return transactionId;
	}

	/**
	 * Sets batch transaction id (entity was created or modified in given transaction).
	 * 
	 * @param transactionId
	 * @since 9.7.0
	 */
	public void setTransactionId(UUID transactionId) {
		this.transactionId = transactionId;
	}
	
	/**
	 * entityId OR ownerId OR subOwnerId
	 * 
	 * @return uuid
	 * @since 12.0.0
	 */
	public UUID getRelatedOwnerId() {
		return relatedOwnerId;
	}
	
	/**
	 * entityId OR ownerId OR subOwnerId
	 * 
	 * @param relatedOwnerId uuid
	 * @since 12.0.0
	 */
	public void setRelatedOwnerId(UUID relatedOwnerId) {
		this.relatedOwnerId = relatedOwnerId;
	}
	
}
