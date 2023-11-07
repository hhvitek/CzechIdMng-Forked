package eu.bcvsolutions.idm.core.api.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.ExternalCodeable;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.InstanceIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.EmbeddedDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.DuplicateExternalIdException;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.rest.FormableDtoController;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Basic dto controller tests.
 * - CRUD
 * - find, quick, autocomplete, count
 * - find by external id (if dto supports {@link ExternalIdentifiable})
 * - find by external code (if dto supports {@link ExternalCodeable})
 * - find by codeable (if dto supports {@link Codeable})
 * - find by id (if service supports {@link DataFilter})
 * - permissions
 * - get form definitions
 * - save form values
 * 
 * Make sure controller support all methods => add methods implementation instead skipping tests (but it's possible).
 * Controller's service should support {@link DataFilter} - methods which requires it are skipped internally (see log), but
 * better way is to implement {@link DataFilter} in service.
 * 
 * Test uses test admin authentication (see {@link TestHelper}).
 * 
 * @author Radek Tomiška
 *
 */
@Ignore
@Transactional
public abstract class AbstractReadWriteDtoControllerRestTest<DTO extends AbstractDto> extends AbstractRestTest {	
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractReadWriteDtoControllerRestTest.class);
	//
	@Autowired private FormService formService;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private BulkActionManager bulkActionManager;
	@Autowired private LookupService lookupService;
	
	@Before
	public void setup() throws Exception {
		super.setup();
	}

	@After
	public void logout() {
		// just for sure
		super.logout();
	}
	
	/**
	 * Controller, which will be tested
	 * 
	 * @return
	 */
	protected abstract AbstractReadWriteDtoController<DTO, ?> getController();
	
	/**
	 * Prepare dto instance (not saved)
	 * 
	 * @return
	 */
	protected abstract DTO prepareDto();
	
	/**
	 * True - only "read" controller method will be tested. 
	 * Shortcut for turn off all "write" method test (TODO: create AbstractReadDtoControllerRestTest superclass instead)
	 * 
	 * @return
	 */
	protected boolean isReadOnly() {
		return false;
	}
	
	/**
	 * True - post method will be tested
	 * 
	 * @return
	 */
	protected boolean supportsPost() {
		return !isReadOnly();
	}
	
	/**
	 * True - put method will be tested
	 * 
	 * @return
	 */
	protected boolean supportsPut() {
		return !isReadOnly();
	}
	
	/**
	 * True - patch method will be tested
	 * 
	 * @return
	 */
	protected boolean supportsPatch() {
		return !isReadOnly();
	}
	
	/**
	 * True - delete method will be tested
	 * @return
	 */
	protected boolean supportsDelete() {
		return !isReadOnly();
	}
	
	/**
	 * True - autocomplete method will be tested
	 * @return
	 */
	protected boolean supportsAutocomplete() {
		return true;
	}
	
	/**
	 * True - supports save form values
	 * @return
	 */
	protected boolean supportsFormValues() {
		return formService.isFormable(getController().getDtoClass());
	}
	
	/**
	 * True - supports bulk actions.
	 * 
	 * @return
	 */
	protected boolean supportsBulkActions() {
		return !bulkActionManager.getAvailableActions(lookupService.getDtoService(getController().getDtoClass()).getEntityClass()).isEmpty();
	}
	
	@Test
	public void testGet() throws Exception {
		DTO dto = createDto();
		// by code
		if (dto instanceof Codeable) {
			getMockMvc().perform(get(getDetailUrl(((Codeable)dto).getCode()))
	        		.with(authentication(getAdminAuthentication()))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andExpect(jsonPath("$.id", equalTo(dto.getId().toString())));
		}
		// by id
		getMockMvc().perform(get(getDetailUrl(dto.getId()))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.id", equalTo(dto.getId().toString())));
	}
	
	@Test
    public void testNotFound() throws Exception {
		getMockMvc().perform(get(getDetailUrl(UUID.randomUUID()))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isNotFound());
    }
	
	@Test
	@SuppressWarnings("unchecked")
	public void testPost() throws Exception {
		if (!supportsPost()) {
			LOG.info("Controller [{}] doesn't support POST method. Method will not be tested.", getController().getClass());
			return;
		}
		DTO dto = prepareDto();
		ObjectMapper mapper = getMapper();
		//
		String response = getMockMvc().perform(post(getBaseUrl())
        		.with(authentication(getAdminAuthentication()))
        		.content(mapper.writeValueAsString(dto))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isCreated())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		DTO createdDto = (DTO) mapper.readValue(response, dto.getClass());
		Assert.assertNotNull(createdDto);
		Assert.assertNotNull(createdDto.getId());
		//
		DTO getDto = getDto(createdDto.getId());
		Assert.assertEquals(createdDto.getId(), getDto.getId());
		Assert.assertNotNull(getDto.getCreator());
		Assert.assertNotNull(getDto.getCreatorId());
		Assert.assertNotNull(getDto.getCreated());
		Assert.assertNotNull(getDto.getTransactionId());
		//
		// TODO: realm
	}
	
	@Test
	public void testPut() throws Exception {
		if (!supportsPut()) {
			LOG.info("Controller [{}] doesn't support PUT method. Method will not be tested.", getController().getClass());
			return;
		}
		DTO dto = createDto();
		dto.setModifier("mock"); // some change is needed, but modifier will be replaced any way by logged used on jpa listener ... but change is made
		//
		getMockMvc().perform(put(getDetailUrl(dto.getId()))
        		.with(authentication(getAdminAuthentication()))
        		.content(getMapper().writeValueAsString(dto))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE));
		dto = getDto(dto.getId());
		//
		Assert.assertNotNull(dto.getModifier());
		Assert.assertNotNull(dto.getModified());
		Assert.assertNotNull(dto.getModifierId());
		//
		// TODO: modify some common prop (code / external id / external code)
	}
	
	@Test
	public void testPatch() throws Exception {
		if (!supportsPatch()) {
			LOG.info("Controller [{}] doesn't support PATCH method. Method will not be tested.", getController().getClass());
			return;
		}
		DTO dto = createDto();
		dto.setModifier("mock"); // some change is needed, but modifier will be replaced any way by logged used on jpa listener ... but change is made
		//
		getMockMvc().perform(patch(getDetailUrl(dto.getId()))
        		.with(authentication(getAdminAuthentication()))
        		.content(getMapper().writeValueAsString(dto))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE));
		dto = getDto(dto.getId());
		//
		Assert.assertNotNull(dto.getModifier());
		Assert.assertNotNull(dto.getModified());
		Assert.assertNotNull(dto.getModifierId());
		//
		// TODO: modify some common prop (code / external id / external code)
	}

	@Test
	public void testDelete() throws Exception {
		if (!supportsDelete()) {
			LOG.info("Controller [{}] doesn't support DELETE method. Method will not be tested.", getController().getClass());
			return;
		}
		DTO dto = createDto();
		DTO getDto = getDto(dto.getId());
		//
		Assert.assertNotNull(getDto);
		Assert.assertEquals(dto.getId(), getDto.getId());
		//
		getMockMvc().perform(delete(getDetailUrl(dto.getId()))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().is2xxSuccessful()); // 204 or 202 (accepted)
		//
		getMockMvc().perform(get(getDetailUrl(dto.getId()))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isNotFound());
	}
	
	/**
	 * Test search by external identifier, if DTO implements {@link ExternalIdentifiable}
	 * 
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testFindByExternalId() {
		DTO dto = prepareDto();
		if (!(dto instanceof ExternalIdentifiable)) {
			// ignore test
			return;
		}
		//
		ExternalIdentifiable externalIdentifiableDto = (ExternalIdentifiable) dto;
		externalIdentifiableDto.setExternalId(getHelper().createName());
		//
		DTO createdDto = createDto(dto);
		//
		// create another mock dto
		ExternalIdentifiable dtoTwo = (ExternalIdentifiable) prepareDto();
		dtoTwo.setExternalId(getHelper().createName());
		createDto((DTO) dtoTwo);
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set(ExternalIdentifiable.PROPERTY_EXTERNAL_ID, externalIdentifiableDto.getExternalId());
		//
		List<DTO> results = find(parameters);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		//
		if (supportsAutocomplete()) {
			results = autocomplete(parameters);
			//
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		} else {
			LOG.info("Controller [{}] doesn't support autocomplete method. Method will not be tested.", getController().getClass());
		}
		//
		Assert.assertEquals(1, count(parameters));
	}
	
	/**
	 * Test search by instance identifier, if DTO implements {@link InstanceIdentifiable}.
	 * 
	 * @throws Exception
	 * @since 11.1.0
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testFindByInstanceId() {
		DTO dto = prepareDto();
		if (!(dto instanceof InstanceIdentifiable)) {
			// ignore test
			return;
		}
		//
		InstanceIdentifiable instanceIdentifiableDto = (InstanceIdentifiable) dto;
		instanceIdentifiableDto.setInstanceId(getHelper().createName());
		//
		DTO createdDto = createDto(dto);
		//
		// create another mock dto
		InstanceIdentifiable dtoTwo = (InstanceIdentifiable) prepareDto();
		dtoTwo.setInstanceId(getHelper().createName());
		createDto((DTO) dtoTwo);
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set(InstanceIdentifiable.PROPERTY_INSTANCE_ID, instanceIdentifiableDto.getInstanceId());
		//
		List<DTO> results = find(parameters);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		//
		if (supportsAutocomplete()) {
			results = autocomplete(parameters);
			//
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		} else {
			LOG.info("Controller [{}] doesn't support autocomplete method. Method will not be tested.", getController().getClass());
		}
		//
		Assert.assertEquals(1, count(parameters));
	}
	
	@Test
	public void testFindByTransactionId() {
		if (!DataFilter.class.isAssignableFrom(getController().getFilterClass())) {
			LOG.warn("Controller [{}] doesn't support DataFilter. Find by id will not be tested.", getController().getClass());
			return;
		}
		//
		TransactionContextHolder.clearContext();
		DTO createdDto = createDto();
		Assert.assertNotNull(createdDto.getTransactionId());
		//
		// create another mock dto
		TransactionContextHolder.clearContext();
		DTO dtoTwo = createDto();
		Assert.assertNotNull(dtoTwo.getTransactionId());
		Assert.assertNotEquals(createdDto.getTransactionId(), dtoTwo.getTransactionId());
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set(DataFilter.PARAMETER_TRANSACTION_ID, createdDto.getTransactionId().toString());
		//
		List<DTO> results = find(parameters);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		//
		if (supportsAutocomplete()) {
			results = autocomplete(parameters);
			//
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		} else {
			LOG.info("Controller [{}] doesn't support autocomplete method. Method will not be tested.", getController().getClass());
		}
		//
		Assert.assertEquals(1, count(parameters));
	}
	
	@Test
	public void testDuplicateExternalId() throws Exception {
		if (!DataFilter.class.isAssignableFrom(getController().getFilterClass())) {
			LOG.warn("Controller [{}] doesn't support DataFilter. Find by external id will not be tested.", getController().getClass());
			return;
		}
		
		//
		DTO dto = prepareDto();
		if (!(dto instanceof ExternalIdentifiable)) {
			// ignore test
			return;
		}
		//
		ExternalIdentifiable externalIdentifiableDto = (ExternalIdentifiable) dto;
		String name = getHelper().createName();
		externalIdentifiableDto.setExternalId(name);
		//
		DTO original = createDto(dto);
		DTO duplicate = prepareDto();
		((ExternalIdentifiable) duplicate).setExternalId(name);
		//
		if (!supportsPost()) {
			try {
				createDto(duplicate);
				Assert.fail();
			} catch (DuplicateExternalIdException ex) {
				Assert.assertEquals(original.getId(), ex.getDuplicateId());
			}
		} else {
			getMockMvc().perform(post(getBaseUrl())
	        		.with(authentication(getAdminAuthentication()))
	        		.content(getMapper().writeValueAsString(duplicate))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isConflict());
		}
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testFindByExternalCode() {
		DTO dto = prepareDto();
		if (!(dto instanceof ExternalCodeable)) {
			// ignore test
			return;
		}
		//
		ExternalCodeable externalCodeableDto = (ExternalCodeable) dto;
		externalCodeableDto.setExternalCode(getHelper().createName());
		//
		DTO createdDto = createDto(dto);
		//
		// create another mock dto
		ExternalCodeable dtoTwo = (ExternalCodeable) prepareDto();
		dtoTwo.setExternalCode(getHelper().createName());
		createDto((DTO) dtoTwo);
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set(ExternalCodeable.PROPERTY_EXTERNAL_CODE, externalCodeableDto.getExternalCode());
		//
		List<DTO> results = find(parameters);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		//
		if (supportsAutocomplete()) {
			results = autocomplete(parameters);
			//
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		} else {
			LOG.info("Controller [{}] doesn't support autocomplete method. Method will not be tested.", getController().getClass());
		}
		//
		Assert.assertEquals(1, count(parameters));
	}
	
	@Test
	public void testDuplicateExternalCode() throws Exception {
		if (!DataFilter.class.isAssignableFrom(getController().getFilterClass())) {
			LOG.warn("Controller [{}] doesn't support DataFilter. Find by external id will not be tested.", getController().getClass());
			return;
		}
		//
		DTO dto = prepareDto();
		if (!(dto instanceof ExternalCodeable)) {
			// ignore test
			return;
		}
		//
		ExternalCodeable externalIdentifiableDto = (ExternalCodeable) dto;
		String name = getHelper().createName();
		externalIdentifiableDto.setExternalCode(name);
		//
		createDto(dto);
		//
		getMockMvc().perform(post(getBaseUrl())
        		.with(authentication(getAdminAuthentication()))
        		.content(getMapper().writeValueAsString(dto))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isConflict());
	}
	
	/**
	 * Test search by external identifier, if DTO implements {@link ExternalIdentifiable}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFindByCodeable() {
		if (!DataFilter.class.isAssignableFrom(getController().getFilterClass())) {
			LOG.warn("Controller [{}] doesn't support DataFilter. Find by codeable will not be tested.", getController().getClass());
			return;
		}
		//
		DTO dto = prepareDto();
		if (!(dto instanceof Codeable)) {
			// ignore test
			return;
		}
		Codeable codeable = (Codeable) dto;
		if (StringUtils.isEmpty(codeable.getCode())) {
			throw new CoreException("Code has to be set by #prepareDto method, its required by default");
		}
		//
		DTO createdDto = createDto(dto);
		// mock dto
		createDto(prepareDto());
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set(DataFilter.PARAMETER_CODEABLE_IDENTIFIER, codeable.getCode());
		//
		List<DTO> results = find(parameters);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		//
		if (supportsAutocomplete()) {
			results = autocomplete(parameters);
			//
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		} else {
			LOG.info("Controller [{}] doesn't support autocomplete method. Method will not be tested.", getController().getClass());
		}
		//
		Assert.assertEquals(1, count(parameters));
	}
	
	/**
	 * Test search by id - supported by default, id DataFilter is used (see #toPedicates in services - has to call super implementation)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFindById() {
		if (!DataFilter.class.isAssignableFrom(getController().getFilterClass())) {
			LOG.warn("Controller [{}] doesn't support DataFilter. Find by id will not be tested.", getController().getClass());
			return;
		}
		//
		DTO dto = prepareDto();
		//
		DTO createdDto = createDto(dto);
		// mock dto
		createDto(prepareDto());
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set(DataFilter.PARAMETER_ID, createdDto.getId().toString());
		//
		List<DTO> results = find(parameters);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		//
		// find quick alias
		results = findQuick(parameters);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		//
		if (supportsAutocomplete()) {
			results = autocomplete(parameters);
			//
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		} else {
			LOG.info("Controller [{}] doesn't support autocomplete method. Method will not be tested.", getController().getClass());
		}
		//
		Assert.assertEquals(1, count(parameters));
	}
	
	@Test
	public void testDeleteEntityNotExists() throws Exception {
		if (!supportsDelete()) {
			LOG.info("Controller [{}] doesn't support DELETE method. Method will not be tested.", getController().getClass());
			return;
		}
		getMockMvc().perform(delete(getDetailUrl(UUID.randomUUID()))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void testCount() {
		if (!DataFilter.class.isAssignableFrom(getController().getFilterClass())) {
			LOG.warn("Controller [{}] doesn't support DataFilter. Count method will not be tested.", getController().getClass());
			return;
		}
		DTO dto = prepareDto();
		//
		DTO createdDto = createDto(dto);
		// mock dto
		createDto(prepareDto());
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set(DataFilter.PARAMETER_ID, createdDto.getId().toString());
		//
		long results = count(parameters);
		//
		Assert.assertEquals(1, results);
	}
	
	@Test
	public void testPermissions() {
		DTO dto = prepareDto();
		//
		DTO createdDto = createDto(dto);
		//
		List<String> permissions = getPermissions(createdDto, getAdminAuthentication());
		//
		Assert.assertNotNull(permissions);
		Assert.assertFalse(permissions.isEmpty());
		Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.ADMIN.getName())));
	}
	
	@Test
	public void testSaveFormDefinition() throws Exception {
		if (!supportsFormValues()) {
			LOG.info("Controller [{}] doesn't support extended attributes. Method will not be tested.", getController().getClass());
			return;
		}
		IdmFormAttributeDto formAttribute = new IdmFormAttributeDto(getHelper().createName());
		IdmFormDefinitionDto formDefinition = formService.createDefinition(getFormOwnerType(), getHelper().createName(), Lists.newArrayList(formAttribute));
		formAttribute = formDefinition.getFormAttributes().get(0);
		//
		DTO owner = createDto();
		//
		// form definition is available
		List<IdmFormDefinitionDto> formDefinitions = getFormDefinitions(owner.getId(), TestHelper.ADMIN_USERNAME);
		Assert.assertTrue(formDefinitions.stream().anyMatch(d -> d.getId().equals(formDefinition.getId())));
		//
		// test prepare values
		if (getController() instanceof FormableDtoController) {
			String response = getMockMvc().perform(get(getPrepareFormValuesUrl())
	        		.with(authentication(getAuthentication(TestHelper.ADMIN_USERNAME)))
	        		.param(IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, formDefinition.getCode())
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andExpect(status().isOk())
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			IdmFormInstanceDto formInstance = toFormInstance(response);
			//
			Assert.assertEquals(formDefinition.getId().toString(), formInstance.getFormDefinition().getId().toString());
			Assert.assertEquals(formDefinition.getFormAttributes().get(0).getId().toString(), formInstance.getFormDefinition().getFormAttributes().get(0).getId().toString());
			Assert.assertTrue(formInstance.getValues().isEmpty());
		}
		//
		// test get values - empty
		IdmFormInstanceDto formInstance = getFormInstance(owner.getId(), TestHelper.ADMIN_USERNAME, formDefinition.getCode());
		Assert.assertEquals(owner.getId().toString(), formInstance.getOwnerId());
		Assert.assertEquals(formDefinition.getId().toString(), formInstance.getFormDefinition().getId().toString());
		Assert.assertEquals(formDefinition.getFormAttributes().get(0).getId().toString(), formInstance.getFormDefinition().getFormAttributes().get(0).getId().toString());
		Assert.assertTrue(formInstance.getValues().isEmpty());
		//
		// save values
		IdmFormValueDto formValue = new IdmFormValueDto(formAttribute);
		formValue.setValue(getHelper().createName());
		List<IdmFormValueDto> formValues = Lists.newArrayList(formValue);
		saveFormValues(owner.getId(), TestHelper.ADMIN_USERNAME, formDefinition.getCode(), formValues);
		
		// get saved values
		formInstance = getFormInstance(owner.getId(), TestHelper.ADMIN_USERNAME, formDefinition.getCode());
		Assert.assertEquals(owner.getId().toString(), formInstance.getOwnerId());
		Assert.assertEquals(formDefinition.getId().toString(), formInstance.getFormDefinition().getId().toString());
		Assert.assertEquals(formDefinition.getFormAttributes().get(0).getId().toString(), formInstance.getFormDefinition().getFormAttributes().get(0).getId().toString());
		Assert.assertEquals(1, formInstance.getValues().size());
		Assert.assertEquals(formValue.getShortTextValue(), formInstance.getValues().get(0).getShortTextValue());
	}
	
	@Test
	public void testSaveFormValue() throws Exception {
		if(!supportsFormValues()) {
			LOG.info("Controller [{}] doesn't support extended attributes. Method will not be tested.", getController().getClass());
			return;
		}
		IdmFormAttributeDto formAttribute = new IdmFormAttributeDto(getHelper().createName());
		IdmFormDefinitionDto formDefinition = formService.createDefinition(getFormOwnerType(), getHelper().createName(), Lists.newArrayList(formAttribute));
		formAttribute = formDefinition.getFormAttributes().get(0);
		//
		DTO owner = createDto();
		//
		// test get values - empty
		IdmFormInstanceDto formInstance = getFormInstance(owner.getId(), TestHelper.ADMIN_USERNAME, formDefinition.getCode());
		Assert.assertEquals(owner.getId().toString(), formInstance.getOwnerId());
		Assert.assertEquals(formDefinition.getId().toString(), formInstance.getFormDefinition().getId().toString());
		Assert.assertEquals(formDefinition.getFormAttributes().get(0).getId().toString(), formInstance.getFormDefinition().getFormAttributes().get(0).getId().toString());
		Assert.assertTrue(formInstance.getValues().isEmpty());
		//
		// save value
		IdmFormValueDto formValue = new IdmFormValueDto(formAttribute);
		formValue.setValue(getHelper().createName());
		saveFormValue(owner.getId(), TestHelper.ADMIN_USERNAME, formValue);
		
		// get saved values
		formInstance = getFormInstance(owner.getId(), TestHelper.ADMIN_USERNAME, formDefinition.getCode());
		Assert.assertEquals(owner.getId().toString(), formInstance.getOwnerId());
		Assert.assertEquals(formDefinition.getId().toString(), formInstance.getFormDefinition().getId().toString());
		Assert.assertEquals(formDefinition.getFormAttributes().get(0).getId().toString(), formInstance.getFormDefinition().getFormAttributes().get(0).getId().toString());
		Assert.assertEquals(1, formInstance.getValues().size());
		Assert.assertEquals(formValue.getShortTextValue(), formInstance.getValues().get(0).getShortTextValue());
		//
		getMockMvc().perform(post(getFormValueUrl(UUID.randomUUID()))
        		.with(authentication(getAuthentication(TestHelper.ADMIN_USERNAME)))
        		.content(getMapper().writeValueAsString(formValue))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isNotFound());
	}
	
	@Test
	public void testDownloadFormValue() throws Exception {
		if(!supportsFormValues()) {
			LOG.info("Controller [{}] doesn't support extended attributes. Method will not be tested.", getController().getClass());
			return;
		}
		IdmFormAttributeDto formAttribute = new IdmFormAttributeDto(getHelper().createName());
		formAttribute.setPersistentType(PersistentType.ATTACHMENT);
		IdmFormDefinitionDto formDefinition = formService.createDefinition(getFormOwnerType(), getHelper().createName(), Lists.newArrayList(formAttribute));
		formAttribute = formDefinition.getFormAttributes().get(0);
		//
		DTO owner = createDto();
		//
		// save value
		String content = "text";
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		UUID owwnerId = UUID.randomUUID();
		attachment.setOwnerId(owwnerId);
		attachment.setOwnerType(attachmentManager.getOwnerType(IdmFormValueDto.class));
		attachment.setName("test.txt");
		attachment.setMimetype("text/plain");
		attachment.setInputData(IOUtils.toInputStream(content));
		attachment = attachmentManager.saveAttachment(owner, attachment);
		
		IdmFormValueDto formValue = new IdmFormValueDto(formAttribute);
		formValue.setId(owwnerId);
		formValue.setValue(attachment.getId());
		IdmFormInstanceDto formInstance = saveFormValue(owner.getId(), TestHelper.ADMIN_USERNAME, formValue);
		Assert.assertEquals(owner.getId().toString(), formInstance.getOwnerId());
		Assert.assertEquals(formDefinition.getId().toString(), formInstance.getFormDefinition().getId().toString());
		Assert.assertEquals(formDefinition.getFormAttributes().get(0).getId().toString(), formInstance.getFormDefinition().getFormAttributes().get(0).getId().toString());
		Assert.assertEquals(1, formInstance.getValues().size());
		formValue = formInstance.getValues().get(0);
		//
		// download saved value
		String response = getMockMvc().perform(MockMvcRequestBuilders.get(getDownloadFormValuesUrl(owner.getId(), formValue.getId()))
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/plain"))
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		// preview for the text files is not available
		getMockMvc().perform(MockMvcRequestBuilders.get(getPreviewFormValuesUrl(owner.getId(), formValue.getId()))
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isNoContent());
		//
		// download is available for the attachment persistent type only
		IdmFormAttributeDto formAttributeWrong = new IdmFormAttributeDto(getHelper().createName());
		formAttributeWrong.setPersistentType(PersistentType.TEXT);
		IdmFormDefinitionDto formDefinitionWrong = formService.createDefinition(getFormOwnerType(), getHelper().createName(), Lists.newArrayList(formAttributeWrong));
		formAttributeWrong = formDefinitionWrong.getFormAttributes().get(0);
		IdmFormValueDto formValueWrong = new IdmFormValueDto(formAttributeWrong);
		formValueWrong.setValue(getHelper().createName());
		formInstance = saveFormValue(owner.getId(), TestHelper.ADMIN_USERNAME, formValueWrong);
		formValueWrong = formInstance.getValues().get(0);
		getMockMvc().perform(MockMvcRequestBuilders.get(getDownloadFormValuesUrl(owner.getId(), formValueWrong.getId()))
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isBadRequest());
		//
		// download is available for the filled attachments
		formValue.setValue(null);
		saveFormValue(owner.getId(), TestHelper.ADMIN_USERNAME, formValue);
		getMockMvc().perform(MockMvcRequestBuilders.get(getDownloadFormValuesUrl(owner.getId(), formValue.getId()))
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isNotFound());
		//
		// entity not found
		getMockMvc().perform(MockMvcRequestBuilders.get(getDownloadFormValuesUrl(owner.getId(), UUID.randomUUID()))
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isNotFound());
		getMockMvc().perform(MockMvcRequestBuilders.get(getDownloadFormValuesUrl(UUID.randomUUID(), formValue.getId()))
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isNotFound());
		//
		Assert.assertEquals(content, response);
	}
	
	@Test
	public void testPreviewFormValue() throws Exception {
		if(!supportsFormValues()) {
			LOG.info("Controller [{}] doesn't support extended attributes. Method will not be tested.", getController().getClass());
			return;
		}
		IdmFormAttributeDto formAttribute = new IdmFormAttributeDto(getHelper().createName());
		formAttribute.setPersistentType(PersistentType.ATTACHMENT);
		IdmFormDefinitionDto formDefinition = formService.createDefinition(getFormOwnerType(), getHelper().createName(), Lists.newArrayList(formAttribute));
		formAttribute = formDefinition.getFormAttributes().get(0);
		//
		DTO owner = createDto();
		//
		// save value
		String content = "wrong ... but image :)";
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		UUID owwnerId = UUID.randomUUID();
		attachment.setOwnerId(owwnerId);
		attachment.setOwnerType(attachmentManager.getOwnerType(IdmFormValueDto.class));
		attachment.setName("test.jpg");
		attachment.setMimetype("image/jpeg");
		attachment.setInputData(IOUtils.toInputStream(content));
		attachment = attachmentManager.saveAttachment(owner, attachment);
		
		IdmFormValueDto formValue = new IdmFormValueDto(formAttribute);
		formValue.setId(owwnerId);
		formValue.setValue(attachment.getId());
		IdmFormInstanceDto formInstance = saveFormValue(owner.getId(), TestHelper.ADMIN_USERNAME, formValue);
		Assert.assertEquals(owner.getId().toString(), formInstance.getOwnerId());
		Assert.assertEquals(formDefinition.getId().toString(), formInstance.getFormDefinition().getId().toString());
		Assert.assertEquals(formDefinition.getFormAttributes().get(0).getId().toString(), formInstance.getFormDefinition().getFormAttributes().get(0).getId().toString());
		Assert.assertEquals(1, formInstance.getValues().size());
		formValue = formInstance.getValues().get(0);
		//
		// preview saved value
		String response = getMockMvc().perform(MockMvcRequestBuilders.get(getPreviewFormValuesUrl(owner.getId(), formValue.getId()))
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("image/jpeg"))
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		// preview is available for the attachment persistent type only
		IdmFormAttributeDto formAttributeWrong = new IdmFormAttributeDto(getHelper().createName());
		formAttributeWrong.setPersistentType(PersistentType.TEXT);
		IdmFormDefinitionDto formDefinitionWrong = formService.createDefinition(getFormOwnerType(), getHelper().createName(), Lists.newArrayList(formAttributeWrong));
		formAttributeWrong = formDefinitionWrong.getFormAttributes().get(0);
		IdmFormValueDto formValueWrong = new IdmFormValueDto(formAttributeWrong);
		formValueWrong.setValue(getHelper().createName());
		formInstance = saveFormValue(owner.getId(), TestHelper.ADMIN_USERNAME, formValueWrong);
		formValueWrong = formInstance.getValues().get(0);
		getMockMvc().perform(MockMvcRequestBuilders.get(getPreviewFormValuesUrl(owner.getId(), formValueWrong.getId()))
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isBadRequest());
		//
		// download is available for the filled attachments
		formValue.setValue(null);
		saveFormValue(owner.getId(), TestHelper.ADMIN_USERNAME, formValue);
		getMockMvc().perform(MockMvcRequestBuilders.get(getPreviewFormValuesUrl(owner.getId(), formValue.getId()))
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isNotFound());
		//
		// entity not found
		getMockMvc().perform(MockMvcRequestBuilders.get(getPreviewFormValuesUrl(owner.getId(), UUID.randomUUID()))
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isNotFound());
		getMockMvc().perform(MockMvcRequestBuilders.get(getPreviewFormValuesUrl(UUID.randomUUID(), formValue.getId()))
        		.with(authentication(getAdminAuthentication())))
				.andExpect(status().isNotFound());
		//
		Assert.assertEquals(content, response);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testCheckAvailableBulkActions() throws Exception {
		if(!supportsBulkActions()) {
			LOG.info("Controller [{}] doesn't support bulk actions. Method will not be tested.", getController().getClass());
			return;
		}
		//
		Authentication authentication;
		if (!(getController().getService() instanceof AuthorizableService)) {
			authentication = getAdminAuthentication();
		} else {
			AuthorizableService<DTO> authorizableService = (AuthorizableService<DTO>) getController().getService();
			if (authorizableService.getAuthorizableType() == null) {
				// Some services can return null - internal transitive security can be implemented.
				authentication = getAdminAuthentication();
			} else {
				// create read policy - all bulk action should be secured under READ permission
				IdmRoleDto readRole = getHelper().createRole();
				getHelper().createBasePolicy(
						readRole.getId(), 
						authorizableService.getAuthorizableType().getGroup(),
						authorizableService.getAuthorizableType().getType(),
						IdmBasePermission.READ);
				// create test identity
				IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
				IdmIdentityContractDto contract = getHelper().createContract(identity);
				getHelper().createIdentityRole(contract, readRole);
				authentication = getAuthentication(identity.getUsername());
			}
		}
		String response = getMockMvc().perform(get(getBulkActionsUrl())
        		.with(authentication(authentication))
        		.contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		List<IdmBulkActionDto> actions = getMapper().readValue(response, new TypeReference<List<IdmBulkActionDto>>(){});
		//
		// if bulk actions are supported, then some action has to be returned (or override #supportsBulkActions method)
		Assert.assertFalse(actions.isEmpty());
		//
		// test prevalidate - just action is available under given authentication for each registered action 
		DTO dto = createDto();
		for (IdmBulkActionDto action : actions) {
			action.setIdentifiers(Sets.newHashSet(dto.getId()));
			//
			// prevalidate
			getMockMvc().perform(post(getBulkPrevalidateUrl())
	        		.with(authentication(authentication))
	        		.content(getMapper().writeValueAsString(action))
	        		.contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk());
			// execute without identifier is set ~ no change, but endpoint can be called
			action.setIdentifiers(null);
			if (!action.isShowWithoutSelection()
					&& action.isShowWithSelection()
					&& !action.isDisabled()) {
				Assert.assertNotNull(
						getMockMvc().perform(post(getBulkActionUrl())
			        		.with(authentication(authentication))
			        		.content(getMapper().writeValueAsString(action))
			        		.contentType(TestHelper.HAL_CONTENT_TYPE))
							.andReturn()
			                .getResponse()
			                .getContentAsString()
			    );
			}
		}
	}
	
	/**
	 * Find dtos by given filter. DataFilter should be fully implemented - only properties mapped in DATA will be used.
	 * 
	 * @param filter
	 * @return
	 */
	public List<DTO> find(String searchName, DataFilter filter, Authentication authentication) {
		return find(searchName, toQueryParams(filter), authentication);
	}
	
	/**
	 * Find dtos by given filter. DataFilter should be fully implemented - only properties mapped in DATA will be used.
	 * 
	 * @param filter
	 * @return
	 */
	public List<DTO> find(String searchName, DataFilter filter) {
		return find(searchName, filter, null);
	}
	
	/**
	 * Find dtos by given filter. DataFilter should be fully implemented - only properties mapped in DATA will be used.
	 * 
	 * @param filter
	 * @return
	 */
	public List<DTO> find(DataFilter filter) {
		return find(null, filter);
	}
	
	/**
	 * Returns json object mapper.
	 * Mapper is asociated by configured controller, but can be used for common json mapping.
	 * 
	 * @return
	 */
	@Override
	protected ObjectMapper getMapper() {
		return getController().getMapper();
	}
	
	/**
	 * Creates dto (saved)
	 * 
	 * @return
	 */
	protected DTO createDto() {
		return createDto(prepareDto());
	}
	
	/**
	 * Creates dto (saved). Auth internally.
	 * 
	 * @param dto
	 * @return
	 */
	protected DTO createDto(DTO dto) {
		try {
			getHelper().loginAdmin();
			return getController().saveDto(dto);
		} finally {
			getHelper().logout();
		}
	}
	
	/**
	 * Get dto by id. Auth internally.
	 * 
	 * @param id
	 * @return
	 */
	protected DTO getDto(UUID id) {
		try {
			getHelper().loginAdmin();
			return getController().getDto(id);
		} finally {
			getHelper().logout();
		}
	}
	
	/**
	 * Entry point url
	 * 
	 * @return
	 */
	protected String getBaseUrl() {
		Class<?> clazz = AopUtils.getTargetClass(getController());
	 
    	RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
    	if (mapping.value().length > 0) {
    		return mapping.value()[0];
    	}
    	if (mapping.path().length > 0) {
    		return mapping.path()[0];
    	}
		throw new CoreException("Controller [" + clazz + "] doeasn't have default mapping, cannot be tested by this abstraction.");
	}
	
	protected String getFindUrl(String searchName) {
		if (StringUtils.isEmpty(searchName)) {
			// quick alias endpoint on base url
			return getBaseUrl(); 
		}
		return String.format("%s%s/%s", getBaseUrl(), "/search", searchName); 
	}
	
	protected String getFindQuickUrl() {
		return getFindUrl("quick");
	}
	
	protected String getAutocompleteUrl() {
		return getFindUrl("autocomplete");
	}
	
	protected String getCountUrl() {
		return getFindUrl("count");
	}
	
	protected String getDetailUrl(Serializable backendId) {
		return String.format("%s/%s", getBaseUrl(), backendId);
	}
	
	protected String getPermissionsUrl(Serializable backendId) {
		return String.format("%s/%s/permissions", getBaseUrl(), backendId);
	}
	
	protected String getBulkActionsUrl() {
		return String.format("%s/bulk/actions", getBaseUrl());
	}
	
	protected String getBulkPrevalidateUrl() {
		return String.format("%s/bulk/prevalidate", getBaseUrl());
	}
	
	protected String getBulkActionUrl() {
		return String.format("%s/bulk/action", getBaseUrl());
	}
	
	protected String getFormDefinitionsUrl(Serializable backendId) {
		return String.format("%s/%s/form-definitions", getBaseUrl(), backendId);
	}
	
	protected String getBackendId(Serializable backendId) {
		if (backendId instanceof Identifiable) {
			return ((Identifiable) backendId).getId().toString();
		}
		return backendId.toString();
	}
	
	protected String getPrepareFormValuesUrl() {
		return String.format("%s/form-values/prepare", getBaseUrl());
	}
	
	protected String getFormValuesUrl(Serializable backendId) {
		return String.format("%s/%s/form-values", getBaseUrl(), getBackendId(backendId));
	}
	
	protected String getFormValueUrl(Serializable backendId) {
		return String.format("%s/%s/form-value", getBaseUrl(), getBackendId(backendId));
	}
	
	protected String getDownloadFormValuesUrl(Serializable backendId, UUID valueId) {
		return String.format("%s/%s/form-values/%s/download", getBaseUrl(), getBackendId(backendId), valueId);
	}
	
	protected String getPreviewFormValuesUrl(Serializable backendId, UUID valueId) {
		return String.format("%s/%s/form-values/%s/preview", getBaseUrl(), getBackendId(backendId), valueId);
	}
	
	/**
	 * Converts path (relative url) and filter parameters to string
	 * 
	 * @param filter
	 * @return
	 */
	protected String toUrl(String path, DataFilter filter) {
		UriComponents uriComponents = UriComponentsBuilder.fromPath(path).queryParams(toQueryParams(filter)).build();
		//
		return uriComponents.toString();
	}
	
	/**
	 * Returns dto's resource name defined by {@link Relation} annotation.
	 * 
	 * @return
	 */
	protected String getResourcesName() {
		return this.getResourcesName(getController().getDtoClass());
	}
	
	/**
	 * Find dtos
	 * 
	 * @param searchName [optional] quick alias is used as default
	 * @param parameters
	 * @param authentication [optional] admin authentication is used as default
	 * @return
	 */
	protected List<DTO> find(String searchName, MultiValueMap<String, String> parameters, Authentication authentication) {
		try {
			String response = getMockMvc().perform(get(getFindUrl(searchName))
	        		.with(authentication(authentication == null ? getAdminAuthentication() : authentication))
	        		.params(parameters)
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return toDtos(response);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
	
	/**
	 * Find dtos
	 * 
	 * @param searchName [optional] quick alias is used as default
	 * @param parameters
	 * @return
	 */
	protected List<DTO> find(String searchName, MultiValueMap<String, String> parameters) {
		return find(searchName, parameters, null);
	}
	
	/**
	 * Find dtos
	 * 
	 * @param parameters
	 * @return
	 */
	protected List<DTO> find(MultiValueMap<String, String> parameters) {
		return find(null, parameters);
	}
	
	/**
	 * Find dtos - "quick" is alias to standard find method. Required by all controllers.
	 * 
	 * @param parameters
	 * @return
	 */
	protected List<DTO> findQuick(MultiValueMap<String, String> parameters) {
		return find("quick", parameters);
	}
	
	/**
	 * Autocomplete dtos
	 * 
	 * @param parameters
	 * @return
	 */
	protected List<DTO> autocomplete(MultiValueMap<String, String> parameters) {
		return find("autocomplete", parameters);
	}
	
	/**
	 * Count records
	 * 
	 * @param parameters
	 * @return
	 */
	protected long count(MultiValueMap<String, String> parameters) {
		try {
			String response = getMockMvc().perform(get(getCountUrl())
	        		.with(authentication(getAdminAuthentication()))
	        		.params(parameters)
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return Long.parseLong(response);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
	
	/**
	 * Transform response with embedded dto list to dtos
	 * 
	 * @param listResponse
	 * @return
	 */
	protected List<DTO> toDtos(String listResponse) {
		return toDtos(listResponse, getController().getDtoClass());
	}
	
	/**
	 * Transform response into single dto
	 * 
	 * @param response
	 * @return
	 */
	protected DTO toDto(String response) {
		ObjectMapper mapper = getMapper();
		try {
			JsonNode json = mapper.readTree(response);
			//
			return mapper.convertValue(json, getController().getDtoClass());
		} catch (Exception ex) {
			throw new RuntimeException("Failed parse entity from response", ex);
		}
	}
	
	protected List<String> getPermissions(DTO dto, Authentication authentication) {
		//
		try {
			String response = getMockMvc().perform(get(getPermissionsUrl(dto.getId()))
	        		.with(authentication(authentication))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			// convert embedded object to list of strings
			return getMapper().readValue(response, new TypeReference<List<String>>(){});
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
	
	/**
	 * Returns available bulk actions for admin
	 * 
	 * @return
	 */
	protected List<IdmBulkActionDto> getAvailableBulkActions() {
		try {
			String response = getMockMvc().perform(get(getBulkActionsUrl())
	        		.with(authentication(getAdminAuthentication()))
	        		.contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return getMapper().readValue(response, new TypeReference<List<IdmBulkActionDto>>(){});
		} catch (Exception ex) {
			throw new RuntimeException("Failed to get available bulk actions", ex);
		}
	}
	
	/**
	 * Execute bulk action
	 * 
	 * @param action
	 * @return
	 */
	protected IdmBulkActionDto bulkAction(IdmBulkActionDto action) {
		try {
			String response = getMockMvc().perform(post(getBulkActionUrl())
	        		.with(authentication(getAdminAuthentication()))
	        		.content(getMapper().writeValueAsString(action))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isCreated())
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return getMapper().readValue(response, IdmBulkActionDto.class);
			// TODO: look out - READ_ONLY fields are not mapped
		} catch (Exception ex) {
			throw new RuntimeException("Failed to execute bulk action [" + action.getName() + "]", ex);
		}
	}
	
	/**
	 * Returns form value owner type - sometimes can be different than controlled dto type (e.g. slices, connectors)
	 * 
	 * @return
	 */
	protected Class<? extends Identifiable> getFormOwnerType() {
		return getController().getDtoClass();
	}
	
	/**
	 * Transform response with embedded dto list to dtos
	 * 
	 * @param response
	 * @return
	 */
	protected List<IdmFormDefinitionDto> toFormDefinitions(String response) {
		try {
			JsonNode json = getMapper().readTree(response);
			JsonNode jsonEmbedded = json.get(EmbeddedDto.PROPERTY_EMBEDDED); // by convention
			JsonNode jsonResources = jsonEmbedded.get(getResourcesName(IdmFormDefinitionDto.class));
			//
			// convert embedded object to target DTO classes
			List<IdmFormDefinitionDto> results = new ArrayList<>();
			jsonResources.forEach(jsonResource -> {
				results.add(getMapper().convertValue(jsonResource, IdmFormDefinitionDto.class));
			});
			//
			return results;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to read form definitioons from response [" + response + "]", ex);
		}
	}
	
	/**
	 * Transform response with embedded dto list to dtos
	 * 
	 * @param response
	 * @return
	 */
	protected IdmFormInstanceDto toFormInstance(String response) {
		try {
			return getMapper().readValue(response, IdmFormInstanceDto.class);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to read form instance from response [" + response + "]", ex);
		}
	}
	
	/**
	 * Get available form definitions for given owner under logged loginAs identity.
	 * 
	 * @param forOwner
	 * @param loginAs
	 * @return
	 * @throws Exception
	 */
	protected List<IdmFormDefinitionDto> getFormDefinitions(UUID forOwner, String loginAs) throws Exception {
		String response = getMockMvc().perform(get(getFormDefinitionsUrl(forOwner))
        		.with(authentication(getAuthentication(loginAs)))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		return toFormDefinitions(response);
	}
	
	/**
	 * Save form values
	 * 
	 * @param forOwner
	 * @param loginAs
	 * @param definitionCode
	 * @param formValues
	 * @throws Exception
	 */
	protected IdmFormInstanceDto saveFormValues(UUID forOwner, String loginAs, String definitionCode, List<IdmFormValueDto> formValues) throws Exception {
		String response = getMockMvc().perform(patch(getFormValuesUrl(forOwner))
        		.with(authentication(getAuthentication(loginAs)))
        		.param(IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, definitionCode)
        		.content(getMapper().writeValueAsString(formValues))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		return toFormInstance(response);
	}
	
	/**
	 * Save form values
	 * 
	 * @param forOwner
	 * @param loginAs
	 * @param definitionCode
	 * @param formValues
	 * @throws Exception
	 */
	protected IdmFormInstanceDto saveFormValue(UUID forOwner, String loginAs, IdmFormValueDto formValue) throws Exception {
		String response = getMockMvc().perform(post(getFormValueUrl(forOwner))
        		.with(authentication(getAuthentication(loginAs)))
        		.content(getMapper().writeValueAsString(formValue))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		return toFormInstance(response);
	}
	
	/**
	 * Get form values
	 * 
	 * @param forOwner
	 * @param loginAs
	 * @param definitionCode
	 * @return
	 * @throws Exception
	 */
	protected IdmFormInstanceDto getFormInstance(UUID forOwner, String loginAs, String definitionCode) throws Exception {
		String response = getMockMvc().perform(get(getFormValuesUrl(forOwner))
        		.with(authentication(getAuthentication(loginAs)))
        		.param(IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, definitionCode)
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		return toFormInstance(response);
	}
}
