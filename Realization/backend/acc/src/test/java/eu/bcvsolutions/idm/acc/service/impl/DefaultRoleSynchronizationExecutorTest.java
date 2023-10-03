package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSyncIdentityConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component
public class DefaultRoleSynchronizationExecutorTest extends AbstractRoleSynchronizationExecutorTest<IdmIdentityDto> {

    @Autowired
    SysSystemMappingService systemMappingService;
    @Autowired
    SysSyncConfigService syncConfigService;
    @Autowired
    private IdmIdentityService identityService;
    @Autowired
    private IdmIdentityRoleService identityRoleService;
    @Autowired
    private IdmRoleService roleService;
    @Autowired
    private SysSystemAttributeMappingService attributeMappingService;

    @Override
    protected SysSystemMappingDto getTargetMapping(SysSystemDto userSystem) {
        List<SysSystemMappingDto> userSystemMappings = systemMappingService.findBySystem(userSystem, SystemOperationType.PROVISIONING, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
        Assert.assertNotNull(userSystemMappings);
        Assert.assertEquals(1, userSystemMappings.size());
        SysSystemMappingDto userMappingDto = userSystemMappings.get(0);
        // Switch to the sync.
        userMappingDto.setOperationType(SystemOperationType.SYNCHRONIZATION);
        return systemMappingService.save(userMappingDto);
    }

    @Override
    protected void assignRoleToTarget(IdmIdentityDto target, UUID role) {
        IdmIdentityContractDto primeContract = getHelper().getPrimeContract(target);
        getHelper().assignRoles(primeContract, roleService.get(role));
    }

    @Override
    protected String getEntityType() {
        return IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE;
    }

    @Override
    protected SysSystemAttributeMappingDto prepareAttributeMappings(SysSystemMappingDto targetMapping) {
        List<SysSystemAttributeMappingDto> attributeMappingDtos = attributeMappingService.findBySystemMapping(targetMapping);
        SysSystemAttributeMappingDto userEmailAttribute = attributeMappingDtos.stream()
                .filter(attribute -> attribute.getName().equalsIgnoreCase(TestHelper.ATTRIBUTE_MAPPING_EMAIL))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(userEmailAttribute);
        SysSystemAttributeMappingDto enableAttribute = attributeMappingDtos.stream()
                .filter(attribute -> attribute.getName().equalsIgnoreCase(TestHelper.ATTRIBUTE_MAPPING_ENABLE))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(enableAttribute);
        enableAttribute.setDisabledAttribute(true);
        attributeMappingService.save(enableAttribute);
        return enableAttribute;
    }

    @Override
    protected List<AbstractRoleAssignmentDto> getAssignmentsForRoleAndTarget(UUID roleId, IdmIdentityDto identityOne) {
        IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
        identityRoleFilter.setRoleId(roleId);
        identityRoleFilter.setIdentityId(identityOne.getId());
        final List<IdmIdentityRoleDto> content = identityRoleService.find(identityRoleFilter, null).getContent();
        // Type shenanigans
        return new ArrayList<>(content);
    }

    @Override
    protected IdmIdentityDto getTargetByCode(String code) {
        return identityService.getByCode(code);
    }


}
