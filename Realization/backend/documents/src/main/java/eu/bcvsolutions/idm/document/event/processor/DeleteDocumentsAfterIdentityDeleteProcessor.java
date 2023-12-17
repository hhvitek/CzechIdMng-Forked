package eu.bcvsolutions.idm.document.event.processor;

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

	public DeleteDocumentsAfterIdentityDeleteProcessor() {
		// processing identity DELETE event only
		super(IdentityEvent.IdentityEventType.DELETE);
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

		// TODO find all documents belonging to identity by identity id
		// we cannot lose identityId inside Documents table - foreign key cannot be set to null, must be ignore - invalid table state - no foreign key REFERENCES?

		// TODO deleteAll documents

		// result
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// right after identity delete
		return CoreEvent.DEFAULT_ORDER + 1;
	}
}
