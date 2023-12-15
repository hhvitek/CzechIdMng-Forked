package eu.bcvsolutions.idm.document.service.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.document.service.api.DocumentService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Example product service tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultDocumentServiceIntegration22Test extends AbstractIntegrationTest {
	
	@Autowired private ApplicationContext context;
	//
	private DocumentService service;

	@Before
	public void init() {
		service = context.getAutowireCapableBeanFactory().createBean(DefaultDocumentService.class);
	}
	
	@Test
	public void testQuickFilter(){
		String productPrefix = getHelper().createName();
		String productOneName = String.format("%sOne", productPrefix);
		String productTwoName = String.format("%sTwo", productPrefix);
		//
		DocumentFilter filter = new DocumentFilter();
		filter.setText(productOneName);
		Page<DocumentDto> results = service.find(filter, null);
		assertEquals(0, results.getTotalElements());
		//
		createProduct(productOneName);
		createProduct(productTwoName);		
		results = service.find(filter, null);
		assertEquals(1, results.getTotalElements());
		//assertEquals(productOneName, results.getContent().get(0).getName());
		//
		filter.setText(productPrefix);
		results = service.find(filter, null);
		assertEquals(2, results.getTotalElements());
	}
	
	private DocumentDto createProduct(String name) {
		DocumentDto product = new DocumentDto();
		//product.setName(name);
		//product.setCode(UUID.randomUUID().toString());
		return service.save(product);
	}
}
