package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Base entity method unit tests
 * 
 * @author Radek Tomiška
 *
 */
public class AbstractDtoUnitTest extends AbstractUnitTest {

	/**
	 * Test equals and hash code methods
	 * @since 13.0.0 the behavior of toString method was changed - it returns just the code for CodeableDto
	 */
	@Test
	public void testEqualsAndHashCode() {
		AbstractDto dtoOne = new IdmIdentityDto();
		AbstractDto dtoTwo = new IdmIdentityDto();
		//
		Assert.assertFalse(dtoOne.equals(dtoTwo));
		Assert.assertFalse(dtoOne.equals(null));
		Assert.assertEquals(dtoOne.hashCode(), dtoTwo.hashCode());
		//
		Object mockResource = Mockito.mock(IdmIdentity.class);
		//
		Assert.assertTrue(mockResource.equals(mockResource));
		Assert.assertFalse(mockResource.equals(null));
		//
		dtoOne = new IdmIdentityDto(UUID.randomUUID());
		dtoTwo = new IdmIdentityDto(UUID.randomUUID());
		//
		Assert.assertFalse(dtoOne.equals(dtoTwo));
		Assert.assertNotEquals(dtoOne.hashCode(), dtoTwo.hashCode());
		//
		dtoOne = new IdmIdentityDto(UUID.randomUUID());
		dtoTwo = new IdmIdentityDto(dtoOne.getId());
		//
		Assert.assertTrue(dtoOne.equals(dtoTwo));
		Assert.assertEquals(dtoOne.hashCode(), dtoTwo.hashCode());
		//
		dtoOne = new IdmIdentityDto(UUID.randomUUID());
		dtoTwo = new IdmIdentityDto();
		//
		Assert.assertFalse(dtoOne.equals(dtoTwo));
		Assert.assertNotEquals(dtoOne.hashCode(), dtoTwo.hashCode());
		//
		dtoOne = new IdmIdentityDto();
		dtoTwo = new IdmIdentityDto(UUID.randomUUID());
		//
		Assert.assertFalse(dtoOne.equals(dtoTwo));
		Assert.assertNotEquals(dtoOne.hashCode(), dtoTwo.hashCode());
	}
}
