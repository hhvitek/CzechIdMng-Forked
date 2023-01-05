package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.ApplicantImplDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Recalculate all automatic role by attribute for all contracts.
 * Automatic roles was added by iterate over all {@link IdmAutomaticRoleAttributeDto}.
 * For each {@link IdmAutomaticRoleAttributeDto} will be founded all newly passed {@link IdmIdentityContractDto}
 * and {@link IdmIdentityContractDto} that contains automatic role and role must be removed.
 * <br />
 * <br />
 * For each contract is created maximal twice {@link IdmRoleRequestDto}. One request contains all newly assigned roles
 * and the second contains newly removed roles. This is now only one solution.
 * <br />
 * 
 * TODO: after some big refactoring can be processed all concept in one request.
 *
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
@DisallowConcurrentExecution
@Component(ProcessAllAutomaticRoleByAttributeTaskExecutor.TASK_NAME)
public class ProcessAllAutomaticRoleByAttributeTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	public static final String TASK_NAME = "core-process-all-automatic-role-attribute-long-running-task";
	private static final int DEFAULT_PAGE_SIZE_ROLE = 10;

	@Autowired
	private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	public Boolean process() {
		// found all IdmAutomaticRoleAttributeDto for process
		Page<IdmAutomaticRoleAttributeDto> toProcessOthers = automaticRoleAttributeService.findAllToProcess(
				null, 
				PageRequest.of(
						0, 
						DEFAULT_PAGE_SIZE_ROLE,
						new Sort(Direction.ASC, AbstractEntity_.id.getName())
				)
		);
		boolean canContinue = true;
		//
		this.counter = 0L;
		this.count = Long.valueOf(toProcessOthers.getTotalElements());
		//
		// others
		while (toProcessOthers.hasContent()) {
			for (IdmAutomaticRoleAttributeDto automaticAttribute : toProcessOthers) {
				// start recalculation
				processAutomaticRoleForContract(automaticAttribute);
				//
				counter++;
				canContinue = updateState();
				if (!canContinue) {
					break;
				}
			}
			if (!toProcessOthers.hasNext()) {
				break;
			}
			toProcessOthers = automaticRoleAttributeService.findAllToProcess(null, toProcessOthers.nextPageable());
		}
		//
		return Boolean.TRUE;
	}
	
	/**
	 * Start recalculation for automatic role. All identity roles (newly added and removed) will be added by {@link IdmRoleRequestDto}.
	 * But role request is created for each contract twice. One for newly added and one for newly removed. This is now only solutions.
	 *
	 * @param automaticRolAttributeDto
	 */
	private void processAutomaticRoleForContract(IdmAutomaticRoleAttributeDto automaticRolAttributeDto) {
		UUID automaticRoleId = automaticRolAttributeDto.getId();
		//
    	// process contracts
    	List<UUID> newPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, true, null).getContent();
    	List<UUID> newNotPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, false, null).getContent();
    	//
    	boolean canContinue = true;
		for (UUID contractId : newPassedContracts) {
			// Concepts that will be added
			List<AbstractConceptRoleRequestDto> concepts = new ArrayList<>();
			//
			IdmIdentityContractDto contract = identityContractService.get(contractId);
			//
    		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
    		concept.setIdentityContract(contract.getId());
    		concept.setValidFrom(contract.getValidFrom());
    		concept.setValidTill(contract.getValidTill());
    		concept.setRole(automaticRolAttributeDto.getRole());
    		concept.setAutomaticRole(automaticRoleId);
			concept.setOperation(ConceptRoleRequestOperation.ADD);
			concepts.add(concept);	

    		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
    		roleRequest.setConceptRoles(concepts);
    		roleRequest.setApplicantInfo(new ApplicantImplDto(contract.getIdentity(), IdmIdentityDto.class.getCanonicalName()));
    		roleRequest = roleRequestService.startConcepts(new RoleRequestEvent(RoleRequestEventType.EXCECUTE, roleRequest), null);

			canContinue = updateState();
			if (!canContinue) {
				break;
			}
    	}
    	//
		if (canContinue) {
    		for (UUID contractId : newNotPassedContracts) {
    			// Identity id is get from embedded identity role. This is little speedup.
    			UUID identityId = null;
    			//
    			IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
    			filter.setIdentityContractId(contractId);
    			filter.setAutomaticRoleId(automaticRoleId);
    			List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null).getContent();
    			// Concepts that will be added
    			List<AbstractConceptRoleRequestDto> concepts = new ArrayList<>(identityRoles.size());
    			for (IdmIdentityRoleDto identityRole : identityRoles) {
    				IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
        			concept.setIdentityContract(contractId);
        			concept.setRole(automaticRolAttributeDto.getRole());
        			concept.setAutomaticRole(automaticRoleId);
        			concept.setIdentityRole(identityRole.getId());
    				concept.setOperation(ConceptRoleRequestOperation.REMOVE);
    				concepts.add(concept);

    				if (identityId == null) {
    					IdmIdentityContractDto contractDto = DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.identityContract, IdmIdentityContractDto.class, null);
						identityId = contractDto.getIdentity();
    				}
    			}
    			
    			IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
    			roleRequest.setConceptRoles(concepts);
    			roleRequest.setApplicantInfo(new ApplicantImplDto(identityId, IdmIdentityDto.class.getCanonicalName()));
    			roleRequest = roleRequestService.startConcepts(new RoleRequestEvent(RoleRequestEventType.EXCECUTE, roleRequest), null);

    			canContinue = updateState();
    			if (!canContinue) {
    				break;
    			}
    		}
    	}
	}
	
	@Override
    public boolean isRecoverable() {
    	return true;
    }
}
