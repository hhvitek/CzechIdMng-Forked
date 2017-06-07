package eu.bcvsolutions.idm.acc.scheduler.task;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.AccountProtectionExpirationTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * LRT test
 * 
 * @author Radek Tomiška
 *
 */
public class AccountProtectionExpirationTaskExecutorUnitTest extends AbstractUnitTest {
	
	@Spy
	@InjectMocks
	private AccountProtectionExpirationTaskExecutor executor;
	@Mock
	private AccAccountService service;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testTwoPageIterations() {
		List<AccAccount> accounts = new ArrayList<>();
		accounts.add(new AccAccount());
		accounts.add(new AccAccount());
		//
		when(service.findExpired(any(DateTime.class), any(PageRequest.class)))
			.thenReturn(new PageImpl<AccAccount>(accounts)) // first call
			.thenReturn(new PageImpl<AccAccount>(new ArrayList<>())); // second call - empty page
		//
		doNothing().when(service).delete(any(AccAccount.class));
		//
		when(executor.updateState()).thenReturn(true);
		//
		Boolean result = executor.process();
		Assert.assertTrue(result);
	}
}
