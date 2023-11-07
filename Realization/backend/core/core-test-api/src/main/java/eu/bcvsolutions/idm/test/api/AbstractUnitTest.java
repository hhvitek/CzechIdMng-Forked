package eu.bcvsolutions.idm.test.api;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public abstract class AbstractUnitTest {

	/**
	 * Verifies that all mocks had only interactions specified by test.
	 * 
	 * @throws Exception if something is broken
	 */
	public void verifyMocks() throws Exception {
		List<Object> mocks = new ArrayList<>();
		for(Field field : getClass().getDeclaredFields()) {
			field.setAccessible(true);
			
			if(field.isAnnotationPresent(Mock.class)) {
				mocks.add(field.get(this));
			}
		}
		
		if(!mocks.isEmpty()) {
			Mockito.verifyNoMoreInteractions(mocks.toArray());
		}
	}

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
}
