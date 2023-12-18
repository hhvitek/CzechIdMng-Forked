package eu.bcvsolutions.idm.document;

import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.document.domain.DocumentState;
import eu.bcvsolutions.idm.document.domain.DocumentType;
import eu.bcvsolutions.idm.document.dto.DocumentDto;

/**
 * Document test helper - custom test helper can be defined in modules.
*/
@Primary
@Component("documentTestHelper")
public class DefaultDocumentTestHelper extends eu.bcvsolutions.idm.test.api.DefaultTestHelper implements TestHelper {

	@Override
	public DocumentDto getDocument(IdmIdentityDto identity) {
		DocumentDto dto = new DocumentDto();
		dto.setUuid(UUID.randomUUID());
		dto.setType(DocumentType.PASSPORT);
		dto.setNumber(1);
		dto.setFirstName(identity.getFirstName());
		dto.setLastName(identity.getLastName());
		dto.setState(DocumentState.INVALID);
		dto.setIdentity(identity.getId());
		return dto;
	}
}
