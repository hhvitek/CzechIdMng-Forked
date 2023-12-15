package eu.bcvsolutions.idm.document.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.document.service.api.DocumentService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Example service - integration tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultDocumentServiceIntegrationTest extends AbstractIntegrationTest {

	
	@Autowired private ConfigurationService configurationService;
	@Autowired private ApplicationContext context;
	//
	private DocumentService service;

	@Before
	public void init() {
		service = context.getAutowireCapableBeanFactory().createBean(DefaultDocumentService.class);
	}
	
	@Test
	public void testPrivateValue() {
		String value = "valueOne";
		//configurationService.setValue(ExampleConfiguration.PROPERTY_PRIVATE, value);
		//
		//Assert.assertEquals(value, service.getPrivateValue());
	}
	
}
