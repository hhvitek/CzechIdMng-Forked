package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

/**
 * Common password filtering
 * 
 * @author Jirka Koula
 *
 */
public abstract class AbstractPasswordFilter extends QuickFilter {

    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public abstract UUID getEntityId();

    public abstract void setEntityId(UUID entityId);
    
}
