package eu.bcvsolutions.idm.document.rest.impl;

import java.util.UUID;

import javax.validation.ConstraintViolationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.document.TestHelper;
import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;
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

	@Test(expected = ConstraintViolationException.class)
	public void createDocumentWithNonExistentIdentityTest() {
		IdmIdentityDto identity = helper.createIdentityOnly();

		DocumentDto document = helper.getDocument(identity);
		Assert.assertEquals(documentService.count(new DocumentFilter()), 0);
		DocumentDto savedDocument = documentService.save(document);
		Assert.assertEquals(documentService.count(new DocumentFilter()), 1);

		// issue with conversion from dto to entity - attribute identity...
		DocumentDto document2 = helper.getDocument(identity);
		document2.setIdentity(UUID.randomUUID());

		// should fail on something like sql constrain but fails on model mapper ignoring identity during dto to entity conversion
		// AbstractReadDtoEntity row 760 return modelMapper.map(dto, getEntityClass(dto));
		// Maybe its standard behaviour - automatic checking of existence fk identity entity and it simply fails later during entity validation
		documentService.save(document2);
	}
}
