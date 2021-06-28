package eu.bcvsolutions.idm.core.monitoring.api.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.CoreModule;
import eu.bcvsolutions.idm.core.api.dto.filter.PermissionContext;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.MonitoringEvaluatorDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringResultFilter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Monitoring manager.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public interface MonitoringManager {
	
	/**
	 * Last monitoring results - results are evaluated on different instances, so synchronized cache is needed.
	 */
	String LAST_RESULT_CACHE_NAME = String.format("%s:monitoring-last-result-cache", CoreModule.MODULE_ID);

	/**
	 * Execute monitoring by publishing entity event.
	 * 
	 * @param monitoring monitoring configuration
	 * @return monitoring result
	 */
	void execute(IdmMonitoringDto monitoring, BasePermission... permission);
	
	/**
	 * Evaluate ~ process monitoring.
	 * Use {@link #execute(IdmMonitoringDto, BasePermission...) instead to call all registered processors
	 * 
	 * @param monitoring monitoring configuration
	 * @return monitoring result
	 */
	IdmMonitoringResultDto evaluate(IdmMonitoringDto monitoring);
	
	/**
	 * Returns supported evaluators definitions.
	 * 
	 * @return
	 */
	List<MonitoringEvaluatorDto> getSupportedEvaluators();
	
	/**
	 * Returns last monitoring results.
	 * Never throws {@link ForbiddenEntityException} - returning available dtos by given permissions (AND).
	 * 
	 * @param filter - level property is supported only!
	 * @param pageable - sort is not supported - results are sorted by order
	 * @param permission base permissions to evaluate (AND / OR by {@link PermissionContext})
	 * @return
	 */
	Page<IdmMonitoringResultDto> getLastResults(IdmMonitoringResultFilter filter, Pageable pageable, BasePermission... permission);
}