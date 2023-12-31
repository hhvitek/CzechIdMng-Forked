package eu.bcvsolutions.idm.core.monitoring.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DisableableFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.InstanceIdentifiableFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;

/**
 * Filter for configured monitoring evaluators.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public class IdmMonitoringFilter extends DataFilter implements DisableableFilter, InstanceIdentifiableFilter {
	
	public static final String PARAMETER_INSTANCE_ID = InstanceIdentifiableFilter.PROPERTY_INSTANCE_ID;
	public static final String PARAMETER_EVALUATOR_TYPE = "evaluatorType";
	
	public IdmMonitoringFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmMonitoringFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmMonitoringFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmMonitoringDto.class, data, parameterConverter);
	}

	/**
	 * Monitoring evaluator type (canonical class name, equals).
	 * 
	 * @return canonical class name
	 */
	public String getEvaluatorType() {
		return getParameterConverter().toString(getData(), PARAMETER_EVALUATOR_TYPE);
	}

	/**
	 * Monitoring evaluator type (canonical class name, equals).
	 * 
	 * @param evaluatorType canonical class name
	 */
	public void setEvaluatorType(String evaluatorType) {
		set(PARAMETER_EVALUATOR_TYPE, evaluatorType);
	}
}
