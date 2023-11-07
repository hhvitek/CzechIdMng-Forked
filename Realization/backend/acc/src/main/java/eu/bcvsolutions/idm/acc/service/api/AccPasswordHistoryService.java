package eu.bcvsolutions.idm.acc.service.api;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.AccPasswordHistoryDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordHistoryFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractPasswordHistoryService;

/**
 * Service for check account password history
 *
 * @author Jirka Koula
 *
 */
public interface AccPasswordHistoryService extends AbstractPasswordHistoryService<AccPasswordHistoryDto, AccPasswordHistoryFilter> {

}
