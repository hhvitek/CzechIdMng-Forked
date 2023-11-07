package eu.bcvsolutions.idm.tool.service.impl;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.bcvsolutions.idm.test.api.AbstractUnitTest;
import eu.bcvsolutions.idm.tool.service.api.ReleaseManager.VersionType;

/**
 * Test release on mock repository.
 * 
 * @author Radek Tomiška
 *
 */
@Ignore
public abstract class AbstractReleaseManagerUnitTest extends AbstractUnitTest {
	
	/**
	 * IdM version - product version used for generation maven poms.
	 */
	protected static final String PRODUCT_VERSION = "10.5.0"; 
	
	protected abstract AbstractReleaseManager getReleaseManager();
	
	/**
	 * Prepare mock repository in target folder for test purposes.
	 * 
	 * @return
	 */
	protected abstract String prepareRepository();
	
	@BeforeClass
	public static void disableTestsOnDocumentation() {
		// generalize unit test, but it's integration test (MAVEN_HOME) is needed 
	    Boolean documentationOnly = Boolean.valueOf(System.getProperty("documentationOnly", "false"));
	    Assume.assumeFalse(documentationOnly);
	}
	
	@Before
	public void initRepository() {
		Assert.assertEquals("1.0.0-SNAPSHOT", prepareRepository());
	}
	
	@Test
	@Ignore
	public void testSetVersion() {
		Assert.assertEquals("1.0.0-SNAPSHOT", getReleaseManager().getCurrentVersion());
		//
		String newVesion = "2.0.0-SNAPSHOT";
		Assert.assertEquals(newVesion, getReleaseManager().setVersion(newVesion));
		Assert.assertEquals(newVesion, getReleaseManager().getCurrentVersion());
		//
		Assert.assertEquals("1.0.0-SNAPSHOT", getReleaseManager().revertVersion());
		Assert.assertEquals("1.0.0-SNAPSHOT", getReleaseManager().getCurrentVersion());
	}
	
	@Test
	@Ignore
	public void testIsSnapshotVersion() {
		Assert.assertTrue(getReleaseManager().isSnapshotVersion("1.0.0-SNAPSHOT"));
		Assert.assertFalse(getReleaseManager().isSnapshotVersion("1.0.0"));
		//
		String newVesion = "2.0.0-SNAPSHOT";
		Assert.assertEquals(newVesion, getReleaseManager().setVersion(newVesion));
		Assert.assertEquals(newVesion, getReleaseManager().getCurrentVersion());
		//
		Assert.assertEquals("1.0.0-SNAPSHOT", getReleaseManager().revertVersion());
		Assert.assertEquals("1.0.0-SNAPSHOT", getReleaseManager().getCurrentVersion());
	}
	
	@Test
	@Ignore
	public void testSetSameVersion() {
		Assert.assertEquals("1.0.0-SNAPSHOT", getReleaseManager().getCurrentVersion());
		//
		Assert.assertEquals("1.0.0-SNAPSHOT", getReleaseManager().setVersion("1.0.0-SNAPSHOT"));
	}
	
	@Test
	@Ignore
	public void testSetSnapshotVersion() {
		Assert.assertEquals("1.0.0-SNAPSHOT", getReleaseManager().getCurrentVersion());
		//
		Assert.assertEquals("1.0.1-SNAPSHOT", getReleaseManager().setSnapshotVersion("1.0.1"));
	}
	
	@Test
	@Ignore
	public void testNextSnapshotVersionNumber() {
		Assert.assertEquals("1.0.1-SNAPSHOT", getReleaseManager().getNextSnapshotVersionNumber("1.0.0-SNAPSHOT", null));
		Assert.assertEquals("1.1.24-SNAPSHOT", getReleaseManager().getNextSnapshotVersionNumber("1.1.23-SNAPSHOT", null));
		Assert.assertEquals("2.0.0-SNAPSHOT", getReleaseManager().getNextSnapshotVersionNumber("1.1.23-SNAPSHOT", VersionType.MAJOR));
		Assert.assertEquals("1.2.0-SNAPSHOT", getReleaseManager().getNextSnapshotVersionNumber("1.1.23-SNAPSHOT", VersionType.MINOR));
		Assert.assertEquals("1.1.24-SNAPSHOT", getReleaseManager().getNextSnapshotVersionNumber("1.1.23-SNAPSHOT", VersionType.PATCH));
		Assert.assertEquals("1.1.23.1-SNAPSHOT", getReleaseManager().getNextSnapshotVersionNumber("1.1.23-SNAPSHOT", VersionType.HOTFIX));
		Assert.assertEquals("1.0.1-SNAPSHOT", getReleaseManager().getNextSnapshotVersionNumber(null, null));
	}
	
	@Test
	@Ignore
	public void testNotSemanticNextSnapshotVersionNumber() {
		Assert.assertEquals("mock.1-SNAPSHOT", getReleaseManager().getNextSnapshotVersionNumber("mock", null));
	}
	
	@Test
	@Ignore
	public void testCreateSnapshotTag() {
		Assert.assertEquals("1.0.1-SNAPSHOT", getReleaseManager().gitCreateTag("1.0.1-SNAPSHOT"));
	}
	
	@Test
	@Ignore
	public void testBuild() {
		Assert.assertEquals("1.0.0-SNAPSHOT", getReleaseManager().getCurrentVersion());
		//
		Assert.assertEquals("1.0.0-SNAPSHOT", getReleaseManager().build());
	}
	
	@Test
	@Ignore
	public void testRelease() {
		Assert.assertEquals("1.0.0-SNAPSHOT", getReleaseManager().getCurrentVersion());
		getReleaseManager().gitAddAll();
		getReleaseManager().gitCommit("before test release");
		//
		Assert.assertEquals("1.0.0", getReleaseManager().release(null, null));
		getReleaseManager().gitSwitchBranch("develop");
		Assert.assertEquals("1.0.1-SNAPSHOT", getReleaseManager().getCurrentVersion());
		getReleaseManager().gitSwitchBranch("master");
		Assert.assertEquals("1.0.0", getReleaseManager().getCurrentVersion());
		// just finish UC - local repository without origin cannot be pushed ...
		getReleaseManager().publish();
	}
	
	@Test
	@Ignore
	public void testReleaseDifferentBranches() {
		Assert.assertEquals("1.0.0-SNAPSHOT", getReleaseManager().getCurrentVersion());
		try {
			String develop = "hotfix";
			String master = "lts-master";
			
			getReleaseManager().gitCreateBranch("hotfix");
			getReleaseManager().gitSwitchBranch("hotfix");
			getReleaseManager().setDevelopBranch(develop);
			getReleaseManager().setVersion("2.3.6-SNAPSHOT");
			getReleaseManager().gitAddAll();
			getReleaseManager().gitCommit("start hotfix");
			//
			getReleaseManager().gitCreateBranch(master);
			getReleaseManager().setMasterBranch(master);
			//
			Assert.assertEquals("3.5.6", getReleaseManager().release("3.5.6", "4.0.0-SNAPSHOT"));
			getReleaseManager().gitSwitchBranch(develop);
			Assert.assertEquals("4.0.0-SNAPSHOT", getReleaseManager().getCurrentVersion());
			getReleaseManager().gitSwitchBranch(master);
			Assert.assertEquals("3.5.6", getReleaseManager().getCurrentVersion());
		} finally {
			getReleaseManager().setDevelopBranch("develop");
			getReleaseManager().setMasterBranch("master");
		}
	}
	
	@Test
	@Ignore
	public void testReleaseNotMergeMaster() {
		Assert.assertEquals("1.0.0-SNAPSHOT", getReleaseManager().getCurrentVersion());
		//
		getReleaseManager().setVersion("1.0.1");
		getReleaseManager().gitAddAll();
		getReleaseManager().gitCommit("develop branch");
		getReleaseManager().gitSwitchBranch("master");
		getReleaseManager().gitMerge("develop");
		getReleaseManager().gitAddAll();
		getReleaseManager().gitCommit("merge develop branch");
		Assert.assertEquals("1.0.1", getReleaseManager().getCurrentVersion());
		//
		getReleaseManager().setVersion("2.0.1-SNAPSHOT");
		getReleaseManager().gitAddAll();
		getReleaseManager().gitCommit("new develop version");
		try {
			getReleaseManager().setMasterBranch(null);
			Assert.assertEquals("2.0.1", getReleaseManager().release(null, null));
			//
			Assert.assertEquals("1.0.1", getReleaseManager().getCurrentVersion("master"));
			Assert.assertEquals("2.0.2-SNAPSHOT", getReleaseManager().getCurrentVersion("develop"));
		} finally {
			getReleaseManager().setMasterBranch("master");
		}

	}	
}
