package eu.bcvsolutions.idm.document.dto.filter;

import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.document.TestHelper;
import eu.bcvsolutions.idm.document.domain.DocumentState;
import eu.bcvsolutions.idm.document.domain.DocumentType;
import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.service.api.DocumentService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

@Transactional
public class DocumentFilterIntegrationTest extends AbstractIntegrationTest {
	@Autowired
	private TestHelper helper;
	@Autowired
	private DocumentService documentService;

	@Test
	public void createAndFilterDocumentsTest() {
		IdmIdentityDto identity1 = helper.createIdentity();

		DocumentDto document1 = helper.getDocument(identity1);
		document1.setState(DocumentState.INVALID);
		document1.setType(DocumentType.PASSPORT);
		documentService.save(document1);

		DocumentDto document2 = helper.getDocument(identity1);
		document2.setState(DocumentState.VALID);
		document2.setType(DocumentType.PASSPORT);
		documentService.save(document2);

		DocumentFilter filter = new DocumentFilter();
		Assert.assertEquals(documentService.count(filter), 2);

		filter.setType(DocumentType.PASSPORT);
		Assert.assertEquals(documentService.count(filter), 2);

		filter.setType(DocumentType.ID_CARD);
		Assert.assertEquals(documentService.count(filter), 0);

		filter.setType(DocumentType.PASSPORT);
		filter.setState(DocumentState.INVALID);
		Assert.assertEquals(documentService.count(filter), 1);

		filter.setType(DocumentType.PASSPORT);
		filter.setState(DocumentState.VALID);
		Assert.assertEquals(documentService.count(filter), 1);

		filter.setType(null);
		filter.setState(null);
		filter.setIdentity(UUID.randomUUID());
		Assert.assertEquals(documentService.count(filter), 0);

		filter.setType(null);
		filter.setState(null);
		filter.setIdentity(identity1.getId());
		Assert.assertEquals(documentService.count(filter), 2);
	}

}