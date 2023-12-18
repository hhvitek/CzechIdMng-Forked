package eu.bcvsolutions.idm.document.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.document.TestHelper;
import eu.bcvsolutions.idm.document.dto.DocumentDto;

/**
 * Controller tests
 *
 */
@Transactional
public class DocumentControllerRestCrudTest extends AbstractReadWriteDtoControllerRestTest<DocumentDto> {

	@Autowired
	private DocumentController controller;
	@Autowired
	private TestHelper helper;
	
	@Override
	protected AbstractReadWriteDtoController<DocumentDto, ?> getController() {
		return controller;
	}

	@Override
	protected DocumentDto prepareDto() {
		IdmIdentityDto identity = helper.createIdentityOnly();
		DocumentDto document = helper.getDocument(identity);
		return document;
	}
	
	@Override
	protected boolean supportsBulkActions() {
		return false;
	}

}
