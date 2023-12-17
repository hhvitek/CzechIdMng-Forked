package eu.bcvsolutions.idm.document.rest.impl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.document.DocumentModuleDescriptor;
import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

public class DocumentControllerValidationTest extends AbstractRestTest {

	@Autowired
	private ModuleService moduleService;
	@Autowired
	private eu.bcvsolutions.idm.document.TestHelper helper;

	@Before
	public void enableModule() {
		// enable document module
		moduleService.enable(DocumentModuleDescriptor.MODULE_ID);
	}

	@Test
	public void createIllegalDocumentTest() throws Exception {
		IdmIdentityDto identity = helper.createIdentityOnly();

		DocumentDto document = helper.getDocument(identity);
		document.setIdentity(null); // invalidating document
		getMockMvc().perform(post(DocumentController.DOCUMENT_BASE_PATH)
						.with(authentication(getAdminAuthentication()))
						.content(getMapper().writeValueAsString(document))
						.contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("DOCUMENTDTO_IDENTITY_NOTNULL")));

		document = helper.getDocument(identity);
		document.setFirstName(null); // invalidating document
		getMockMvc().perform(post(DocumentController.DOCUMENT_BASE_PATH)
						.with(authentication(getAdminAuthentication()))
						.content(getMapper().writeValueAsString(document))
						.contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("DOCUMENTDTO_FIRSTNAME_NOTEMPTY")));
	}
}
