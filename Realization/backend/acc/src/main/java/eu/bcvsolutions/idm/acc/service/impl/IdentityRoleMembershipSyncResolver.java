package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmBaseConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmGeneralConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmConceptRoleRequestService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component
public class IdentityRoleMembershipSyncResolver extends AbstractRoleMembershipSyncResolver<IdmIdentityRoleDto, AccIdentityAccountDto, AccIdentityAccountFilter, IdmIdentityDto, IdmConceptRoleRequestDto> {

    private final IdmIdentityRoleService identityRoleService;
    private final DefaultAccIdentityAccountService identityAccountService;

    private final IdmIdentityContractService identityContractService;
    private final DefaultIdmConceptRoleRequestService conceptRoleRequestService;

    public IdentityRoleMembershipSyncResolver(IdmRoleRequestService roleRequestService, SysSystemEntityService systemEntityService, AccAccountService accountService,
            IdmIdentityRoleService identityRoleService, DefaultAccIdentityAccountService identityAccountService, IdmIdentityContractService identityContractService,
            DefaultIdmConceptRoleRequestService conceptRoleRequestService) {
        super(roleRequestService, systemEntityService, accountService);
        this.identityRoleService = identityRoleService;
        this.identityAccountService = identityAccountService;
        this.identityContractService = identityContractService;
        this.conceptRoleRequestService = conceptRoleRequestService;
    }

    @Override
    protected List<IdmIdentityRoleDto> getRoleAssignments(IdmRoleDto roleDto) {
        // Find identities with this role.
        IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
        identityRoleFilter.setRoleId(roleDto.getId());
        return identityRoleService.find(identityRoleFilter, null).getContent();
    }

    @Override
    protected String getEntityType() {
        return IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE;
    }

    @Override
    protected IdmGeneralConceptRoleRequestService<IdmIdentityRoleDto, IdmConceptRoleRequestDto, ? extends IdmBaseConceptRoleRequestFilter> getConceptService() {
        return conceptRoleRequestService;
    }

    @Override
    protected IdmIdentityDto getAccountOwner(AccIdentityAccountDto entityAccountDto) {
        return DtoUtils.getEmbedded(entityAccountDto, AccIdentityAccount_.identity, IdmIdentityDto.class);
    }


    @Override
    protected AbstractDto getAssignmentOwnerByAccountOwnerId(UUID accountOwnerId) {
        return identityContractService.getPrimeContract(accountOwnerId);
    }

    @Override
    protected ReadDtoService<AccIdentityAccountDto, AccIdentityAccountFilter> getAccountService() {
        return identityAccountService;
    }

    @Override
    protected AccIdentityAccountFilter getEmptyFilter() {
        return new AccIdentityAccountFilter();
    }

    @Override
    protected IdmIdentityDto getRequestOwnerFromAssignment(IdmIdentityRoleDto redundantIdentityRole) {
        final var contract = identityContractService.get(redundantIdentityRole.getIdentityContract());
        return DtoUtils.getEmbedded(contract, IdmIdentityContract_.identity, IdmIdentityDto.class);
    }

    @Override
    protected Class<IdmIdentityDto> getRequestOwnerType() {
        return IdmIdentityDto.class;
    }


}
