package eu.bcvsolutions.idm.acc.service.impl;

import com.google.common.collect.Sets;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncRoleConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.ApplicantImplDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmBaseConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.service.IdmGeneralConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * This class is used for resolve role membership for various types of owners. It is used by {@link RoleMembershipSyncExecutor} when resolving group memberships and
 * assigning / removing roles to / from owners.
 * </p>
 * <p>
 *     Each implementation of this class is responsible for resolving role membership for one type of owner and one type of role assignment.
 * </p>
 *
 *
 * @param <R> Type of role assignment.
 * @param <A> Type of Entity account.
 * @param <F> Type of Entity account filter.
 * @param <O> Type of owner.
 * @param <C> Type of concept role request used for assigning / removing roles to / from owners.
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractRoleMembershipSyncResolver<
        R extends AbstractRoleAssignmentDto,
        A extends EntityAccountDto,
        F extends EntityAccountFilter,
        O extends Codeable,
        C extends AbstractConceptRoleRequestDto> implements RoleMembershipSyncResolver<O>{

    private final IdmRoleRequestService roleRequestService;
    private final SysSystemEntityService systemEntityService;
    private final AccAccountService accountService;

    protected AbstractRoleMembershipSyncResolver(IdmRoleRequestService roleRequestService, SysSystemEntityService systemEntityService, AccAccountService accountService) {
        this.roleRequestService = roleRequestService;
        this.systemEntityService = systemEntityService;
        this.accountService = accountService;
    }

    @Override
    public boolean resolve(
            IdmRoleDto roleDto,
            SysSyncRoleConfigDto config, SysSyncItemLogDto logItem, SysSystemDto userSystemDto, Set<String> membersUid, SynchronizationContext context, RoleMembershipSyncResolver.SynchronizationExecutorAcessor executorAcessor) {

        Set<UUID> assignmentsOwnersUuids = Sets.newHashSet();

        // Find identities with this role.
        List<R> existsIdentityRoleDtos = getRoleAssignments(roleDto);

        final int[] count = {0};
        membersUid.forEach(uid -> assignMissingIdentityRoles(roleDto, config, logItem, existsIdentityRoleDtos, assignmentsOwnersUuids, userSystemDto, count, uid, context, executorAcessor));

        if (!executorAcessor.checkForCancelAndFlush(config)) {
            return false;
        }

        // Remove redundant identity roles.
        List<R> redundantIdentityRoles = existsIdentityRoleDtos.stream()
                .filter(existsIdentityRole -> !assignmentsOwnersUuids.contains(existsIdentityRole.getEntity()))
                .collect(Collectors.toList());

        count[0] = 0;
        redundantIdentityRoles.forEach(redundantIdentityRole -> removeRedundantIdentityRoles(roleDto, config, logItem, count, redundantIdentityRole, executorAcessor));

        return true;
    }

    protected abstract List<R> getRoleAssignments(IdmRoleDto roleDto);

    /**
     * Assign missing identity roles.
     */
    private void assignMissingIdentityRoles(IdmRoleDto roleDto,
            SysSyncRoleConfigDto config,
            SysSyncItemLogDto logItem,
            List<R> existingRoleAssignments,
            Set<UUID> assignmentOwnersIds,
            SysSystemDto userSystemDto,
            int[] count,
            String uid,
            SynchronizationContext context, RoleMembershipSyncResolver.SynchronizationExecutorAcessor executorAcessor) {
        // On every 20th item will be hibernate flushed and check if sync was not ended.
        if (count[0] % 20 == 0 && count[0] > 0 && (!executorAcessor.checkForCancelAndFlush(config))) {
                return;
        }
        count[0]++;

        // Need to find account using SysSystemEntityDto uid, because uid of AccAccountDto can be different.
        SysSystemEntityFilter entityFilter = new SysSystemEntityFilter();
        entityFilter.setEntityType(getEntityType());
        entityFilter.setSystemId(userSystemDto.getId());
        entityFilter.setUid(uid);
        SysSystemEntityDto systemEntity = systemEntityService.find(entityFilter, null)
                .stream()
                .findFirst()
                .orElse(null);
        if (systemEntity == null) {
            return;
        }

        AccAccountFilter accAccountFilter = new AccAccountFilter();
        accAccountFilter.setSystemEntityId(systemEntity.getId());
        final UUID accAccountId = accountService.findIds(accAccountFilter, null)
                .stream()
                .findFirst()
                .orElse(null);
        if (accAccountId == null) {
            return;
        }

        F filter = getEmptyFilter();
        filter.setAccountId(accAccountId);
        A entityAccountDto = getAccountService().find(filter, null).getContent()
                .stream()
                .findFirst()
                .orElse(null);
        if (entityAccountDto == null)  {
            return;
        }
        O accountOwner = getAccountOwner(entityAccountDto);

        final var assignmentOwner = getAssignmentOwnerByAccountOwnerId(UUID.fromString(String.valueOf(accountOwner.getId())));
        if (assignmentOwner == null) {
            executorAcessor.addToItemLog(logItem, MessageFormat.format("!!Role was not assigned to the user [{0}], because primary contract was not found!!", uid));
            executorAcessor.initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, context.getLog(),
                    context.getActionLogs());
            return;
        }
        assignmentOwnersIds.add(assignmentOwner.getId());

        R existingAssignment = existingRoleAssignments.stream()
                .filter(assignment -> assignmentOwner.getId().equals(assignment.getEntity()))
                .findFirst()
                .orElse(null);
        if (existingAssignment != null){
            // Owner already has the role.
            return;
        }

        executorAcessor.addToItemLog(logItem, MessageFormat.format("Role is not assigned for user [{0}] and contract [{1}]. Role request for add role will be created.", uid, assignmentOwner.getId()));

        // Get cache with role-requests by identity-contract.
        Map<UUID, UUID> roleRequestCache = executorAcessor.getRoleRequestCache();

        // Get role-request for the primary contract from a cache. If no request is present, then create one.
        initRoleRequest(accountOwner, assignmentOwner, roleRequestCache, config, executorAcessor);

        UUID roleRequestId = roleRequestCache.get(assignmentOwner.getId());
        IdmRoleRequestDto mockRoleRequest = new IdmRoleRequestDto();
        mockRoleRequest.setId(roleRequestId);
        // Create a concept for assign a role to primary contract.
        final var addConcept = getConceptService().createEmptyConcept();
        addConcept.setRoleRequest(roleRequestId);
        addConcept.setOwnerUuid(assignmentOwner.getId());
        addConcept.setRole(roleDto.getId());
        addConcept.setOperation(ConceptRoleRequestOperation.ADD);
        getConceptService().save(addConcept);
    }

    protected abstract String getEntityType();

    protected abstract IdmGeneralConceptRoleRequestService<R, C, ? extends IdmBaseConceptRoleRequestFilter> getConceptService();

    protected abstract O getAccountOwner(A entityAccountDto);

    protected abstract AbstractDto getAssignmentOwnerByAccountOwnerId(UUID accountOwnerId);

    protected abstract ReadDtoService<A, F> getAccountService();

    protected abstract F getEmptyFilter();

    /**
     *  Remove redundant identity roles.
     */
    private void removeRedundantIdentityRoles(IdmRoleDto roleDto, SysSyncRoleConfigDto config, SysSyncItemLogDto logItem, int[] count,
            R redundantIdentityRole, RoleMembershipSyncResolver.SynchronizationExecutorAcessor executorAcessor) {
        // On every 20th item will be hibernate flushed and check if sync was not ended.
        if (count[0] % 20 == 0 && count[0] > 0 && (!executorAcessor.checkForCancelAndFlush(config))) {
                return;
        }
        count[0]++;

        final O owner = getRequestOwnerFromAssignment(redundantIdentityRole);
        final AbstractDto assignmentOwner = getAssignmentOwnerByAccountOwnerId(UUID.fromString(String.valueOf(owner.getId())));

        if (!config.isAssignRoleRemoveSwitch()) {
            executorAcessor.addToItemLog(logItem, MessageFormat.format("!!Role is assigned for username [{0}] and contract [{1}], but isn't member of role. Role will be not removed from a user, because removing of redundant roles is not allowed in this sync now!", owner.getCode(), assignmentOwner.getId()));
            return;
        }
        if (redundantIdentityRole.getAutomaticRole() != null) {
            executorAcessor.addToItemLog(logItem, MessageFormat.format("!!Role is assigned for username [{0}] and contract [{1}], but isn't member of role. Assigned role will be not removed, because the role was assigned by automatic role!", owner.getCode(), assignmentOwner.getId()));
            return;
        }
        if (redundantIdentityRole.getDirectRole() != null) {
            executorAcessor.addToItemLog(logItem, MessageFormat.format("!!Role is assigned for username [{0}] and contract [{1}], but isn't member of role. Assigned role will be not removed, because the role was assigned by business role!", owner.getCode(), assignmentOwner.getId()));
            return;
        }
        executorAcessor.addToItemLog(logItem, MessageFormat.format("Role is assigned for username [{0}] and contract [{1}], but isn't member of role. Assigned role will be removed.", owner.getCode(), assignmentOwner.getId()));
        // Get cache with role-requests by identity-contract.
        Map<UUID, UUID> roleRequestCache = executorAcessor.getRoleRequestCache();

        // Get role-request for the primary contract from a cache. If no request is present, then create one.
        initRoleRequest(getRequestOwnerFromAssignment(redundantIdentityRole), assignmentOwner, roleRequestCache, config, executorAcessor);
        UUID roleRequestId = roleRequestCache.get(assignmentOwner.getId());
        // Create a concept for remove  an assigned role.
        final var conceptToRemoveIdentityRole = getConceptService().createConceptToRemoveIdentityRole(redundantIdentityRole);
        conceptToRemoveIdentityRole.setRole(roleDto.getId());
        conceptToRemoveIdentityRole.setRoleRequest(roleRequestId);
        getConceptService().save(conceptToRemoveIdentityRole);
    }

    protected abstract O getRequestOwnerFromAssignment(R redundantIdentityRole);

    /**
     * Get role-request from a cache or create new one if no exist in a cache.
     */
    private void initRoleRequest(O requestOwner, AbstractDto assignmentOwner, Map<UUID, UUID> roleRequestCache, SysSyncRoleConfigDto config, RoleMembershipSyncResolver.SynchronizationExecutorAcessor executorAcessor) {
        if (!roleRequestCache.containsKey(assignmentOwner.getId())) {
            IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
            roleRequest.setApplicantInfo(new ApplicantImplDto(UUID.fromString(String.valueOf(requestOwner.getId())), getRequestOwnerType().getCanonicalName()));
            roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
            roleRequest.setState(RoleRequestState.CONCEPT);
            final String systemCode = executorAcessor.getSyncContext().getSystem().getCode();
            OperationResultDto systemResult = new OperationResultDto.Builder(OperationState.NOT_EXECUTED)
                    .setModel(new DefaultResultModel(AccResultCode.SYNC_OF_ROLES_COMMON_ROLE_REQUEST, Map.of("system", systemCode)))
                    .build();
            roleRequest.setSystemState(systemResult);
            roleRequest.addToLog(MessageFormat.format("Role-request created from ROLE sync with ID [{0}] on the system [{1}].", config.getId(), systemCode));
            roleRequest = roleRequestService.save(roleRequest);
            roleRequestCache.put(assignmentOwner.getId(), roleRequest.getId());
        }
    }

    protected abstract Class<O> getRequestOwnerType();
}
