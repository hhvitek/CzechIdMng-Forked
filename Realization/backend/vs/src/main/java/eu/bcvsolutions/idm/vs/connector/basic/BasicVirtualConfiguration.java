package eu.bcvsolutions.idm.vs.connector.basic;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.UUID;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfigurationClass;
import eu.bcvsolutions.idm.ic.api.annotation.IcConfigurationClassProperty;
import eu.bcvsolutions.idm.ic.exception.IcException;

/**
 * Configuration for virtual system
 * 
 * @author svandav
 *
 */
public class BasicVirtualConfiguration implements IcConnectorConfigurationClass {

	private static final long serialVersionUID = 1L;
	public static final String FACE_IDENTITY_SELECT = "IDENTITY-SELECT";
	public static final String FACE_ROLE_SELECT = "ROLE-SELECT";
	public static final String[] DEFAULT_ATTRIBUTES = { 
			IdmIdentity_.firstName.getName(), IdmIdentity_.lastName.getName(),
			IdmIdentity_.email.getName(), IdmIdentity_.titleAfter.getName(), IdmIdentity_.titleBefore.getName(),
			IdmIdentity_.phone.getName() };
	private String[] attributes = DEFAULT_ATTRIBUTES;
	private UUID[] implementers;
	private UUID[] implementerRoles;
	private boolean resetPasswordSupported = false;
	private boolean disableSupported = true;
	private boolean requiredConfirmation = true;
	private String[] reservedNames = { "uid", "__NAME__", "enable", "__ENABLE__", "__PASSWORD__" };

	@IcConfigurationClassProperty(
			order = 5, 
			displayName = "Required confirmation by the implementer", 
			helpMessage = "If not checked, then all requests will be solved immediately. No notification will be sent to implementers.")
	public boolean isRequiredConfirmation() {
		return requiredConfirmation;
	}

	public void setRequiredConfirmation(boolean requiredConfirmation) {
		this.requiredConfirmation = requiredConfirmation;
	}

	@IcConfigurationClassProperty(order = 10, required = true, displayName = "Attributes", helpMessage = "Properties for create EAV model.")
	public String[] getAttributes() {
		return attributes;
	}

	public void setAttributes(String[] attributes) {
		this.attributes = attributes;
	}

	@IcConfigurationClassProperty(
			order = 20, 
			face = FACE_IDENTITY_SELECT, 
			displayName = "Implementers",
			helpMessage = "Requests for implementing changes on the real system will be created for these implementers. Every implementer must be an identity in CzechIdM. Values are UUIDs of identities (multivalued).")
	public UUID[] getImplementers() {
		return implementers;
	}

	public void setImplementers(UUID[] implementers) {
		this.implementers = implementers;
	}

	@IcConfigurationClassProperty(
			order = 30, 
			face = FACE_ROLE_SELECT, 
			displayName = "Roles of implementers", 
			helpMessage = "All identities with these roles will be implementers. Every role must be a role in CzechIdM. Values are UUIDs of roles (multivalued).")
	public UUID[] getImplementerRoles() {
		return implementerRoles;
	}

	public void setImplementerRoles(UUID[] realizatorRoles) {
		this.implementerRoles = realizatorRoles;
	}

	// Not implemented yet! @IcConfigurationClassProperty(order = 40, displayName = "Supports password reset")
	public boolean isResetPasswordSupported() {
		return resetPasswordSupported;
	}

	public void setResetPasswordSupported(boolean changePasswordSupported) {
		this.resetPasswordSupported = changePasswordSupported;
	}

	@IcConfigurationClassProperty(order = 35, displayName = "Supports account disable/enable")
	public boolean isDisableSupported() {
		return disableSupported;
	}

	public void setDisableSupported(boolean disableSupported) {
		this.disableSupported = disableSupported;
	}

	@Override
	public void validate() {
		if (this.getAttributes() == null || this.getAttributes().length == 0) {
			throw new IcException("BasicVirtualConfiguration validation problem: Attributes cannot be null or empty!");
		}

		// Validation on reserved attribute names
		for (String name : this.reservedNames) {
			boolean reservedAttributeFound = Arrays.asList(this.getAttributes())
					.stream()
					.filter(attribute -> attribute.toLowerCase().equals(name.toLowerCase()))
					.findFirst()
					.isPresent();

			if (reservedAttributeFound) {
				throw new IcException(MessageFormat.format(
						"BasicVirtualConfiguration validation problem: Attributes contain [{0}] attribute. This attribute name is reserved!",
						name));
			}
		}
	}

}
