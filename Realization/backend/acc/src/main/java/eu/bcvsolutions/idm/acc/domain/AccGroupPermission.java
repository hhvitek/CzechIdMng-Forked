package eu.bcvsolutions.idm.acc.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.RoleBasePermission;

/**
 * Aggregate base permission. Name can't contain character '_' - its used for joining to authority name.
 * 
 * @author Radek Tomiška
 *
 */
public enum AccGroupPermission implements GroupPermission {
	
	SYSTEM(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ,
			IdmBasePermission.CREATE,
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE,
			SystemBasePermission.PASSWORDFILTERCHANGE,
			SystemBasePermission.PASSWORDFILTERVALIDATE),
	// RT: commented till system agenda will be secured properly
	// SYSTEMATTRIBUTEMAPPING(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	ACCOUNT(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE, IdmBasePermission.AUTOCOMPLETE),
	IDENTITYACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	ROLEACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	TREEACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	ROLECATALOGUEACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	SYNCHRONIZATION(IdmBasePermission.CREATE, IdmBasePermission.UPDATE),
	CONTRACTACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	IDENTITYROLEACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	CONTRACTSLICEACCOUNT(IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	PROVISIONINGBREAK(
			IdmBasePermission.ADMIN,
			IdmBasePermission.READ, 
			IdmBasePermission.CREATE, 
			IdmBasePermission.UPDATE, 
			IdmBasePermission.DELETE),
	PROVISIONINGOPERATION(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.READ, 
			IdmBasePermission.UPDATE, 
			IdmBasePermission.DELETE),
	PROVISIONINGARCHIVE(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.READ),
	UNIFORMPASSWORD(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ,
			IdmBasePermission.CREATE,
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE),
	REMOTESERVER(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ,
			IdmBasePermission.CREATE,
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE),
	SYSTEMGROUP(
			IdmBasePermission.ADMIN,
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.COUNT,
			IdmBasePermission.READ,
			IdmBasePermission.CREATE,
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE),
	SYSTEMGROUPSYSTEM(
			IdmBasePermission.ADMIN,
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.COUNT,
			IdmBasePermission.READ,
			IdmBasePermission.CREATE,
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE),
	SYSTEMOWNER(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ,
			IdmBasePermission.CREATE,
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE),
	SYSTEMOWNERROLE(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ,
			IdmBasePermission.CREATE,
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE),
	ACCOUNTFORMVALUE(
			IdmBasePermission.ADMIN,
			IdmBasePermission.READ, 
			IdmBasePermission.CREATE, 
			IdmBasePermission.UPDATE, 
			IdmBasePermission.DELETE),
	ACCOUNTROLEASSIGNMENT(
			IdmBasePermission.ADMIN,
			IdmBasePermission.READ,
			IdmBasePermission.CREATE,
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE,
			IdmBasePermission.COUNT,
			IdmBasePermission.AUTOCOMPLETE,
			RoleBasePermission.CANBEREQUESTED
	);
			
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String SYSTEM_ADMIN = "SYSTEM" + BasePermission.SEPARATOR + "ADMIN";
	public static final String SYSTEM_COUNT = "SYSTEM" + BasePermission.SEPARATOR + "COUNT";
	public static final String SYSTEM_AUTOCOMPLETE = "SYSTEM" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String SYSTEM_READ = "SYSTEM" + BasePermission.SEPARATOR + "READ";
	public static final String SYSTEM_CREATE = "SYSTEM" + BasePermission.SEPARATOR + "CREATE";
	public static final String SYSTEM_UPDATE = "SYSTEM" + BasePermission.SEPARATOR + "UPDATE";
	public static final String SYSTEM_DELETE = "SYSTEM" + BasePermission.SEPARATOR + "DELETE";
	public static final String SYSTEM_PASSWORDFILTERCHANGE = "SYSTEM" + BasePermission.SEPARATOR + "PASSWORDFILTERCHANGE";
	public static final String SYSTEM_PASSWORDFILTERVALIDATE = "SYSTEM" + BasePermission.SEPARATOR + "PASSWORDFILTERVALIDATE";
	//
	// RT: commented till system agenda will be secured properly
//	public static final String SYSTEM_ATTRIBUTE_MAPPING_READ = "SYSTEMATTRIBUTEMAPPING" + BasePermission.SEPARATOR + "READ";
//	public static final String SYSTEM_ATTRIBUTE_MAPPING_CREATE = "SYSTEMATTRIBUTEMAPPING" + BasePermission.SEPARATOR + "CREATE";
//	public static final String SYSTEM_ATTRIBUTE_MAPPING_UPDATE = "SYSTEMATTRIBUTEMAPPING" + BasePermission.SEPARATOR + "UPDATE";
//	public static final String SYSTEM_ATTRIBUTE_MAPPING_DELETE = "SYSTEMATTRIBUTEMAPPING" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String ACCOUNT_AUTOCOMPLETE = "ACCOUNT" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String ACCOUNT_READ = "ACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String ACCOUNT_CREATE = "ACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String ACCOUNT_UPDATE = "ACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String ACCOUNT_DELETE = "ACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	public static final String ACCOUNT_PASSWORDCHANGE = "ACCOUNT" + BasePermission.SEPARATOR + "PASSWORDCHANGE";
	//
	public static final String IDENTITY_ACCOUNT_READ = "IDENTITYACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String IDENTITY_ACCOUNT_CREATE = "IDENTITYACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String IDENTITY_ACCOUNT_UPDATE = "IDENTITYACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String IDENTITY_ACCOUNT_DELETE = "IDENTITYACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String ROLE_ACCOUNT_READ = "ROLEACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String ROLE_ACCOUNT_CREATE = "ROLEACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String ROLE_ACCOUNT_UPDATE = "ROLEACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String ROLE_ACCOUNT_DELETE = "ROLEACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String TREE_ACCOUNT_READ = "TREEACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String TREE_ACCOUNT_CREATE = "TREEACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String TREE_ACCOUNT_UPDATE = "TREEACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String TREE_ACCOUNT_DELETE = "TREEACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String ROLE_CATALOGUE_ACCOUNT_READ = "ROLECATALOGUEACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String ROLE_CATALOGUE_ACCOUNT_CREATE = "ROLECATALOGUEACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String ROLE_CATALOGUE_ACCOUNT_UPDATE = "ROLECATALOGUEACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String ROLE_CATALOGUE_ACCOUNT_DELETE = "ROLECATALOGUEACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String SYNCHRONIZATION_CREATE = "SYNCHRONIZATION" + BasePermission.SEPARATOR + "CREATE";
	public static final String SYNCHRONIZATION_UPDATE = "SYNCHRONIZATION" + BasePermission.SEPARATOR + "UPDATE";
	//
	public static final String CONTRACT_ACCOUNT_READ = "CONTRACTACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String CONTRACT_ACCOUNT_CREATE = "CONTRACTACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String CONTRACT_ACCOUNT_UPDATE = "CONTRACTACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String CONTRACT_ACCOUNT_DELETE = "CONTRACTACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String CONTRACT_SLICE_ACCOUNT_READ = "CONTRACTSLICEACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String CONTRACT_SLICE_ACCOUNT_CREATE = "CONTRACTSLICEACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String CONTRACT_SLICE_ACCOUNT_UPDATE = "CONTRACTSLICEACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String CONTRACT_SLICE_ACCOUNT_DELETE = "CONTRACTSLICEACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String PROVISIONINGBREAK_ADMIN = "PROVISIONINGBREAK" + BasePermission.SEPARATOR + "ADMIN";
	public static final String PROVISIONINGBREAK_READ = "PROVISIONINGBREAK" + BasePermission.SEPARATOR + "READ";
	public static final String PROVISIONINGBREAK_CREATE = "PROVISIONINGBREAK" + BasePermission.SEPARATOR + "CREATE";
	public static final String PROVISIONINGBREAK_UPDATE = "PROVISIONINGBREAK" + BasePermission.SEPARATOR + "UPDATE";
	public static final String PROVISIONINGBREAK_DELETE = "PROVISIONINGBREAK" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String IDENTITY_ROLE_ACCOUNT_READ = "IDENTITYROLEACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String IDENTITY_ROLE_ACCOUNT_CREATE = "IDENTITYROLEACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String IDENTITY_ROLE_ACCOUNT_UPDATE = "IDENTITYROLEACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String IDENTITY_ROLE_ACCOUNT_DELETE = "IDENTITYROLEACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String PROVISIONING_OPERATION_ADMIN = "PROVISIONINGOPERATION" + BasePermission.SEPARATOR + "ADMIN";
	public static final String PROVISIONING_OPERATION_COUNT = "PROVISIONINGOPERATION" + BasePermission.SEPARATOR + "COUNT";
	public static final String PROVISIONING_OPERATION_READ = "PROVISIONINGOPERATION" + BasePermission.SEPARATOR + "READ";
	public static final String PROVISIONING_OPERATION_UPDATE = "PROVISIONINGOPERATION" + BasePermission.SEPARATOR + "UPDATE";
	public static final String PROVISIONING_OPERATION_DELETE = "PROVISIONINGOPERATION" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String PROVISIONING_ARCHIVE_ADMIN = "PROVISIONINGARCHIVE" + BasePermission.SEPARATOR + "ADMIN";
	public static final String PROVISIONING_ARCHIVE_COUNT = "PROVISIONINGARCHIVE" + BasePermission.SEPARATOR + "COUNT";
	public static final String PROVISIONING_ARCHIVE_READ = "PROVISIONINGARCHIVE" + BasePermission.SEPARATOR + "READ";
	public static final String PROVISIONING_ARCHIVE_DELETE = "PROVISIONINGARCHIVE" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String UNIFORM_PASSWORD_ADMIN = "UNIFORMPASSWORD" + BasePermission.SEPARATOR + "ADMIN";
	public static final String UNIFORM_PASSWORD_COUNT = "UNIFORMPASSWORD" + BasePermission.SEPARATOR + "COUNT";
	public static final String UNIFORM_PASSWORD_AUTOCOMPLETE = "UNIFORMPASSWORD" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String UNIFORM_PASSWORD_READ = "UNIFORMPASSWORD" + BasePermission.SEPARATOR + "READ";
	public static final String UNIFORM_PASSWORD_CREATE = "UNIFORMPASSWORD" + BasePermission.SEPARATOR + "CREATE";
	public static final String UNIFORM_PASSWORD_UPDATE = "UNIFORMPASSWORD" + BasePermission.SEPARATOR + "UPDATE";
	public static final String UNIFORM_PASSWORD_DELETE = "UNIFORMPASSWORD" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String REMOTESERVER_ADMIN = "REMOTESERVER" + BasePermission.SEPARATOR + "ADMIN";
	public static final String REMOTESERVER_COUNT = "REMOTESERVER" + BasePermission.SEPARATOR + "COUNT";
	public static final String REMOTESERVER_AUTOCOMPLETE = "REMOTESERVER" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String REMOTESERVER_READ = "REMOTESERVER" + BasePermission.SEPARATOR + "READ";
	public static final String REMOTESERVER_CREATE = "REMOTESERVER" + BasePermission.SEPARATOR + "CREATE";
	public static final String REMOTESERVER_UPDATE = "REMOTESERVER" + BasePermission.SEPARATOR + "UPDATE";
	public static final String REMOTESERVER_DELETE = "REMOTESERVER" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String SYSTEM_GROUP_ADMIN = "SYSTEMGROUP" + BasePermission.SEPARATOR + "ADMIN";
	public static final String SYSTEM_GROUP_AUTOCOMPLETE = "SYSTEMGROUP" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String SYSTEM_GROUP_COUNT = "SYSTEMGROUP" + BasePermission.SEPARATOR + "COUNT";
	public static final String SYSTEM_GROUP_READ = "SYSTEMGROUP" + BasePermission.SEPARATOR + "READ";
	public static final String SYSTEM_GROUP_CREATE = "SYSTEMGROUP" + BasePermission.SEPARATOR + "CREATE";
	public static final String SYSTEM_GROUP_UPDATE = "SYSTEMGROUP" + BasePermission.SEPARATOR + "UPDATE";
	public static final String SYSTEM_GROUP_DELETE = "SYSTEMGROUP" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String SYSTEM_GROUP_SYSTEM_ADMIN = "SYSTEMGROUPSYSTEM" + BasePermission.SEPARATOR + "ADMIN";
	public static final String SYSTEM_GROUP_SYSTEM_AUTOCOMPLETE = "SYSTEMGROUPSYSTEM" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String SYSTEM_GROUP_SYSTEM_COUNT = "SYSTEMGROUPSYSTEM" + BasePermission.SEPARATOR + "COUNT";
	public static final String SYSTEM_GROUP_SYSTEM_READ = "SYSTEMGROUPSYSTEM" + BasePermission.SEPARATOR + "READ";
	public static final String SYSTEM_GROUP_SYSTEM_CREATE = "SYSTEMGROUPSYSTEM" + BasePermission.SEPARATOR + "CREATE";
	public static final String SYSTEM_GROUP_SYSTEM_UPDATE = "SYSTEMGROUPSYSTEM" + BasePermission.SEPARATOR + "UPDATE";
	public static final String SYSTEM_GROUP_SYSTEM_DELETE = "SYSTEMGROUPSYSTEM" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String SYSTEMOWNER_COUNT = "SYSTEMOWNER" + BasePermission.SEPARATOR + "COUNT";
	public static final String SYSTEMOWNER_AUTOCOMPLETE = "SYSTEMOWNER" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String SYSTEMOWNER_READ = "SYSTEMOWNER" + BasePermission.SEPARATOR + "READ";
	public static final String SYSTEMOWNER_CREATE = "SYSTEMOWNER" + BasePermission.SEPARATOR + "CREATE";
	public static final String SYSTEMOWNER_UPDATE = "SYSTEMOWNER" + BasePermission.SEPARATOR + "UPDATE";
	public static final String SYSTEMOWNER_DELETE = "SYSTEMOWNER" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String SYSTEMOWNERROLE_COUNT = "SYSTEMOWNERROLE" + BasePermission.SEPARATOR + "COUNT";
	public static final String SYSTEMOWNERROLE_AUTOCOMPLETE = "SYSTEMOWNERROLE" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String SYSTEMOWNERROLE_READ = "SYSTEMOWNERROLE" + BasePermission.SEPARATOR + "READ";
	public static final String SYSTEMOWNERROLE_CREATE = "SYSTEMOWNERROLE" + BasePermission.SEPARATOR + "CREATE";
	public static final String SYSTEMOWNERROLE_UPDATE = "SYSTEMOWNERROLE" + BasePermission.SEPARATOR + "UPDATE";
	public static final String SYSTEMOWNERROLE_DELETE = "SYSTEMOWNERROLE" + BasePermission.SEPARATOR + "DELETE";

	public static final String ACCOUNT_FORM_VALUE_ADMIN = "ACCOUNTFORMVALUE" + BasePermission.SEPARATOR + "ADMIN";
	public static final String ACCOUNT_FORM_VALUE_READ = "ACCOUNTFORMVALUE" + BasePermission.SEPARATOR + "READ";
	public static final String ACCOUNT_FORM_VALUE_CREATE = "ACCOUNTFORMVALUE" + BasePermission.SEPARATOR + "CREATE";
	public static final String ACCOUNT_FORM_VALUE_UPDATE = "ACCOUNTFORMVALUE" + BasePermission.SEPARATOR + "UPDATE";
	public static final String ACCOUNT_FORM_VALUE_DELETE = "ACCOUNTFORMVALUE" + BasePermission.SEPARATOR + "DELETE";

	public static final String ACCOUNTROLEASSIGNMENT_ADMIN = "ACCOUNTFORMVALUE" + BasePermission.SEPARATOR + "ADMIN";
	public static final String ACCOUNTROLEASSIGNMENT_READ = "ACCOUNTROLEASSIGNMENT" + BasePermission.SEPARATOR + "READ";
	public static final String ACCOUNTROLEASSIGNMENT_AUTOCOMPLETE = "ACCOUNTROLEASSIGNMENT" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String ACCOUNTROLEASSIGNMENT_COUNT = "ACCOUNTROLEASSIGNMENT" + BasePermission.SEPARATOR + "COUNT";
	public static final String ACCOUNTROLEASSIGNMENT_CREATE = "ACCOUNTROLEASSIGNMENT" + BasePermission.SEPARATOR + "CREATE";
	public static final String ACCOUNTROLEASSIGNMENT_UPDATE = "ACCOUNTROLEASSIGNMENT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String ACCOUNTROLEASSIGNMENT_DELETE = "ACCOUNTROLEASSIGNMENT" + BasePermission.SEPARATOR + "DELETE";
	public static final String ACCOUNTROLEASSIGNMENT_CANBEREQUESTED = "ACCOUNTROLEASSIGNMENT" + BasePermission.SEPARATOR + "CANBEREQUESTED";

	private final List<BasePermission> permissions;

	private AccGroupPermission(BasePermission... permissions) {
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
		return AccModuleDescriptor.MODULE_ID;
	}
}
