package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTokenRepository;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;
import static org.mockito.Mockito.*;

/**
 * Identity service unit tests
 * - nice label
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmIdentityServiceUnitTest extends AbstractUnitTest {

	private final IdmIdentityRepository repository = mock(IdmIdentityRepository.class);
	private final IdmRoleService roleService = mock(IdmRoleService.class);
	private final IdmTokenRepository authChangeRepository = mock(IdmTokenRepository.class);
	private final EntityEventManager entityEventManager = mock(EntityEventManager.class);
	private final RoleConfiguration roleConfiguration = mock(RoleConfiguration.class);
	private final FormService formService = mock(FormService.class);
	private final IdmIdentityContractService identityContractService = mock(IdmIdentityContractService.class);
	private final IdmPasswordService passwordService = mock(IdmPasswordService.class);
	private final TokenManager tokenManager = mock(TokenManager.class);
	private final ModelMapper modelMapper = spy(ModelMapper.class);
	//
	private final DefaultIdmIdentityService service = spy(new DefaultIdmIdentityService(repository, formService, entityEventManager));

	@Before
	public void init() {
		ReflectionTestUtils.setField(service, "modelMapper", modelMapper);
		ReflectionTestUtils.setField(service, "identityContractService", identityContractService);
	}

	@Test
	public void testNiceLabelWithNull() {
		Assert.assertNull(service.getNiceLabel(null));
	}
	
	@Test
	public void testNiceLabelWithUsernameOnly() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "validation_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		//
		Assert.assertEquals(username, service.getNiceLabel(identity));
	}
	
	@Test
	public void testNiceLabelWithTitlesAndFirstnameOnly() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "validation_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setFirstName("firstname");
		identity.setTitleAfter("csc.");
		identity.setTitleBefore("Bc.");
		//
		Assert.assertEquals(username, service.getNiceLabel(identity));
	}
	
	@Test
	public void testNiceLabelWithLastnameOnly() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setLastName("lastName");
		//
		Assert.assertEquals(identity.getLastName(), service.getNiceLabel(identity));
	}
	
	@Test
	public void testNiceLabelWithFirstnameLastName() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setFirstName("firstname");
		identity.setLastName("lastName");
		//
		Assert.assertEquals(String.format("%s %s", 
				identity.getFirstName(),
				identity.getLastName()), 
				service.getNiceLabel(identity));
	}
	
	@Test
	public void testNiceLabelWithFullName() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setFirstName("firstname");
		identity.setLastName("lastName");
		identity.setTitleAfter("csc.");
		identity.setTitleBefore("Bc.");
		//
		Assert.assertEquals(String.format("%s %s %s, %s", 
				identity.getTitleBefore(),
				identity.getFirstName(),
				identity.getLastName(),
				identity.getTitleAfter()), 
				service.getNiceLabel(identity));
	}
	
	
	@Test
	public void testValidState() {
		IdmIdentity identity = new IdmIdentity(UUID.randomUUID());
		IdmIdentityContractDto contractOne = new IdmIdentityContractDto();
		contractOne.setState(ContractState.DISABLED);
		IdmIdentityContractDto contractTwo = new IdmIdentityContractDto();
		when(repository.findById(identity.getId())).thenReturn(Optional.of(identity));	
		when(identityContractService.findAllByIdentity(identity.getId())).thenReturn(Lists.newArrayList(contractOne, contractTwo));	
		//
		Assert.assertEquals(IdentityState.VALID, service.evaluateState(identity.getId()));
	}
	
	@Test
	public void testDisabledState() {
		IdmIdentity identity = new IdmIdentity(UUID.randomUUID());
		IdmIdentityContractDto contractOne = new IdmIdentityContractDto();
		contractOne.setState(ContractState.DISABLED);
		IdmIdentityContractDto contractTwo = new IdmIdentityContractDto();
		contractTwo.setState(ContractState.DISABLED);
		when(repository.findById(identity.getId())).thenReturn(Optional.of(identity));	
		when(identityContractService.findAllByIdentity(identity.getId())).thenReturn(Lists.newArrayList(contractOne, contractTwo));	
		//
		Assert.assertEquals(IdentityState.LEFT, service.evaluateState(identity.getId()));
	}
	
	@Test
	public void testNoContractState() {
		IdmIdentity identity = new IdmIdentity(UUID.randomUUID());
		when(repository.findById(identity.getId())).thenReturn(Optional.of(identity));	
		when(identityContractService.findAllByIdentity(identity.getId())).thenReturn(Lists.newArrayList());	
		//
		Assert.assertEquals(IdentityState.NO_CONTRACT, service.evaluateState(identity.getId()));
	}	
	
	@Test
	public void testDisabledManuallyState() {
		IdmIdentity identity = new IdmIdentity(UUID.randomUUID());
		identity.setState(IdentityState.DISABLED_MANUALLY);
		when(repository.findById(identity.getId())).thenReturn(Optional.of(identity));		
		//
		Assert.assertEquals(IdentityState.DISABLED_MANUALLY, service.evaluateState(identity.getId()));
	}
	
	@Test
	public void testFutureContractState() {
		IdmIdentity identity = new IdmIdentity(UUID.randomUUID());
		IdmIdentityContractDto contractOne = new IdmIdentityContractDto();
		contractOne.setState(ContractState.DISABLED);
		IdmIdentityContractDto contractTwo = new IdmIdentityContractDto();
		contractTwo.setValidFrom(LocalDate.now().plusDays(1));
		when(repository.findById(identity.getId())).thenReturn(Optional.of(identity));	
		when(identityContractService.findAllByIdentity(identity.getId())).thenReturn(Lists.newArrayList(contractOne, contractTwo));	
		//
		Assert.assertEquals(IdentityState.FUTURE_CONTRACT, service.evaluateState(identity.getId()));
	}
}
