package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.audit.service.SiemLoggerManager;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.event.DefaultEventContext;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.event.processor.NeverEndingProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmConfigurationRepository;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Configuration service unit tests:
 * - multiple properties
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultConfigurationServiceUnitTest extends AbstractUnitTest {

	private static final String VALUE_ONE = "valueOne";
	private static final String VALUE_TWO = "value Two";
	private static final String VALUE_THREE = "valueThree";
	//
	ModelMapper modelMapper = spy(ModelMapper.class);
	private final IdmConfigurationRepository repository = mock(IdmConfigurationRepository.class);
	private final ConfidentialStorage confidentialStorage = mock(ConfidentialStorage.class);
	private final EntityEventManager entityEventManager = mock(EntityEventManager.class);
	private final IdmCacheManager cacheManager = mock(IdmCacheManager.class);
	private final SiemLoggerManager logger = mock(SiemLoggerManager.class);
	private final ConfigurableEnvironment env = mock(ConfigurableEnvironment.class);
	//
	private final DefaultConfigurationService service = spy(new DefaultConfigurationService(repository, confidentialStorage, env, entityEventManager));

	@Before
	public void init() {
		when(logger.buildAction(any(String.class), any(String.class))).thenReturn("");
		when(logger.skipLogging(any(String.class))).thenReturn(false);
		ReflectionTestUtils.setField(service, "modelMapper", modelMapper);
		ReflectionTestUtils.setField(service, "siemLoggerManager", logger);
		ReflectionTestUtils.setField(service, "cacheManager", cacheManager);
	}

	@Test
	public void testGetValuesWithNull() {
		when(repository.findOneByName(any(String.class))).thenReturn(null);
		when(cacheManager.getValue(any(String.class), any(String.class))).thenReturn(null);
		List<String> results = service.getValues("key");
		//
		Assert.assertNotNull(results);
		Assert.assertEquals(0, results.size());
	}
	
	@Test
	public void testGetValuesWithEmpty() {
		IdmConfiguration configurationItem = new IdmConfiguration(null, "  , ,  ,,, ,");
		when(repository.findOneByName(any(String.class))).thenReturn(configurationItem);
		//
		List<String> results = service.getValues("key");
		//
		Assert.assertNotNull(results);
		Assert.assertEquals(0, results.size());
	}
	
	@Test
	public void testGetValues() {
		IdmConfiguration configurationItem = new IdmConfiguration(null, String.format("%s,%s,%s", VALUE_ONE, VALUE_TWO, VALUE_THREE));
		when(repository.findOneByName(any(String.class))).thenReturn(configurationItem);
		//
		List<String> results = service.getValues("key");
		//
		Assert.assertNotNull(results);
		Assert.assertEquals(3, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.equals(VALUE_ONE)));
		Assert.assertTrue(results.stream().anyMatch(e -> e.equals(VALUE_TWO)));
		Assert.assertTrue(results.stream().anyMatch(e -> e.equals(VALUE_THREE)));
	}
	
	@Test
	public void testGetValuesWithBlankSpaces() {
		IdmConfiguration configurationItem = new IdmConfiguration(null, String.format(" %s   ,%s,  %s,,,     ,", VALUE_ONE, VALUE_TWO, VALUE_THREE));
		when(repository.findOneByName(any(String.class))).thenReturn(configurationItem);
		//
		List<String> results = service.getValues("key");
		//
		Assert.assertNotNull(results);
		Assert.assertEquals(3, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.equals(VALUE_ONE)));
		Assert.assertTrue(results.stream().anyMatch(e -> e.equals(VALUE_TWO)));
		Assert.assertTrue(results.stream().anyMatch(e -> e.equals(VALUE_THREE)));
	}
	
	@Test
	public void testSaveValues() {
		EventAnswer answer = new EventAnswer();
		when(entityEventManager.process(any(), any())).thenAnswer(answer);
		//
		service.setValues("key", Lists.newArrayList(VALUE_ONE, VALUE_TWO, VALUE_THREE));
		//
		Assert.assertEquals(String.format("%s,%s,%s", VALUE_ONE, VALUE_TWO, VALUE_THREE), answer.getValue());
	}
	
	@Test
	public void testSaveValuesWithNull() {
		EventAnswer answer = new EventAnswer();
		when(entityEventManager.process(any(), any())).thenAnswer(answer);
		//
		service.setValues("key", null);
		//
		Assert.assertEquals(null, answer.getValue());
	}
	
	@Test
	public void testSaveValuesWithEmptyList() {
		EventAnswer answer = new EventAnswer();
		when(entityEventManager.process(any(), any())).thenAnswer(answer);
		//
		service.setValues("key", Lists.newArrayList());
		//
		Assert.assertEquals(null, answer.getValue());
	}
	
	@Test
	public void testSaveValuesWithNullValue() {
		EventAnswer answer = new EventAnswer();
		when(entityEventManager.process(any(), any())).thenAnswer(answer);
		//
		String nullValue = null;
		service.setValues("key", Lists.newArrayList(nullValue));
		//
		Assert.assertEquals(null, answer.getValue());
	}
	
	@Test
	public void testSaveValuesWithNullValues() {
		EventAnswer answer = new EventAnswer();
		when(entityEventManager.process(any(), any())).thenAnswer(answer);
		//
		service.setValues("key", Lists.newArrayList(null, VALUE_ONE, null, null, VALUE_TWO, VALUE_THREE));
		//
		Assert.assertEquals(String.format("%s,%s,%s", VALUE_ONE, VALUE_TWO, VALUE_THREE), answer.getValue());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private class EventAnswer implements Answer<EventContext> {
		
		private IdmConfigurationDto configurationItem;
		
		@Override
		public EventContext answer(InvocationOnMock invocation) throws Throwable {
			EntityEvent event = (EntityEvent) invocation.getArguments()[0];
			configurationItem = (IdmConfigurationDto) event.getContent();
			DefaultEventContext eventContext = new DefaultEventContext();
			eventContext.addResult(new DefaultEventResult(event, new NeverEndingProcessor()));
			return eventContext;
		}
		
		public String getValue() {
			return configurationItem == null ? null : configurationItem.getValue();
		}
	}
}
