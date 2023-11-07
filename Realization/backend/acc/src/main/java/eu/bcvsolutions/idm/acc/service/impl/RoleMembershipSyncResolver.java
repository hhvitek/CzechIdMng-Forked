package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncRoleConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface RoleMembershipSyncResolver<O extends Codeable> {

    boolean resolve(
            IdmRoleDto roleDto,
            SysSyncRoleConfigDto config, SysSyncItemLogDto logItem, SysSystemDto userSystemDto, Set<String> membersUid, SynchronizationContext context,
            SynchronizationExecutorAcessor executorAcessor);

    interface SynchronizationExecutorAcessor {

        void addToItemLog(Loggable logItem, String text);

        Map<UUID, UUID> getRoleRequestCache();

        void initSyncActionLog(SynchronizationActionType actionType, OperationResultType resultType,
                SysSyncItemLogDto logItem, SysSyncLogDto log, List<SysSyncActionLogDto> actionLogs);

        boolean checkForCancelAndFlush(SysSyncRoleConfigDto log);

        SynchronizationContext getSyncContext();
    }
}
