package eu.bcvsolutions.idm.rpt;

import org.junit.Test;

import eu.bcvsolutions.idm.test.api.AbstractSwaggerTest;


/**
 * Static swagger generation to sources - will be used as input for openapi-generator build
 * 
 * @author Radek Tomiška
 *
 */
public class Swagger2MarkupTest extends AbstractSwaggerTest {
	
	@Test
	public void testConvertSwagger() throws Exception {
		super.convertSwagger(RptModuleDescriptor.MODULE_ID);
	}
    
}