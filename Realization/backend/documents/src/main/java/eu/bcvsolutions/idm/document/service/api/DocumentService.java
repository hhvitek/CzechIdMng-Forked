package eu.bcvsolutions.idm.document.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;

/**
 * Document service
 *
 */
public interface DocumentService extends
		ReadWriteDtoService<DocumentDto, DocumentFilter>,
		AuthorizableService<DocumentDto> {
}
