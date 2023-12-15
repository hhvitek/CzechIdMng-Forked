package eu.bcvsolutions.idm.document.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;

/**
 * Example product service
 * 
 * @author Radek Tomiška
 *
 */
public interface DocumentService extends
		ReadWriteDtoService<DocumentDto, DocumentFilter>,
		AuthorizableService<DocumentDto> {

	DocumentDto getByUuid(String uuid);
	List<DocumentDto> findByText(String text);
}
