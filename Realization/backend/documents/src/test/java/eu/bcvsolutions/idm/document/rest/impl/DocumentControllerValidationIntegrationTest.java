package eu.bcvsolutions.idm.document.rest.impl;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.document.TestHelper;
import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.service.api.DocumentService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

public class DocumentControllerValidationIntegrationTest extends AbstractIntegrationTest {
	@Autowired
	private TestHelper helper;
	@Autowired
	private DocumentService documentService;

	@Before
	public void init() {
		helper.loginAdmin();
	}

	@After
	public void logout() {
		helper.logout();
	}

	@Test
	public void createDocumentWithNonExistentIdentityTest() {
		IdmIdentityDto identity = helper.createIdentityOnly();
		DocumentDto document = helper.getDocument(identity);
		document.setIdentity(UUID.randomUUID());

		documentService.save(document);
	}
}
