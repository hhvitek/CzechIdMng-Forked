package eu.bcvsolutions.idm.acc.security.evaluator;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.identity.SelfIdentityEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;

/**
 * Permission for identity account by identity.
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityAccountByRoleEvaluatorIntegrationTest extends AbstractEvaluatorIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private AccAccountService accountService;
	@Autowired private AccIdentityAccountService identityAccountService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	
	@Test
	public void testCanReadIdentityAccount() {
		IdmIdentityDto identity = helper.createIdentity();
		//
		SysSystemDto system = helper.createTestResourceSystem(true);
		
		AccAccountDto accountOne = new AccAccountDto();
		accountOne.setSystem(system.getId());
		accountOne.setUid(identity.getUsername());
		accountOne = accountService.save(accountOne);

		AccIdentityAccountDto accountIdentityOne = new AccIdentityAccountDto();
		accountIdentityOne.setIdentity(identity.getId());
		accountIdentityOne.setOwnership(true);
		accountIdentityOne.setAccount(accountOne.getId());
		accountIdentityOne = identityAccountService.save(accountIdentityOne);
		
		IdmRoleDto role = helper.createRole();
		
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setRole(role.getId());
		policy.setPermissions(IdmBasePermission.READ);
		policy.setGroupPermission(CoreGroupPermission.IDENTITY.getName());
		policy.setAuthorizableType(IdmIdentity.class.getCanonicalName());
		policy.setEvaluator(SelfIdentityEvaluator.class);
		authorizationPolicyService.save(policy);
		
		policy = new IdmAuthorizationPolicyDto();
		policy.setRole(role.getId());
		policy.setGroupPermission(AccGroupPermission.IDENTITYACCOUNT.getName());
		policy.setAuthorizableType(AccIdentityAccount.class.getCanonicalName());
		policy.setEvaluator(IdentityAccountByAccountEvaluator.class);
		authorizationPolicyService.save(policy);
		
		IdmAuthorizationPolicyDto policyAccount = new IdmAuthorizationPolicyDto();
		policyAccount.setRole(role.getId());
		policyAccount.setGroupPermission(AccGroupPermission.ACCOUNT.getName());
		policyAccount.setAuthorizableType(AccAccount.class.getCanonicalName());
		policyAccount.setEvaluator(ReadAccountByIdentityEvaluator.class);
		authorizationPolicyService.save(policyAccount);
		
		helper.createIdentityRole(identity, role);
		//
		// check
		try {
			getHelper().login(identity);
			AccIdentityAccountDto read = identityAccountService.get(accountIdentityOne.getId(), IdmBasePermission.READ);
			Assert.assertEquals(accountIdentityOne, read);
		} finally {
			logout();
		}
	}
	
	@Test(expected = ForbiddenEntityException.class)
	public void testCannotReadIdentityAccount() {
		IdmIdentityDto identity = helper.createIdentity();
		//
		SysSystemDto system = helper.createTestResourceSystem(true);
		
		AccAccountDto accountOne = new AccAccountDto();
		accountOne.setSystem(system.getId());
		accountOne.setUid(identity.getUsername());
		accountOne = accountService.save(accountOne);

		AccIdentityAccountDto accountIdentityOne = new AccIdentityAccountDto();
		accountIdentityOne.setIdentity(identity.getId());
		accountIdentityOne.setOwnership(true);
		accountIdentityOne.setAccount(accountOne.getId());
		accountIdentityOne = identityAccountService.save(accountIdentityOne);
		//
		// check
		try {
			getHelper().login(identity);
			identityAccountService.get(accountIdentityOne.getId(), IdmBasePermission.READ);
		} finally {
			logout();
		}
	}
}
