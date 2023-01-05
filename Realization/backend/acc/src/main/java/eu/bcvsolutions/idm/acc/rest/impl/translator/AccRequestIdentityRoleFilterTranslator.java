package eu.bcvsolutions.idm.acc.rest.impl.translator;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.FormableFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.OwnerTypeFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractPluggableFilterTranslator;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import static eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter.PARAMETER_ACCOUNT_ID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component
public class AccRequestIdentityRoleFilterTranslator extends AbstractPluggableFilterTranslator<IdmRequestIdentityRoleFilter> {

    @Autowired
    public AccRequestIdentityRoleFilterTranslator(LookupService lookupService, ObjectMapper objectMapper) {
        super(lookupService, objectMapper);
    }

    @Override
    public IdmRequestIdentityRoleFilter transformInternal(IdmRequestIdentityRoleFilter filter, MultiValueMap<String, Object> parameters) {
        filter.set(PARAMETER_ACCOUNT_ID, getParameterConverter().toUuid(parameters, PARAMETER_ACCOUNT_ID));
        return filter;
    }

    @Override
    protected IdmRequestIdentityRoleFilter getEmptyFilter() {
        return new IdmRequestIdentityRoleFilter();
    }
}
