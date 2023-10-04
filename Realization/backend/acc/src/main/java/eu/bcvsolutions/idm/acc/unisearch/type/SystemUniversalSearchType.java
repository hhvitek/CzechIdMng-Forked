package eu.bcvsolutions.idm.acc.unisearch.type;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractUniversalSearchType;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 *
 * Universal search for systems.
 *
 * @author Vít Švanda
 * @since 12.0.0
 */
@Enabled(module = CoreModuleDescriptor.MODULE_ID, property = SystemUniversalSearchType.PROPERTY_SEARCH_TYPE)
@Component(SystemUniversalSearchType.NAME)
public class SystemUniversalSearchType extends AbstractUniversalSearchType<SysSystemDto, SysSystemFilter> {

	public static final String PROPERTY_SEARCH_TYPE =
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "acc.universal-search-type.system.enabled";

	public static final String NAME = "system-universal-search-type";
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SecurityService securityService;

	@Override
	public Class<SysSystemDto> getOwnerType() {
		return SysSystemDto.class;
	}

	@Override
	protected Pageable getPageable(Pageable pageable) {
		Sort sort = Sort.by(Sort.Direction.ASC, SysSystem_.name.getName());

		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
	}

	protected SysSystemFilter createFilter(String text) {
		SysSystemFilter filter = new SysSystemFilter();
		filter.setText(text);
		return filter;
	}

	@Override
	protected ReadDtoService<SysSystemDto, SysSystemFilter> getService() {
		return systemService;
	}

	@Override
	protected boolean hasPermissions(BasePermission[] permission) {
		return securityService.hasAnyAuthority(AccGroupPermission.SYSTEM_READ);
	}

	@Override
	public int getOrder() {
		return 30;
	}
}
