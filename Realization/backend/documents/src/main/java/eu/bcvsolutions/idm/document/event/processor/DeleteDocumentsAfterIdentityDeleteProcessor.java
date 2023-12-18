package eu.bcvsolutions.idm.document.event.processor;

import java.util.UUID;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.document.DocumentModuleDescriptor;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.document.service.api.DocumentService;


/**
 * Deletes all remaining documents belonging to just deleted identity
 */
@Enabled(DocumentModuleDescriptor.MODULE_ID)
@Component("deleteDocumentsAfterIdentityDeleteProcessor")
@Description("Deletes all remaining documents belonging to just deleted identity")
public class DeleteDocumentsAfterIdentityDeleteProcessor
		extends CoreEventProcessor<IdmIdentityDto>
		implements IdentityProcessor {

	/**
	 * Processor's identifier - has to be unique by module
	 */
	public static final String PROCESSOR_NAME = "delete-documents-after-identity-delete-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteDocumentsAfterIdentityDeleteProcessor.class);

	private final DocumentService documentService;

	public DeleteDocumentsAfterIdentityDeleteProcessor(DocumentService documentService) {
		// processing identity DELETE event only
		super(IdentityEvent.IdentityEventType.DELETE);
		this.documentService = documentService;
	}

	@Override
	public String getName() {
		// processor's identifier - has to be unique by module
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		// event content - identity
		IdmIdentityDto deletedIdentity = event.getContent();
		// log
		LOG.info("Identity [{},{}] was deleted. Now deleting remaining identity's documents", deletedIdentity.getUsername(), deletedIdentity.getId());

		DocumentFilter filter = new DocumentFilter();
		filter.setIdentity(deletedIdentity.getId());
		for (UUID documentId: documentService.findIds(filter, null)) {
			LOG.info("Deleting document [{}].", documentId);
			documentService.deleteById(documentId);
		}

		// result
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// right after identity delete
		return CoreEvent.DEFAULT_ORDER + 1;
	}
}
