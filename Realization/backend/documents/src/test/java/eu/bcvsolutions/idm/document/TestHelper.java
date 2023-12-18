package eu.bcvsolutions.idm.document;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.document.dto.DocumentDto;

/**
 * Reuses core TestHelper and adds document spec. methods
 *
 */
public interface TestHelper extends eu.bcvsolutions.idm.test.api.TestHelper {

	DocumentDto getDocument(IdmIdentityDto identity);
}
