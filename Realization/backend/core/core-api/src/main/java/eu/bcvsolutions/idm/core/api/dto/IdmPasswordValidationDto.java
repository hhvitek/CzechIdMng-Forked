package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * DTO for identity password validation.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
public class IdmPasswordValidationDto extends AbstractPasswordValidationDto<IdmIdentityDto> implements Serializable {

    private static final long serialVersionUID = 5422443380932424940L;
    //
    private IdmIdentityDto identity; // password owner
    /**
     * Validation to check minimum days, before password can be changed again will be enforced, 
     * even when password is changed under different identity (e.g. from password filter).
     * 
     * @since 11.0.0
     */
    @JsonIgnore
	private boolean enforceMinPasswordAgeValidation = false;

    public IdmIdentityDto getIdentity() {
        return identity;
    }

    public void setIdentity(IdmIdentityDto identity) {
        this.identity = identity;
    }

    /**
     * Validation to check minimum days, before password can be changed again will be enforced, 
     * even when password is changed under different identity except admin (e.g. from password filter).
     * 
     * @return true => validated
     * @since 11.0.0
     */
    public boolean isEnforceMinPasswordAgeValidation() {
		return enforceMinPasswordAgeValidation;
	}
    
    /**
     * Validation to check minimum days, before password can be changed again will be enforced, 
     * even when password is changed under different identity except admin (e.g. from password filter).
     * 
     * @param enforceMinPasswordAgeValidation true => validated
     * @since 11.0.0
     */
    public void setEnforceMinPasswordAgeValidation(boolean enforceMinPasswordAgeValidation) {
		this.enforceMinPasswordAgeValidation = enforceMinPasswordAgeValidation;
	}

	@Override
	public IdmIdentityDto getEntity() { return getIdentity(); }
}
