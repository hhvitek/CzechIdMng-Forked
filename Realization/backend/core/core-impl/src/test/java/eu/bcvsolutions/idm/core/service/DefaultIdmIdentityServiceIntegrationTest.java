package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitDemoData;
import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic identity service operations
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmIdentityServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired
	private ApplicationContext context;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmRoleGuaranteeRepository roleGuaranteeRepository;
	@Autowired
	private IdmPasswordService passwordService;
	//
	private IdmIdentityService identityService;

	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_USER_1);
		identityService = context.getAutowireCapableBeanFactory().createBean(DefaultIdmIdentityService.class);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testReferentialIntegrity() {
		IdmIdentity identity = new IdmIdentity();
		String username = "delete_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setPassword(new GuardedString("heslo")); // confidential
															// storage
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		// eav
		IdmFormDefinition formDefinition = formService.getDefinition(IdmIdentity.class);
		AbstractFormValue value1 = new IdmIdentityFormValue(
				formDefinition.getMappedAttributeByName(InitDemoData.FORM_ATTRIBUTE_PASSWORD));
		value1.setValue("one");
		formService.saveValues(identity, formDefinition, Lists.newArrayList(value1));
		// role guarantee
		IdmRole role = new IdmRole();
		String roleName = "test_r_" + System.currentTimeMillis();
		role.setName(roleName);
		IdmRoleGuarantee roleGuarantee = new IdmRoleGuarantee();
		roleGuarantee.setRole(role);
		roleGuarantee.setGuarantee(identity);
		;
		role.setGuarantees(Lists.newArrayList(roleGuarantee));
		roleService.save(role);
		// contract
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		contract.setPosition("test");
		identityContractService.save(contract);
		// contract guarantee
		IdmIdentityContract contractGuarantee = new IdmIdentityContract();
		contractGuarantee.setIdentity(identityService.getByUsername(InitTestData.TEST_USER_1));
		contractGuarantee.setPosition("test");
		contractGuarantee.setGuarantee(identity);
		contractGuarantee = identityContractService.save(contractGuarantee);
		// assigned role
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentityContract(contract);
		identityRole.setRole(role);
		identityRoleService.save(identityRole);
		IdentityRoleFilter identityRolefilter = new IdentityRoleFilter();
		identityRolefilter.setIdentityId(identity.getId());

		assertNotNull(identityService.getByUsername(username));
		assertNotNull(passwordService.get(identity));
		assertEquals(1, formService.getValues(identity).size());
		assertEquals(username, roleGuaranteeRepository.findAllByRole(role).get(0).getGuarantee().getUsername());
		assertEquals(1, identityRoleService.find(identityRolefilter, null).getTotalElements());
		assertEquals(2, identityContractService.getContracts(identity).size()); // +default
																				// contract
																				// is
																				// created
		assertEquals(username, identityContractService.get(contractGuarantee.getId()).getGuarantee().getUsername());

		identityService.delete(identity);

		assertNull(identityService.getByUsername(username));
		assertNull(passwordService.get(identity));
		assertEquals(0, formService.getValues(identity).size());
		assertEquals(0, roleGuaranteeRepository.findAllByRole(role).size());
		assertEquals(0, identityRoleService.find(identityRolefilter, null).getTotalElements());
		assertEquals(0, identityContractService.getContracts(identity).size());
		assertNull(identityContractService.get(contractGuarantee.getId()).getGuarantee());
	}

	/**
	 * When identity is created, then default contract have to be created too.
	 */
	@Test
	public void testCreateDefaultContract() {
		IdmIdentity identity = new IdmIdentity();
		String username = "contract_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setPassword(new GuardedString("heslo")); // confidential
															// storage
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		//
		List<IdmIdentityContract> contracts = identityContractService.getContracts(identity);
		assertEquals(1, contracts.size());
		//
		IdmIdentityContract defaultContract = identityContractService.prepareDefaultContract(identity);
		assertEquals(defaultContract.getIdentity(), contracts.get(0).getIdentity());
		assertEquals(defaultContract.getPosition(), contracts.get(0).getPosition());
		assertEquals(defaultContract.getWorkingPosition(), contracts.get(0).getWorkingPosition());
	}

}
