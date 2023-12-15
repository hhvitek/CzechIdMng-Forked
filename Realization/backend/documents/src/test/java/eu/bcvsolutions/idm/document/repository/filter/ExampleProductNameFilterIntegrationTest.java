package eu.bcvsolutions.idm.document.repository.filter;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.document.TestHelper;
import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for {@link TextDocumentFilter}
 * 
 * @author Radek Tomiška
 */
@Transactional
public class ExampleProductNameFilterIntegrationTest extends AbstractIntegrationTest {

//	@Autowired private DocumentFilter filter;
//
//	@Test
//	public void testFilter() {
//		DocumentDto productOne = getHelper().createProduct();
//		getHelper().createProduct(); // other product
//		//
//		DocumentFilter productFilter = new DocumentFilter();
//		//productFilter.setName(productOne.getName());
//		List<ExampleProduct> products = filter.find(productFilter, null).getContent();
//		//
//		Assert.assertEquals(1, products.size());
//		Assert.assertEquals(productOne.getId(), products.get(0).getId());
//	}
	
	@Override
	protected TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
}
