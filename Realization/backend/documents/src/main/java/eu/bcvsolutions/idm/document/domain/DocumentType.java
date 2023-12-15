package eu.bcvsolutions.idm.document.domain;

public enum DocumentType {
	PASSPORT(1),
	ID_CARD(2);


	private int typeId;
	DocumentType(int typeId) {
		this.typeId = typeId;
	}
}
