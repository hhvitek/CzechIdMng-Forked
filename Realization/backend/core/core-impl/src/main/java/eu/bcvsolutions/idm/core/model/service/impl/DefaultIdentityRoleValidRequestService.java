package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import java.time.ZonedDateTime;

import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleValidRequestEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRoleValidRequest;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleValidRequestRepository;

/**
 * Default implementation {@link IdmIdentityRoleValidRequestService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultIdentityRoleValidRequestService 
		extends AbstractReadWriteDtoService<IdmIdentityRoleValidRequestDto, IdmIdentityRoleValidRequest, EmptyFilter>
		implements IdmIdentityRoleValidRequestService {
	
	private final IdmIdentityRoleValidRequestRepository repository;
	private final EntityEventManager entityEventManager;
	
	@Autowired
	public DefaultIdentityRoleValidRequestService(
			IdmIdentityRoleValidRequestRepository repository, EntityEventManager entityEventManager) {
		super(repository);
		//
		this.repository = repository;
		this.entityEventManager = entityEventManager;
	}

	@Override
	public IdmIdentityRoleValidRequestDto createByIdentityRoleId(UUID identityRoleId) {
		IdmIdentityRoleValidRequestDto dto = toDto(repository.findOneByIdentityRole_Id(identityRoleId));
		//
		if (dto == null) {
			dto = new IdmIdentityRoleValidRequestDto();
			dto.setResult(new OperationResult.Builder(OperationState.CREATED).build());
			dto.setIdentityRole(identityRoleId);
		}
		//
		// just update modified date
		return this.save(dto);
	}

	@Override
	public List<IdmIdentityRoleValidRequestDto> findAllValid() {
		return this.findAllValidFrom(ZonedDateTime.now());
	}
	@Override
	public void publishOrIncrease(IdmIdentityRoleValidRequestDto validRequestDto) {
		try {
			// after success provisioning is request removed from db
			entityEventManager.process(new IdentityRoleValidRequestEvent<>(IdentityRoleValidRequestEvent.IdentityRoleValidRequestEventType.IDENTITY_ROLE_VALID, validRequestDto));
		} catch (RuntimeException e) {
			// log failed operation
			validRequestDto.increaseAttempt();
			validRequestDto.setResult(new OperationResult.Builder(OperationState.NOT_EXECUTED).setCause(e).build());

			this.save(validRequestDto);
		}
	}

	@Override
	public List<IdmIdentityRoleValidRequestDto> findAllValidFrom(ZonedDateTime from) {
		return toDtos(this.repository.findAllValidFrom(from.toLocalDate()), true);
	}

	@Override
	public List<IdmIdentityRoleValidRequestDto> findAllValidRequestForRoleId(UUID roleId) {
		return toDtos(repository.findAllByIdentityRole_Id(roleId), true);
	}

	@Override
	public List<IdmIdentityRoleValidRequestDto> findAllValidRequestForIdentityId(UUID identityId) {
		return toDtos(repository.findAllByIdentityRole_IdentityContract_Identity_Id(identityId), true);
	}

	@Override
	public void deleteAll(List<IdmIdentityRoleValidRequestDto> entities) {
		if (entities != null && !entities.isEmpty()) {
			for (IdmIdentityRoleValidRequestDto entity : entities) {
				this.deleteInternalById(entity.getId());
			}
		}
	}

	@Override
	public List<IdmIdentityRoleValidRequestDto> findAllValidRequestForIdentityRoleId(UUID identityRoleId) {
		return toDtos(repository.findAllByIdentityRole_Id(identityRoleId), true);

	}

	@Override
	public List<IdmIdentityRoleValidRequestDto> findAllValidRequestForIdentityContractId(
			UUID identityContractId) {
		return toDtos(repository.findAllByIdentityRole_IdentityContract_Id(identityContractId), true);
	}
}
