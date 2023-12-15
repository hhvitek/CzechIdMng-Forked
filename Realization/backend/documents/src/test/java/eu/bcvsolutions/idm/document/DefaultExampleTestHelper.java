package eu.bcvsolutions.idm.document;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.service.api.DocumentService;

/**
 * Example test helper - custom test helper can be defined in modules.
 * 
 * @author Radek Tomiška
 */
@Primary
@Component("exampleTestHelper")
public class DefaultExampleTestHelper extends eu.bcvsolutions.idm.test.api.DefaultTestHelper implements TestHelper {

	@Autowired private DocumentService productService;
	
	@Override
	public DocumentDto createProduct() {
		return createProduct(new BigDecimal(ThreadLocalRandom.current().nextDouble(0, 1000000)));
	}
	
	@Override
	public DocumentDto createProduct(String name) {
		return createProduct(createName(), name, new BigDecimal(ThreadLocalRandom.current().nextDouble(0, 1000000)));
	}

	@Override
	public DocumentDto createProduct(BigDecimal price) {
		return createProduct(createName(), createName(), price);
	}
	
	@Override
	public DocumentDto createProduct(String code, String name, BigDecimal price) {
		DocumentDto product = new DocumentDto();
		//product.setCode(code == null ? createName() : code);
		//product.setName(name == null ? createName() : name);
		//product.setPrice(price == null ? null : price.round(new MathContext(4, RoundingMode.HALF_UP)));
		//
		return productService.save(product);
	}
	
	
}
