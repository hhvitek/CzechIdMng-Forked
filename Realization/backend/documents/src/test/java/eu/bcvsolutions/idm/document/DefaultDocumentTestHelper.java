package eu.bcvsolutions.idm.document;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.document.domain.DocumentState;
import eu.bcvsolutions.idm.document.domain.DocumentType;
import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.service.api.DocumentService;

/**
 * Document test helper - custom test helper can be defined in modules.
*/
@Primary
@Component("documentTestHelper")
public class DefaultDocumentTestHelper extends eu.bcvsolutions.idm.test.api.DefaultTestHelper implements TestHelper {

	@Autowired private DocumentService documentService;


	@Override
	public DocumentDto createDocument(IdmIdentityDto identity) {
		DocumentDto document = getDocument(identity);
		DocumentDto savedDocument = documentService.save(document);
		return savedDocument;
	}

	@Override
	public DocumentDto getDocument(IdmIdentityDto identity) {
		DocumentDto dto = new DocumentDto();
		dto.setUuid(UUID.randomUUID());
		dto.setType(DocumentType.PASSPORT);
		dto.setNumber(1);
		dto.setFirstName(identity.getFirstName());
		dto.setLastName(identity.getLastName());
		dto.setState(DocumentState.VALID);
		dto.setIdentity(identity.getId());
		return dto;
	}
}
