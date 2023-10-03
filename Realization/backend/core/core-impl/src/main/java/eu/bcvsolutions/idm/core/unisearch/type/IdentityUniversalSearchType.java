package eu.bcvsolutions.idm.core.unisearch.type;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractUniversalSearchType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 *
 * Universal search for identities.
 *
 * @author Vít Švanda
 * @since 12.0.0
 */
@Enabled(module = CoreModuleDescriptor.MODULE_ID, property = IdentityUniversalSearchType.PROPERTY_SEARCH_TYPE)
@Component(IdentityUniversalSearchType.NAME)
public class IdentityUniversalSearchType extends AbstractUniversalSearchType<IdmIdentityDto, IdmIdentityFilter> {

	public static final String PROPERTY_SEARCH_TYPE =
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.universal-search-type.identity.enabled";

	public static final String NAME = "identity-universal-search-type";
	@Autowired
	private IdmIdentityService identityService;

	@Override
	public Class<IdmIdentityDto> getOwnerType() {
		return IdmIdentityDto.class;
	}

	protected IdmIdentityFilter createFilter(String text) {
		IdmIdentityFilter filter = new IdmIdentityFilter();
		if (StringUtils.isNotEmpty(text)) {
			text = text.trim();
			// Workaround for simulate an indexing. If text contains space, then we will search in first and last name separately.
			String[] split = text.split(" ");
			if (split.length == 2) {
				filter.setFirstNameLike(split[0]);
				filter.setLastNameLike(split[1]);
				return filter;
			}
		}
		filter.setText(text);
		return filter;
	}

	@Override
	protected Pageable getPageable(Pageable pageable) {
		Sort sort = Sort.by(Sort.Direction.ASC, IdmIdentity_.username.getName());
		
		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
	}

	@Override
	protected ReadDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}

	@Override
	public int getOrder() {
		return 10;
	}
}
