package eu.bcvsolutions.idm.document.repository.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.document.TestHelper;
import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.document.service.api.DocumentService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for {@link TextDocumentFilter}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Transactional
public class TextDocumentFilterIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private DocumentService productService;
	@Autowired
	private TextDocumentFilter textProductFilter;

//	@Test
//	public void testFilteringFound() {
//		String textValue = getHelper().createName() + System.currentTimeMillis();
//		DocumentDto productOne = getHelper().createProduct(getHelper().createName() + textValue.toLowerCase() + System.currentTimeMillis());
//		DocumentDto productTwo = getHelper().createProduct(getHelper().createName(), textValue + getHelper().createName(), null);
//		DocumentDto productThree = getHelper().createProduct();
//		//productThree.setDescription(getHelper().createName() + textValue.toUpperCase() + getHelper().createName());
//		productService.save(productThree);
//
//		DocumentFilter filter = new DocumentFilter();
//		filter.setText(textValue);
//		List<ExampleProduct> products = null;//= textProductFilter.find(filter, null).getContent();
//
//		assertEquals(3, products.size());
//
//		// list must contains all products
//		ExampleProduct product = products.stream().filter(prod -> prod.getId().equals(productOne.getId())).findFirst().get();
//		assertNotNull(product);
//
//		product = products.stream().filter(prod -> prod.getId().equals(productTwo.getId())).findFirst().get();
//		assertNotNull(product);
//
//		product = products.stream().filter(prod -> prod.getId().equals(productThree.getId())).findFirst().get();
//		assertNotNull(product);
//	}
//
//	@Test
//	public void testFilteringNotFound() {
//		String textValue = "textValue" + System.currentTimeMillis();
//		getHelper().createProduct("123" + textValue + System.currentTimeMillis());
//		DocumentDto productTwo = getHelper().createProduct();
//		DocumentDto productThree = getHelper().createProduct();
//		//productTwo.setCode(textValue + getHelper().createName());
//		//productThree.setDescription(getHelper().createName() + textValue + getHelper().createName());
//
//		productService.save(productTwo);
//		productService.save(productThree);
//
//		DocumentFilter filter = new DocumentFilter();
//		filter.setText("textValue" + getHelper().createName()); // different value than in variable textValue
//		List<ExampleProduct> products = null;//= textProductFilter.find(filter, null).getContent();
//
//		assertEquals(0, products.size());
//	}
//
	@Override
	protected TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
}
