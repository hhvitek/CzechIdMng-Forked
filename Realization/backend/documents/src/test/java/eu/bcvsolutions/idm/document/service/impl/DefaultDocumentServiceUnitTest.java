package eu.bcvsolutions.idm.document.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.document.config.domain.DocumentConfiguration;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Example service - unit tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultDocumentServiceUnitTest extends AbstractUnitTest {
	
	@Mock 
	private DocumentConfiguration documentConfiguration;
	@Mock 
	private NotificationManager notificationManager;
	@InjectMocks 
	private DefaultDocumentService service;
	
	@Test
	public void testPingWithMessage() {
//		String message = "test";
//		Pong pong = service.ping(message);
//		Assert.assertNotNull(pong);
//		Assert.assertEquals(message, pong.getMessage());
	}
}
