package eu.bcvsolutions.idm.document.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.document.dto.DocumentDto;

/**
 * Controller tests
 * 
 * @author Radek Tomiška
 *
 */
public class ExampleProductControllerRestTest extends AbstractReadWriteDtoControllerRestTest<DocumentDto> {

	@Autowired private ExampleProductController controller;
	
	@Override
	protected AbstractReadWriteDtoController<DocumentDto, ?> getController() {
		return controller;
	}

	@Override
	protected DocumentDto prepareDto() {
		DocumentDto dto = new DocumentDto();
		//dto.setCode(getHelper().createName());
		//dto.setName(getHelper().createName());
		return dto;
	}
	
	@Override
	protected boolean supportsBulkActions() {
		return false;
	}

}
