package eu.bcvsolutions.idm.document.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	
	@Override
	@Transactional(readOnly = true)
	public DocumentDto getByUuid(String uuid) {
		return toDto(repository.findOneByUuid(uuid));
	}

	@Override
	public List<DocumentDto> findByText(String text) {
		return null;
	}
}
