package eu.bcvsolutions.idm.document.service.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitTestDataProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;
import eu.bcvsolutions.idm.document.DocumentModuleDescriptor;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Example service - rest tests
 * 
 * @author Radek Tomiška
 * 
 */
public class ExampleControllerRestTest extends AbstractRestTest {

	@Autowired private IdmIdentityService identityService;
	@Autowired private ModuleService moduleService;
	
	private Authentication getAuthentication() {
		return new IdmJwtAuthentication(identityService.getByUsername(InitTestDataProcessor.TEST_ADMIN_USERNAME), null, Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()), "test");
	}
	
	@Before
	public void enableModule() {
		// enable example module
		moduleService.enable(DocumentModuleDescriptor.MODULE_ID);
	}
	
	@Test
	public void testPing() throws Exception {
		String message = "test";
		getMockMvc().perform(get(BaseController.BASE_PATH + "/examples/ping")
				.with(authentication(getAuthentication()))
				.param("message", message)
				.contentType(TestHelper.HAL_CONTENT_TYPE))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.message", equalTo(message)));
    }
}
