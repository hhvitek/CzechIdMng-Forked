package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import eu.bcvsolutions.idm.core.api.config.domain.TreeConfiguration;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.config.domain.EntityToUuidConditionalConverter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Identity contract unit tests:
 * - prime contract
 * - contract state
 * 
 * @author Radek Tomiška
 */
public class DefaultIdmIdentityContractServiceUnitTest extends AbstractUnitTest {

	private final IdmIdentityContractRepository repository = mock(IdmIdentityContractRepository.class);;
	private final FormService formService = mock(FormService.class);;
	private final EntityEventManager entityEventManager = mock(EntityEventManager.class);;
	private final IdmTreeTypeRepository treeTypeRepository = mock(IdmTreeTypeRepository.class);;
	private final TreeConfiguration treeConfiguration = mock(TreeConfiguration.class);;
	private final IdmTreeNodeRepository treeNodeRepository = mock(IdmTreeNodeRepository.class);;
	private final IdmContractSliceService contractSliceService = mock(IdmContractSliceService.class);;
	private final LookupService lookupService = mock(LookupService.class);;
	private final ApplicationContext applicationContext = mock(ApplicationContext.class);
	private final ModelMapper modelMapper = spy(ModelMapper.class);
	//
	private final DefaultIdmIdentityContractService service = spy(new DefaultIdmIdentityContractService(repository, formService, entityEventManager));
	
	@Before
	public void init() {
		modelMapper.getConfiguration().getConverters().add(new EntityToUuidConditionalConverter(modelMapper, applicationContext));
		ReflectionTestUtils.setField(service, "modelMapper", modelMapper);
		ReflectionTestUtils.setField(service, "treeConfiguration", treeConfiguration);
		ReflectionTestUtils.setField(service, "contractSliceService", contractSliceService);
	}
	
	@Test
	public void testSimplePrimeContract() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract otherContract = new IdmIdentityContract(UUID.randomUUID());
		otherContract.setMain(false);
		IdmIdentityContract mainContract = new IdmIdentityContract(UUID.randomUUID());
		mainContract.setMain(true);
		contracts.add(otherContract);
		contracts.add(mainContract);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);	
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.count(any(IdmContractSliceFilter.class))).thenReturn(0L);
		
		//
		Assert.assertEquals(mainContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testPrimeContractWithWorkingPosition() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract contractWithPosition = new IdmIdentityContract(UUID.randomUUID());
		contractWithPosition.setMain(false);
		IdmTreeNode workPosition = new IdmTreeNode();
		workPosition.setId(UUID.randomUUID());
		workPosition.setTreeType(new IdmTreeType(UUID.randomUUID()));
		contractWithPosition.setWorkPosition(workPosition);
		//
		IdmIdentityContract otherContract = new IdmIdentityContract(UUID.randomUUID());
		otherContract.setMain(false);
		contracts.add(contractWithPosition);
		contracts.add(otherContract);
		//
		when(applicationContext.getBean(LookupService.class)).thenReturn(lookupService);
		when(lookupService.toDto(any(), any(), any())).then(new LookupAnswer());
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.count(any(IdmContractSliceFilter.class))).thenReturn(0L);
		//
		Assert.assertEquals(contractWithPosition.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testPrimeContractWithDefaultTreeType() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract contractWithDefaultPosition = new IdmIdentityContract(UUID.randomUUID());
		contractWithDefaultPosition.setMain(false);
		IdmTreeNode defaultWorkPosition = new IdmTreeNode();
		defaultWorkPosition.setId(UUID.randomUUID());
		IdmTreeType defaultTreeType = new IdmTreeType(UUID.randomUUID());
		defaultWorkPosition.setTreeType(defaultTreeType);
		contractWithDefaultPosition.setWorkPosition(defaultWorkPosition);
		//
		IdmIdentityContract otherContract = new IdmIdentityContract(UUID.randomUUID());
		otherContract.setMain(false);
		IdmTreeNode workPosition = new IdmTreeNode();
		workPosition.setId(UUID.randomUUID());
		workPosition.setTreeType(new IdmTreeType(UUID.randomUUID()));
		otherContract.setWorkPosition(workPosition);
		//
		contracts.add(contractWithDefaultPosition);
		contracts.add(otherContract);
		//
		when(applicationContext.getBean(LookupService.class)).thenReturn(lookupService);
		when(lookupService.toDto(any(), any(), any())).then(new LookupAnswer());
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(new IdmTreeTypeDto(defaultTreeType.getId()));
		when(contractSliceService.count(any(IdmContractSliceFilter.class))).thenReturn(0L);
		//
		Assert.assertEquals(contractWithDefaultPosition.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testSimpleValidPrimeContract() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract invalidContract = new IdmIdentityContract(UUID.randomUUID());
		invalidContract.setMain(true);
		invalidContract.setValidFrom(LocalDate.now().plusDays(1));
		IdmIdentityContract mainContract = new IdmIdentityContract(UUID.randomUUID());
		mainContract.setMain(true);
		contracts.add(invalidContract);
		contracts.add(mainContract);
		//
		when(repository.findAllByIdentity_Id((UUID) any(), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.count((IdmContractSliceFilter) any())).thenReturn(0L);
		//
		Assert.assertEquals(mainContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testSimpleDisabledPrimeContract() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract invalidContract = new IdmIdentityContract(UUID.randomUUID());
		invalidContract.setMain(true);
		invalidContract.setDisabled(true);
		IdmIdentityContract mainContract = new IdmIdentityContract(UUID.randomUUID());
		mainContract.setMain(true);
		contracts.add(invalidContract);
		contracts.add(mainContract);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.count(any(IdmContractSliceFilter.class))).thenReturn(0L);
		
		//
		Assert.assertEquals(mainContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	/**
	 * Invalid main contract has still higher priority
	 */
	@Test
	public void testDisabledMainContract() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract invalidContract = new IdmIdentityContract(UUID.randomUUID());
		invalidContract.setMain(true);
		invalidContract.setDisabled(true);
		IdmIdentityContract otherContract = new IdmIdentityContract(UUID.randomUUID());
		otherContract.setMain(false);
		contracts.add(otherContract);
		contracts.add(invalidContract);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.count(any(IdmContractSliceFilter.class))).thenReturn(0L);
		
		//
		Assert.assertEquals(invalidContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testOtherMainContractByFilledValidFrom() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract oneContract = new IdmIdentityContract(UUID.randomUUID());
		oneContract.setValidFrom(LocalDate.now());
		oneContract.setMain(false);
		IdmIdentityContract twoContract = new IdmIdentityContract(UUID.randomUUID());
		twoContract.setMain(false);
		contracts.add(twoContract);
		contracts.add(oneContract);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.count(any(IdmContractSliceFilter.class))).thenReturn(0L);
		
		//
		Assert.assertEquals(twoContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testOtherMainContractByValidFrom() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract oneContract = new IdmIdentityContract(UUID.randomUUID());
		oneContract.setValidFrom(LocalDate.now().minusDays(2));
		oneContract.setCreated(ZonedDateTime.now());
		oneContract.setMain(false);
		IdmIdentityContract twoContract = new IdmIdentityContract(UUID.randomUUID());
		twoContract.setMain(false);
		twoContract.setValidFrom(LocalDate.now().minusDays(1));
		twoContract.setCreated(ZonedDateTime.now().minusSeconds(2));
		contracts.add(twoContract);
		contracts.add(oneContract);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.count(any(IdmContractSliceFilter.class))).thenReturn(0L);
		//
		Assert.assertEquals(oneContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testOtherMainContractByCreated() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract oneContract = new IdmIdentityContract(UUID.randomUUID());
		oneContract.setCreated(ZonedDateTime.now().minusSeconds(2));
		oneContract.setMain(false);
		IdmIdentityContract twoContract = new IdmIdentityContract(UUID.randomUUID());
		twoContract.setMain(false);
		twoContract.setCreated(ZonedDateTime.now());
		contracts.add(twoContract);
		contracts.add(oneContract);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.count(any(IdmContractSliceFilter.class))).thenReturn(0L);
		//
		Assert.assertEquals(oneContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	private class LookupAnswer implements Answer<BaseDto> {
		
		@Override
		public BaseDto answer(InvocationOnMock invocation) throws Throwable {
			BaseEntity entity = (BaseEntity) invocation.getArguments()[0];
			BaseDto dto = (BaseDto) invocation.getArguments()[1];
			//
			if (dto == null) {
				return modelMapper.map(entity, IdmIdentityContractDto.class);
			}
			modelMapper.map(entity, dto);
			return dto;
			
		}
	}
}
