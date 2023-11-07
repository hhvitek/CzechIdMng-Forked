package eu.bcvsolutions.idm.core.api.service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmBaseConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.adapter.AdaptableService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface IdmConceptRoleRequestManager extends AdaptableService<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter, IdmRequestIdentityRoleDto>
       , MultiResourceProvider<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter, IdmRequestIdentityRoleDto> {


    <C extends AbstractConceptRoleRequestDto> void save(C concept);

    <C extends AbstractConceptRoleRequestDto> IdmGeneralConceptRoleRequestService<AbstractRoleAssignmentDto, C, IdmBaseConceptRoleRequestFilter> getServiceForConcept(AbstractConceptRoleRequestDto concept);

    <C extends AbstractConceptRoleRequestDto> IdmGeneralConceptRoleRequestService<AbstractRoleAssignmentDto, C, IdmBaseConceptRoleRequestFilter>getServiceForConcept(Class<C> assignmentType);
    List<AbstractConceptRoleRequestDto> findAllByRoleRequest(UUID id, Pageable pageable, IdmBasePermission... permissions);

    Collection<AbstractConceptRoleRequestDto> findAllByRoleAssignment(UUID identityRoleId);

    Page<IdmRequestIdentityRoleDto> find(IdmRequestIdentityRoleFilter filter, Pageable pageable, BasePermission[] permission);

    List<AbstractConceptRoleRequestDto> getAllByRoleId(UUID roleId);

    List<AbstractConceptRoleRequestDto> getAllByRoleRequest(UUID requestId, ConceptRoleRequestOperation operation);
}
