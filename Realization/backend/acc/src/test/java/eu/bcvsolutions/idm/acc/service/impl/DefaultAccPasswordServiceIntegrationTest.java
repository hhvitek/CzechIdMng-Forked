package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilter;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmSystemDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Account password service integration test.
 * 
 * @author Jirka Koula
 */
@Transactional
public class DefaultAccPasswordServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private SysSystemService systemService;
	@Autowired private ApplicationContext context;
	@Autowired private AccAccountService accountService;
	@Autowired private IdmIdentityService identityService;
	//
	private DefaultAccPasswordService passwordService;
	private SysSystemDto system;

	@Before
	public void init() {
		loginAsAdmin();
		passwordService = context.getAutowireCapableBeanFactory().createBean(DefaultAccPasswordService.class);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void checkNullValueNewPassword() {
		GuardedString passwordForCheck = new GuardedString("password");
		AccPasswordDto newPassword = new AccPasswordDto();
		assertFalse(passwordService.checkPassword(passwordForCheck, newPassword));
	}

	@Test
	public void checkNullValuePasswordForCheck() {
		GuardedString passwordForCheck = new GuardedString();
		AccPasswordDto newPassword = new AccPasswordDto();
		newPassword.setPassword(generateHash("password"));
		assertFalse(passwordService.checkPassword(passwordForCheck, newPassword));
	}
	
	@Test
	public void checkNullBothPasswords() {
		GuardedString passwordForCheck = new GuardedString();
		AccPasswordDto newPassword = new AccPasswordDto();
		assertFalse(passwordService.checkPassword(passwordForCheck, newPassword));
	}

	@Test
	public void checkCorrectBehaviorTrue() {
		String password = "password" + System.currentTimeMillis();
		GuardedString passwordForCheck = new GuardedString(password);
		AccPasswordDto newPassword = new AccPasswordDto();
		newPassword.setPassword(generateHash(password));
		assertTrue(passwordService.checkPassword(passwordForCheck, newPassword));
	}

	@Test
	public void checkCorrectBehaviorFalse() {
		String password = "password" + System.currentTimeMillis();
		GuardedString passwordForCheck = new GuardedString(password + "2");
		AccPasswordDto newPassword = new AccPasswordDto();
		newPassword.setPassword(generateHash(password));
		assertFalse(passwordService.checkPassword(passwordForCheck, newPassword));
	}

	@Test
	public void testFilterAccountId() {
		SysSystemDto system = helper.createTestResourceSystem(false);
		AccAccountDto account = helper.createAccountWithPassword(system, "test" + System.currentTimeMillis());
		AccPasswordFilter filter = new AccPasswordFilter();
		filter.setAccountId(account.getId());
		List<AccPasswordDto> passwords = passwordService.find(filter, null).getContent();
		assertEquals(1, passwords.size());
		AccPasswordDto passwordDto = passwords.get(0);
		assertEquals(account.getId(), passwordDto.getAccount());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testFilterText() {
		AccPasswordFilter filter = new AccPasswordFilter();
		filter.setText("text-" + System.currentTimeMillis());
		passwordService.find(filter, null).getContent();
		fail();
	}

	@Test
	public void testFilterByPassword() {
		SysSystemDto system = helper.createTestResourceSystem(false);
		AccAccountDto account = helper.createAccountWithPassword(system, "test" + System.currentTimeMillis());

		AccPasswordFilter filter = new AccPasswordFilter();
		filter.setAccountId(account.getId());
		List<AccPasswordDto> passwords = passwordService.find(filter, null).getContent();

		assertEquals(1, passwords.size());
		AccPasswordDto passwordDto = passwords.get(0);
		assertEquals(account.getId(), passwordDto.getAccount());
		
		filter = new AccPasswordFilter();
		filter.setPassword(passwordDto.getPassword());
		
		passwords = passwordService.find(filter, null).getContent();

		assertEquals(1, passwords.size());
		AccPasswordDto passwordDtoTwo = passwords.get(0);
		assertEquals(account.getId(), passwordDtoTwo.getAccount());
		assertEquals(passwordDto.getId(), passwordDtoTwo.getId());
		assertEquals(passwordDto.getPassword(), passwordDtoTwo.getPassword());
	}

	private String generateHash(String password) {
		return passwordService.generateHash(new GuardedString(password), passwordService.getSalt());
	}
}