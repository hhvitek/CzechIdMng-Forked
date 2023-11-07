package eu.bcvsolutions.idm.acc.dto;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.dto.AbstractPasswordValidationDto;

/**
 * DTO for account password validation.
 *
 * @author Jirka Koula
 */
public class AccPasswordValidationDto extends AbstractPasswordValidationDto<AccAccountDto> implements Serializable {
    private static final long serialVersionUID = 5422443380932424940L;
    //
    private AccAccountDto account; // password owner

    public AccAccountDto getAccount() {
        return account;
    }

    public void setAccount(AccAccountDto account) {
        this.account = account;
    }

	@Override
	public AccAccountDto getEntity() { return getAccount(); }
}
