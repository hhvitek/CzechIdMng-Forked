package eu.bcvsolutions.idm.core.model.repository.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;

import eu.bcvsolutions.idm.security.service.SecurityService;

/**
 * Retrieve auditor for auditable entity
 * 
 * @author Radek Tomiška 
 */
public class UsernameAuditor implements AuditorAware<String> {
	
	@Autowired
	private SecurityService securityService;

	@Override
	public String getCurrentAuditor() {
	    String username = securityService.getUsername();
	    return StringUtils.isEmpty(username) ? "[GUEST]" : username;
	}

}