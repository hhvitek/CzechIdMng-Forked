package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.ApplicantImplDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityState_;
import eu.bcvsolutions.idm.core.model.event.ContractPositionEvent;
import eu.bcvsolutions.idm.core.model.event.ContractPositionEvent.ContractPositionEventType;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.model.event.processor.contract.ContractPositionAutomaticRoleProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.contract.IdentityContractUpdateByAutomaticRoleProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Recalculate automatic roles by tree structure for skipped contracts and positions.
 * Both contracts and positions are processed together.
 *
 * @author Radek Tomiška
 * @since 10.4.0
 */
@DisallowConcurrentExecution
@Component(ProcessSkippedAutomaticRoleByTreeForContractTaskExecutor.TASK_NAME)
public class ProcessSkippedAutomaticRoleByTreeForContractTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmEntityStateDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProcessSkippedAutomaticRoleByTreeForContractTaskExecutor.class);
	public static final String TASK_NAME = "core-process-skipped-automatic-role-by-tree-for-contract-long-running-task";
	//
	@Autowired private LookupService lookupService;
	@Autowired private EntityStateManager entityStateManager;
	@Autowired private IdentityContractUpdateByAutomaticRoleProcessor contractProcessor;
	@Autowired private ContractPositionAutomaticRoleProcessor positionProcessor;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	//
	private Set<UUID> processedOwnerIds = new HashSet<>(); // distinct owners will be processed

	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	public boolean requireNewTransaction() {
		return true;
	}
	
	@Override
	public boolean continueOnException() {
		return true;
	}
	
	@Override
	public boolean isRecoverable() {
		return true;
	}
	
	@Override
	public boolean supportsQueue() {
		return false;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		processedOwnerIds = new HashSet<>();
	}

	@Override
	public Page<IdmEntityStateDto> getItemsToProcess(Pageable pageable) {
		List<IdmEntityStateDto> states = new ArrayList<>();
		// find all states for flag
		IdmEntityStateFilter filter = new IdmEntityStateFilter();
		pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Direction.ASC, IdmEntityState_.created.getName()));
		filter.setStates(Lists.newArrayList(OperationState.BLOCKED));
		filter.setResultCode(CoreResultCode.AUTOMATIC_ROLE_SKIPPED.getCode());
		// for position
		filter.setOwnerType(entityStateManager.getOwnerType(IdmContractPositionDto.class));
		states.addAll(entityStateManager.findStates(filter, pageable).getContent());
		// for contract
		filter.setOwnerType(entityStateManager.getOwnerType(IdmIdentityContractDto.class));
		states.addAll(entityStateManager.findStates(filter, pageable).getContent());
		// for invalid contracts - has to be last => new roles are assigned already
		filter.setResultCode(CoreResultCode.AUTOMATIC_ROLE_SKIPPED_INVALID_CONTRACT.getCode());
		states.addAll(entityStateManager.findStates(filter, pageable).getContent());
		//
		return new PageImpl<>(states);
	}

	@Override
	public Optional<OperationResult> processItem(IdmEntityStateDto state) {
		UUID ownerId = state.getOwnerId();
		if (processedOwnerIds.contains(ownerId)) {
			LOG.debug("Automatic roles for owner [{}] was already processed, delete state only.", ownerId);
			// 
			entityStateManager.deleteState(state);
			// Item will be deleted only - we need some result - counter / count will match.
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		}
		//
		// process automatic role on state owner 
		if (state.getOwnerType().equals(entityStateManager.getOwnerType(IdmIdentityContractDto.class))) { // contract
			LOG.debug("Process automatic roles for contract [{}].", ownerId);
			//
			IdmIdentityContractDto contract = lookupService.lookupDto(IdmIdentityContractDto.class, ownerId);
			if (contract == null) {
				getItemService().createLogItem(
						state, 
						new OperationResult
							.Builder(OperationState.NOT_EXECUTED)
							.setModel(new DefaultResultModel(CoreResultCode.CONTENT_DELETED, ImmutableMap.of(
									"ownerId", ownerId,
									"ownerType", entityStateManager.getOwnerType(IdmIdentityContractDto.class))))
							.build(), 
						this.getLongRunningTaskId()
				);
			} else if(!contract.isValidNowOrInFuture()) {
				removeAllAutomaticRoles(contract);
				//
				getItemService().createLogItem(
						contract, 
						new OperationResult.Builder(OperationState.EXECUTED).build(), 
						this.getLongRunningTaskId()
				);
			} else {			
				EntityEvent<IdmIdentityContractDto> event = new IdentityContractEvent(IdentityContractEventType.NOTIFY, contract);
				event.setOriginalSource((IdmIdentityContractDto) state
						.getResult().getModel().getParameters().get(EntityEvent.EVENT_PROPERTY_ORIGINAL_SOURCE));
				contractProcessor.process(event);
				//
				getItemService().createLogItem(
						contract, 
						new OperationResult.Builder(OperationState.EXECUTED).build(), 
						this.getLongRunningTaskId()
				);
			}
		} else  { // position
			LOG.debug("Process automatic roles for position [{}].", ownerId);
			//
			IdmContractPositionDto position = lookupService.lookupDto(IdmContractPositionDto.class, ownerId);
			if (position == null) {
				getItemService().createLogItem(
						state, 
						new OperationResult
							.Builder(OperationState.NOT_EXECUTED)
							.setModel(new DefaultResultModel(CoreResultCode.CONTENT_DELETED, ImmutableMap.of(
									"ownerId", ownerId,
									"ownerType", entityStateManager.getOwnerType(IdmIdentityContractDto.class))))
							.build(), 
						this.getLongRunningTaskId()
				);
			} else {
				EntityEvent<IdmContractPositionDto> event = new ContractPositionEvent(ContractPositionEventType.NOTIFY, position);
				positionProcessor.process(event);
				//
				getItemService().createLogItem(
						position, 
						new OperationResult.Builder(OperationState.EXECUTED).build(), 
						this.getLongRunningTaskId()
				);
			}
		}
		processedOwnerIds.add(ownerId);
		entityStateManager.deleteState(state);
		//
		// Log added manually above - log processed contract / position instead of deleted entity state.
		return Optional.empty();
	}
	
	private void removeAllAutomaticRoles(IdmIdentityContractDto invalidContract) {
		UUID contractId = invalidContract.getId();
		UUID identityId = invalidContract.getIdentity();
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityContractId(contractId);
		filter.setAutomaticRole(Boolean.TRUE);
		filter.setDirectRole(Boolean.TRUE);
		//
		List<IdmIdentityRoleDto> contractRoles = identityRoleService.find(filter, null).getContent();
		List<IdmConceptRoleRequestDto> concepts = new ArrayList<>(contractRoles.size());
		for (IdmIdentityRoleDto identityRole : contractRoles) {
			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setIdentityRole(identityRole.getId());
			conceptRoleRequest.setAutomaticRole(identityRole.getAutomaticRole());
			conceptRoleRequest.setRole(identityRole.getRole());
			conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
			conceptRoleRequest.setIdentityContract(contractId);
			//
			concepts.add(conceptRoleRequest);
		}
		if (concepts.isEmpty()) {
			LOG.debug("invalid contract [{}] does not have assigned roles.", contractId);
			//
			return;
		}
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setState(RoleRequestState.CONCEPT);
		roleRequest.setExecuteImmediately(true); // without approval
		roleRequest.setApplicantInfo(new ApplicantImplDto(identityId, IdmIdentityDto.class.getCanonicalName()));
		roleRequest.setRequestedByType(RoleRequestedByType.AUTOMATICALLY);
		roleRequest = roleRequestService.save(roleRequest);
		//
		for (IdmConceptRoleRequestDto concept : concepts) {
			concept.setRoleRequest(roleRequest.getId());
			//
			conceptRoleRequestService.save(concept);
		}
		//
		// start event with skip check authorities
		RoleRequestEvent requestEvent = new RoleRequestEvent(RoleRequestEventType.EXCECUTE, roleRequest);
		requestEvent.getProperties().put(IdmIdentityRoleService.SKIP_CHECK_AUTHORITIES, Boolean.TRUE);
		// prevent to start asynchronous event before previous update event is completed. 
		requestEvent.setSuperOwnerId(identityId);
		//
		roleRequestService.startRequestInternal(requestEvent);
	}
}
