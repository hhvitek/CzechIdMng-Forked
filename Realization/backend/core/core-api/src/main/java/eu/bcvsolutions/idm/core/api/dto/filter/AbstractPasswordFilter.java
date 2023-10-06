package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Common password filtering
 * 
 * @author Jirka Koula
 *
 */
public abstract class AbstractPasswordFilter extends QuickFilter {

    private String password;
    private LocalDate validTill;
    private LocalDate validFrom;
    private Boolean mustChange;

    public LocalDate getValidTill() {
        return validTill;
    }

    public void setValidTill(LocalDate validTill) {
        this.validTill = validTill;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getMustChange() {
        return mustChange;
    }

    public void setMustChange(Boolean mustChange) {
        this.mustChange = mustChange;
    }

    public abstract UUID getEntityId();

    public abstract void setEntityId(UUID entityId);
    
}
