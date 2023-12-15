package eu.bcvsolutions.idm.document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.PropertyModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.entity.IdmConsoleLog;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.document.domain.DocumentGroupPermission;
import eu.bcvsolutions.idm.document.domain.DocumentResultCode;

/**
 * Document module descriptor.
 *
 */
@Component
@PropertySource("classpath:module-" + DocumentModuleDescriptor.MODULE_ID + ".properties")
@ConfigurationProperties(prefix = "module." + DocumentModuleDescriptor.MODULE_ID + ".build", ignoreUnknownFields = true, ignoreInvalidFields = true)
public class DocumentModuleDescriptor extends PropertyModuleDescriptor {

	public static final String MODULE_ID = "document";
	public static final String TOPIC_EXAMPLE = String.format("%s:example", MODULE_ID);
	
	@Override
	public String getId() {
		return MODULE_ID;
	}
	
	/**
	 * Enables links to swagger documention
	 */
	@Override
	public boolean isDocumentationAvailable() {
		return true;
	}
	
	@Override
	public List<NotificationConfigurationDto> getDefaultNotificationConfigurations() {
		List<NotificationConfigurationDto> configs = new ArrayList<>();
		configs.add(new NotificationConfigurationDto(
				TOPIC_EXAMPLE, 
				null, 
				IdmConsoleLog.NOTIFICATION_TYPE,
				"Example notification", 
				null));
		return configs;
	}
	
	@Override
	public List<GroupPermission> getPermissions() {
		return Arrays.asList(DocumentGroupPermission.values());
	}

	@Override
	public List<ResultCode> getResultCodes() {
		return Arrays.asList(DocumentResultCode.values());
	}
}
