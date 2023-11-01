package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordHistoryFilter;

/**
 * Service for check password history
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmPasswordHistoryService extends AbstractPasswordHistoryService<IdmPasswordHistoryDto, IdmPasswordHistoryFilter> {

}
