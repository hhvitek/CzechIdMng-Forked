package eu.bcvsolutions.idm.document.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.document.DocumentModuleDescriptor;

/**
 * Aggregate base permission. Name can't contain character '_' - its used for joining to authority name.
 */
public enum DocumentGroupPermission implements GroupPermission {
	
	DOCUMENT(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ,
			IdmBasePermission.CREATE,
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String EXAMPLE_PRODUCT_ADMIN = "DOCUMENT" + BasePermission.SEPARATOR + "ADMIN";
	public static final String EXAMPLE_PRODUCT_COUNT = "DOCUMENT" + BasePermission.SEPARATOR + "COUNT";
	public static final String EXAMPLE_PRODUCT_AUTOCOMPLETE = "DOCUMENT" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String EXAMPLE_PRODUCT_READ = "DOCUMENT" + BasePermission.SEPARATOR + "READ";
	public static final String EXAMPLE_PRODUCT_CREATE = "DOCUMENT" + BasePermission.SEPARATOR + "CREATE";
	public static final String EXAMPLE_PRODUCT_UPDATE = "DOCUMENT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String EXAMPLE_PRODUCT_DELETE = "DOCUMENT" + BasePermission.SEPARATOR + "DELETE";
	
	private final List<BasePermission> permissions;
	
	private DocumentGroupPermission(BasePermission... permissions) {
		this.permissions = Arrays.asList(permissions);
	}
	
	@Override
	public List<BasePermission> getPermissions() {		
		return permissions;
	}
	
	@Override
	public String getName() {
		return name();
	}	
	
	@Override
	public String getModule() {
		return DocumentModuleDescriptor.MODULE_ID;
	}
}
