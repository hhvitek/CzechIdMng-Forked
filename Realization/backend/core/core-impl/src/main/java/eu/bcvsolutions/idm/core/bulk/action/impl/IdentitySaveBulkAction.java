package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Bulk operation for save identity
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 * @deprecated @since 9.4.0 Use concrete bulk actions for execute ACM or provisioning only
 */
@Deprecated
@Description("Bulk action save identity.")
public class IdentitySaveBulkAction extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	public static final String NAME = "identity-save-bulk-action";
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Override
	protected OperationResult processDto(IdmIdentityDto dto) {
		identityService.save(dto);

		return new OperationResult(OperationState.EXECUTED);
	}
	
	@Override
	protected List<UUID> getAllEntities(IdmBulkActionDto action, StringBuilder description) {
		// all identities
		return identityService.findIds((Pageable) null, getPermissionForEntity()).getContent();
	}
	
	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.IDENTITY_READ, CoreGroupPermission.IDENTITY_UPDATE);
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 1500;
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
	
	@Override
	public boolean showWithoutSelection() {
		return true;
	}
}