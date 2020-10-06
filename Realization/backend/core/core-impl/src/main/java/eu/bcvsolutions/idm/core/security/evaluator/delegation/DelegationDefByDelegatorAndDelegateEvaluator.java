package eu.bcvsolutions.idm.core.security.evaluator.delegation;

import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegationDefinition;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * Allows create (depends on the settings) delegation if logged users has IdentityBasePermission.DELEGATOR for delegator and IdentityBasePermission.DELEGATE for delegate.
 *
 * @author Vít Švanda
 */
@Component
@Description("Allows create (depends on the settings) delegation if logged users has IdentityBasePermission.DELEGATOR for delegator and IdentityBasePermission.DELEGATE for delegate.")
public class DelegationDefByDelegatorAndDelegateEvaluator extends AbstractAuthorizationEvaluator<IdmDelegationDefinition> {

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private SecurityService securityService;

	@Override
	public Set<String> getPermissions(IdmDelegationDefinition entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || !securityService.isAuthenticated() || entity.getDelegator() == null || entity.getDelegate() == null) {
			return permissions;
		}

		Set<String> permissionsDelegator = identityService.getPermissions(entity.getDelegator());
		Set<String> permissionsDelegate = identityService.getPermissions(entity.getDelegate());

		if (permissionsDelegator.contains(IdentityBasePermission.DELEGATOR.getName())
				&& permissionsDelegate.contains(IdentityBasePermission.DELEGATE.getName())) {
			permissions.addAll(policy.getPermissions());
		}
		return permissions;
	}
}
