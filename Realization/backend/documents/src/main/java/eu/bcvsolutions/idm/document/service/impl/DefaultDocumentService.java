package eu.bcvsolutions.idm.document.service.impl;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.document.domain.DocumentGroupPermission;
import eu.bcvsolutions.idm.document.domain.DocumentResultCode;
import eu.bcvsolutions.idm.document.domain.DocumentState;
import eu.bcvsolutions.idm.document.domain.DocumentType;
import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.document.entity.DocumentEntity;
import eu.bcvsolutions.idm.document.exception.DocumentValidationException;
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

	private final IdmIdentityService identityService;
	private final IdmIdentityRepository identityRepository;

	public DefaultDocumentService(DocumentRepository repository, IdmIdentityService identityService,
								  IdmIdentityRepository identityRepository) {
		super(repository);
		//
		this.identityService = identityService;
		this.identityRepository = identityRepository;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(DocumentGroupPermission.DOCUMENT, getEntityClass());
	}

	/**
	 * Validate additional business logic Document attributes.
	 * - do associated identity actually exists?
	 * - ensure that each identity can have only one VALID Document of each type.
	 * - if there is a mismatch in firstName and/or lastName between document and associated identity then mark document as INVALID
	 *
	 * @param dto Document dto
	 * @return
	 *
	 * @throws DocumentValidationException if validation fails
	 */
	@Override
	@Transactional
	public DocumentDto validateDto(DocumentDto dto) {
		DocumentDto dtoToValidate = super.validateDto(dto);

		// do associated identity actually exists?
		validateIdentityExists(dtoToValidate);
		// Ensure that each identity can have only one VALID Document of each type.
		validateIdentityDocumentStateAndType(dtoToValidate);

		boolean isValid = isValidFirstNameAndLastName(dtoToValidate);
		if (!isValid) {
			// if there is a mismatch in firstName and/or lastName between document and associated identity then mark document as INVALID
			dtoToValidate.setState(DocumentState.INVALID);
		}

		return dtoToValidate;
	}

	/**
	 * Ensures associated identity actually exists
	 */
	private void validateIdentityExists(DocumentDto dto) {
		boolean identityExists = identityRepository.existsById(dto.getIdentity());
		if (!identityExists) {
			throw new DocumentValidationException(
					DocumentResultCode.DOCUMENT_VALIDATION_IDENTITY_NOT_EXISTS_ERROR,
					ImmutableMap.of("identity", dto.getIdentity())
			);
		}
	}

	/**
	 * Ensure that each identity can have only one VALID Document of each type.
	 */
	private void validateIdentityDocumentStateAndType(DocumentDto dto) {
		UUID identity = dto.getIdentity();
		DocumentState documentState = dto.getState();
		DocumentType documentType = dto.getType();

		if (!documentState.isValid()) {
			return; // only valid documents are relevant, single identity can have many invalid documents
		}

		DocumentFilter filter = new DocumentFilter();
		filter.setType(documentType);
		filter.setState(DocumentState.VALID);
		filter.setIdentity(identity);

		Page<UUID> documentIds = findIds(filter, null);
		boolean foundConflictingDocument = !documentIds.isEmpty();
		if (!foundConflictingDocument) {
			return;
		}

		UUID alreadyExistingDocumentId = documentIds.getContent().get(0);
		boolean updatingThisDocument = alreadyExistingDocumentId.equals(dto.getId());
		if (updatingThisDocument) {
			return;
		}

		throw new DocumentValidationException(
				DocumentResultCode.DOCUMENT_VALIDATION_IDENTITY_STATE_TYPE_ERROR,
				ImmutableMap.of(
						"identity", identity,
						"state", documentState,
						"type", documentType,
						"id", alreadyExistingDocumentId
				)
		);
	}

	/**
	 * Compares associated identity's firstName and lastName to passed document firstName nad lastName attributes.
	 */
	private boolean isValidFirstNameAndLastName(DocumentDto dto) {
		IdmIdentityDto identity = identityService.get(dto.getIdentity());
		if (identity == null) {
			throw new NoSuchElementException("Identity not found.");
		}

		return StringUtils.equals(dto.getFirstName(), identity.getFirstName())
				&& StringUtils.equals(dto.getLastName(), identity.getLastName());
	}

}
