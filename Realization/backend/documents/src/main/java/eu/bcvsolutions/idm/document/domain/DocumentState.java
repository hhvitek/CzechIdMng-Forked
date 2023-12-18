package eu.bcvsolutions.idm.document.domain;

public enum DocumentState {
	VALID,
	INVALID;

	public boolean isValid() {
		return this == VALID;
	}
}
