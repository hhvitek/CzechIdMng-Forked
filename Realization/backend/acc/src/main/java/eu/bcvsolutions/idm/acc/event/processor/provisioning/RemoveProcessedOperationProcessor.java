package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Archives processed provisioning operations.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Archives processed provisioning operation.")
public class RemoveProcessedOperationProcessor extends AbstractEntityEventProcessor<SysProvisioningOperation> {
	
	public static final String PROCESSOR_NAME = "remove-processed-operation-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RemoveProcessedOperationProcessor.class);
	private final SysProvisioningOperationService provisioningOperationService;
	
	@Autowired
	public RemoveProcessedOperationProcessor(
			SysProvisioningOperationService provisioningOperationService,
			SysProvisioningArchiveService provisioningArchiveService) {
		super(ProvisioningOperationType.CREATE, ProvisioningOperationType.UPDATE, ProvisioningOperationType.DELETE, ProvisioningOperationType.CANCEL);
		//
		Assert.notNull(provisioningOperationService);
		//
		this.provisioningOperationService = provisioningOperationService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<SysProvisioningOperation> process(EntityEvent<SysProvisioningOperation> event) {
		SysProvisioningOperation provisioningOperation = event.getContent();
		if (OperationState.EXECUTED.equals(provisioningOperation.getResultState()) 
				|| ProvisioningOperationType.CANCEL.equals(event.getType())) {
			provisioningOperationService.delete(provisioningOperation);
			LOG.debug("Executed provisioning operation [{}] was removed from queue.", provisioningOperation.getId());
		}
		return new DefaultEventResult<>(event, this, isClosable());
	}
	
	@Override
	public boolean isClosable() {
		return true;
	}
	
	@Override
	public int getOrder() {
		// on the end
		return 5000;
	}
}
