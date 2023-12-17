package eu.bcvsolutions.idm.document.service.impl;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.document.domain.DocumentGroupPermission;
import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.document.entity.DocumentEntity;
import eu.bcvsolutions.idm.document.repository.DocumentRepository;
import eu.bcvsolutions.idm.document.service.api.DocumentService;

/**
 * Default document service implementation
 *
 */
@Service("documentService")
public class DefaultDocumentService
		extends AbstractReadWriteDtoService<DocumentDto, DocumentEntity, DocumentFilter>
		implements DocumentService {
	
	private final DocumentRepository repository;

	public DefaultDocumentService(DocumentRepository repository) {
		super(repository);
		//
		this.repository = repository;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(DocumentGroupPermission.DOCUMENT, getEntityClass());
	}
}
