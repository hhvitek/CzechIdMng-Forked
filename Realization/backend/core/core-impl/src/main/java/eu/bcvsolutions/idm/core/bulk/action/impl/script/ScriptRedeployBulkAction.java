package eu.bcvsolutions.idm.core.bulk.action.impl.script;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRedeployBulkAction;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptFilter;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Redeploy given script definition
 *
 * @author Ondrej Husnik
 *
 */
@Component(ScriptRedeployBulkAction.NAME)
@Description("Redeploy given script definition.")
public class ScriptRedeployBulkAction extends AbstractRedeployBulkAction<IdmScriptDto, IdmScriptFilter> {

	public static final String NAME = "script-definition-redeploy-bulk-action";

	@Autowired
	private IdmScriptService scriptService;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.SCRIPT_UPDATE);
	}

	@Override
	public ReadWriteDtoService<IdmScriptDto, IdmScriptFilter> getService() {
		return scriptService;
	}
}
