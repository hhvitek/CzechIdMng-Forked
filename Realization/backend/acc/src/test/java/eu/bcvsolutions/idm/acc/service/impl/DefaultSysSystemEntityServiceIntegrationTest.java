package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * System entity operation tests.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultSysSystemEntityServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private SysSystemService systemService;
	@Autowired private AccAccountService accountService;
	@Autowired private SysSystemEntityService systemEntityService;
	
	@Test
	public void testReferentialIntegrity() {
		// system
		SysSystemDto system = new SysSystemDto();
		String systemName = "t_s_" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);
		// system entity
		SysSystemEntityDto systemEntity = new SysSystemEntityDto();
		systemEntity.setSystem(system.getId());
		systemEntity.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		String uid = "se_uid_" + System.currentTimeMillis();
		systemEntity.setUid(uid);
		systemEntity = systemEntityService.save(systemEntity);
		// account
		AccAccountDto account = new AccAccountDto();
		account.setSystem(system.getId());
		account.setUid("test_uid_" + System.currentTimeMillis());
		account.setSystemEntity(systemEntity.getId());
		account.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		account = accountService.save(account);
		
		SysSystemEntityDto systemEntityDto = DtoUtils.getEmbedded(account, AccAccount_.systemEntity);
		assertEquals(uid, systemEntityDto.getUid());
		
		systemEntityService.delete(systemEntity);
		
		assertNull(accountService.get(account.getId()).getSystemEntity());		
	}
}
