package eu.bcvsolutions.idm.document.service.impl;

import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.document.TestHelper;
import eu.bcvsolutions.idm.document.domain.DocumentState;
import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.document.exception.DocumentValidationException;
import eu.bcvsolutions.idm.document.service.api.DocumentService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

@Transactional
public class DefaultDocumentServiceIntegrationTest extends AbstractIntegrationTest {
	@Autowired
	private TestHelper helper;
	@Autowired
	private DocumentService documentService;

	@Test(expected = DocumentValidationException.class)
	public void createDocumentWithNonExistentIdentityTest() {
		IdmIdentityDto identity = helper.createIdentityOnly();
		DocumentDto document = helper.getDocument(identity);

		Assert.assertEquals(documentService.count(new DocumentFilter()), 0);
		documentService.save(document);
		Assert.assertEquals(documentService.count(new DocumentFilter()), 1);

		DocumentDto document2 = helper.getDocument(identity);
		document2.setIdentity(UUID.randomUUID());

		documentService.save(document2);
	}

	@Test
	public void createDocumentWithInvalidFirstNameTest() {
		// create document and associated identity
		IdmIdentityDto identity = helper.createIdentityOnly();
		DocumentDto document = helper.getDocument(identity);
		document.setState(DocumentState.VALID);

		String originalFirstName = document.getFirstName();
		String newFirstName = "Non Existent First Name";

		// save document, expecting count of all documents to increase by one
		Assert.assertEquals(documentService.count(new DocumentFilter()), 0);
		DocumentDto beforeModification = documentService.save(document);
		Assert.assertEquals(documentService.count(new DocumentFilter()), 1);
		Assert.assertEquals(beforeModification.getFirstName(), originalFirstName);
		Assert.assertEquals(beforeModification.getState(), DocumentState.VALID);


		// change document's first name, identity's first name remain the same => should change document's state to INVALID
		beforeModification.setFirstName(newFirstName);
		DocumentDto afterModification = documentService.save(beforeModification);
		Assert.assertEquals(documentService.count(new DocumentFilter()), 1,
				"The document has already been created, save operation should update this document not create a new one.");
		Assert.assertEquals(afterModification.getFirstName(), newFirstName);
		Assert.assertEquals(afterModification.getState(), DocumentState.INVALID,
				"Because first name does not match associated identity's first name, stored document should have it's state changed to INVALID");

	}
}