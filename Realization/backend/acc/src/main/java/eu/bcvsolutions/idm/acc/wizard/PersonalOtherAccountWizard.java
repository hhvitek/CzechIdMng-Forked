package eu.bcvsolutions.idm.acc.wizard;

import org.springframework.stereotype.Component;

/**
 * Wizard for personal other accounts
 * @author Roman Kucera
 */
@Component(PersonalOtherAccountWizard.NAME)
public class PersonalOtherAccountWizard extends AbstractAccountWizard {

	public static final String NAME = "personal-other-account-wizard";

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public boolean supports() {
		return true;
	}
}
