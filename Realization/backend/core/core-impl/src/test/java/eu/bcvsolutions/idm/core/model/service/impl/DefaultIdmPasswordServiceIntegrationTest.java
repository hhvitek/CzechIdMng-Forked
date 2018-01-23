package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.rest.impl.LoginController;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Password service integration test.
 * 
 * @author Jan Helbich
 * @author Petr Hanák
 */
@Transactional // we need rollback after each test
public class DefaultIdmPasswordServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmPasswordService passwordService;
	@Autowired private IdmPasswordPolicyService policyService;
	@Autowired private TestHelper testHelper;
	@Autowired private IdmIdentityService identityService;
	@Autowired private LoginController loginController;

	@Before
	public void before() {
		this.loginAsAdmin("[DefaultIdmPasswordServiceIntegrationTest]");
		removeDefaultPolicy();
	}

	@After
	public void after() {
		this.logout();
	}

	@Test
	public void testCreatePasswordNoPolicy() {
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		assertEquals(LocalDate.now(), password.getValidFrom());
		assertEquals(identity.getId(), password.getIdentity());
		assertNull(password.getValidTill());
	}

	@Test
	public void testCreatePasswordNonDefaultPolicy() {
		IdmPasswordPolicyDto policy = getTestPolicy(false);
		assertNotNull(policy);
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		assertEquals(LocalDate.now(), password.getValidFrom());
		assertEquals(identity.getId(), password.getIdentity());
		// when not exists default validation policy valid till be null
		assertNull(password.getValidTill());
	}

	@Test
	public void testCreatePasswordDefaultPolicy() {
		IdmPasswordPolicyDto policy = getTestPolicy(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		assertEquals(LocalDate.now(), password.getValidFrom());
		assertEquals(identity.getId(), password.getIdentity());
		assertEquals(LocalDate.now().plusDays(policy.getMaxPasswordAge()), password.getValidTill());
	}

	@Test
	public void testCreatePasswordMultiplePolicies() {
		IdmPasswordPolicyDto policy1 = getTestPolicy(true, IdmPasswordPolicyType.VALIDATE, 365);
		assertNotNull(policy1);
		IdmPasswordPolicyDto policy2 = getTestPolicy(true, IdmPasswordPolicyType.VALIDATE, 5);
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		assertEquals(LocalDate.now(), password.getValidFrom());
		assertEquals(identity.getId(), password.getIdentity());
		// default password policy may be only one
		assertEquals(LocalDate.now().plusDays(policy2.getMaxPasswordAge()), password.getValidTill());
	}
	
	@Test
	public void testTwoPoliciesSecondValidTillNull() {
		IdmPasswordPolicyDto policy1 = getTestPolicy(false, IdmPasswordPolicyType.VALIDATE, null);
		IdmPasswordPolicyDto policy2 = getTestPolicy(true, IdmPasswordPolicyType.VALIDATE, 5);
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		assertEquals(LocalDate.now(), password.getValidFrom());
		assertEquals(identity.getId(), password.getIdentity());
		assertEquals(LocalDate.now().plusDays(policy2.getMaxPasswordAge()), password.getValidTill());
		//
		policy1.setDefaultPolicy(true);
		policy1 = policyService.save(policy1);
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setNewPassword(new GuardedString("testPassword"));
		identityService.passwordChange(identity, passwordChangeDto);
		password = passwordService.findOneByIdentity(identity.getId());
		assertNull(password.getValidTill());
	}

	@Test
	public void testCreatePasswordValidationPolicy() {
		getTestPolicy(false, IdmPasswordPolicyType.VALIDATE, 365);
		IdmIdentityDto identity = testHelper.createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		assertEquals(LocalDate.now(), password.getValidFrom());
		assertEquals(identity.getId(), password.getIdentity());
		assertNull(password.getValidTill());
	}

	@Test
	public void testIncreaseUnsuccessfulAttempts() {
		IdmIdentityDto identity = testHelper.createIdentity();
		passwordService.increaseUnsuccessfulAttempts(identity.getUsername());
		passwordService.increaseUnsuccessfulAttempts(identity.getUsername());
		assertEquals(2, passwordService.findOneByIdentity(identity.getId()).getUnsuccessfulAttempts());
	}

	@Test
	public void testSetLastSuccessfulLogin() {
		IdmIdentityDto identity = testHelper.createIdentity();
		passwordService.setLastSuccessfulLogin(identity.getUsername());
		assertNotNull(passwordService.findOneByIdentity(identity.getId()).getLastSuccessfulLogin());
		assertTrue(DateTime.now().isAfter(passwordService.findOneByIdentity(identity.getId()).getLastSuccessfulLogin()));
	}

	@Test
	public void testSuccessfulLoginTimestamp() {
		IdmIdentityDto identity = testHelper.createIdentity();
		identity.setUsername("Honza");
		identity.setPassword(new GuardedString("SomePasswd"));
		identityService.save(identity);

		// first login
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername("Honza");
		loginDto.setPassword(new GuardedString("SomePasswd"));
		Resource<LoginDto> response = loginController.login(loginDto);
		DateTime timestamp = passwordService.findOneByIdentity("Honza").getLastSuccessfulLogin();

		assertNotNull(passwordService.findOneByIdentity("Honza").getLastSuccessfulLogin());

		// second login
		loginDto = new LoginDto();
		loginDto.setUsername("Honza");
		loginDto.setPassword(new GuardedString("SomePasswd"));
		response = loginController.login(loginDto);
		DateTime timestamp2 = passwordService.findOneByIdentity("Honza").getLastSuccessfulLogin();

		assertTrue(timestamp2.isAfter(timestamp));
	}

	private IdmPasswordPolicyDto getTestPolicy(boolean isDefault, IdmPasswordPolicyType type, Integer maxAge) {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName(UUID.randomUUID().toString());
		policy.setType(type);
		policy.setMaxPasswordAge(maxAge);
		policy.setDefaultPolicy(isDefault);
		return policyService.save(policy);
	}

	private IdmPasswordPolicyDto getTestPolicy(boolean isDefault) {
		return getTestPolicy(isDefault, IdmPasswordPolicyType.VALIDATE, 365);
	}

	private void removeDefaultPolicy() {
		// I need to get rid of default policy defined in init test data
		policyService.find(null).forEach(p -> policyService.delete(p));
	}

}