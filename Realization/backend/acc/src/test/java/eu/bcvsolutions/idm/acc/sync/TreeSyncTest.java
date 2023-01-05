package eu.bcvsolutions.idm.acc.sync;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.core.api.config.datasource.CoreEntityManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncTreeConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.TestTreeResource;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.TreeSynchronizationExecutor;
import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Advanced tree synchronization tests
 * 
 * @author Svanda
 *
 */
public class TreeSyncTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService schemaAttributeMappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private SysSyncConfigService syncConfigService;
	@Autowired
	private SysSyncLogService syncLogService;
	@Autowired
	@CoreEntityManager
	private EntityManager entityManager;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private IdmTreeTypeService treeTypeService;
	@Autowired
	private IdmTreeNodeService treeNodeService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;

	private SysSystemDto system;

	@After
	public void logout() {
		this.getBean().deleteAllResourceData();
	}

	@Test
	public void syncNodesUnderExistsRoot() {
		String treeTypeCode = getHelper().createName();
		AbstractSysSyncConfigDto syncConfigCustom = this.getBean().doCreateSyncConfig(treeTypeCode, false);
		Assert.assertFalse(syncConfigService.isRunning(syncConfigCustom));
		
		IdmTreeTypeDto treeType = treeTypeService.find(null).getContent().stream().filter(tree -> {
			return tree.getName().equals(treeTypeCode);
		}).findFirst().get();

		// We want to sync all account under that node!
		IdmTreeNodeDto treeNodeExistedNode = helper.createTreeNode(treeType, null);

		syncConfigCustom.setRootsFilterScript("if(account){ def parentValue = account.getAttributeByName(\"" + helper.getSchemaColumnName("PARENT") + "\").getValue();"
				+ " if(parentValue == null || parentValue.isEmpty()){"
				+ "	 account.getAttributeByName(\"" + helper.getSchemaColumnName("PARENT") + "\").setValues([\""+treeNodeExistedNode.getId()+"\"]); return Boolean.TRUE;}}"
				+ " \nreturn Boolean.FALSE;");
		syncConfigService.save(syncConfigCustom);

		//
		helper.startSynchronization(syncConfigCustom);
		//
		SysSyncLogFilter logFilter = new SysSyncLogFilter();
		logFilter.setSynchronizationConfigId(syncConfigCustom.getId());
		List<SysSyncLogDto> logs = syncLogService.find(logFilter, null).getContent();
		Assert.assertEquals(1, logs.size());
		SysSyncLogDto log = logs.get(0);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		helper.checkSyncLog(syncConfigCustom, SynchronizationActionType.CREATE_ENTITY, 12, OperationResultType.SUCCESS);

		Assert.assertEquals(1, treeNodeService.findRoots(treeType.getId(), null).getContent().size());
		IdmTreeNodeDto root = treeNodeService.findRoots(treeType.getId(), null).getContent().get(0);
		Assert.assertEquals(root, treeNodeExistedNode);
		List<IdmTreeNodeDto> children = treeNodeService.findChildrenByParent(root.getId(), null).getContent();
		Assert.assertEquals(2, children.size());
		IdmTreeNodeDto child = children.get(0);
		IdmTreeNodeDto child2 = children.get(1);
		Assert.assertEquals(child.getCode(), "1");
		Assert.assertEquals(child2.getCode(), "2");
		// Delete log
		syncLogService.delete(log);
	}
	
	@Test
	public void testDifferentialSync() {
		String treeTypeCode = helper.createName();
		AbstractSysSyncConfigDto syncConfigCustom = this.getBean().doCreateSyncConfig(treeTypeCode, false);
		Assert.assertFalse(syncConfigService.isRunning(syncConfigCustom));

		IdmTreeTypeDto treeType = treeTypeService.find(null).getContent().stream().filter(tree -> {
			return tree.getName().equals(treeTypeCode);
		}).findFirst().get();

		// We want to sync all account under that node!
		IdmTreeNodeDto treeNodeExistedNode = helper.createTreeNode(treeType, null);

		syncConfigCustom.setRootsFilterScript("if(account){ def parentValue = account.getAttributeByName(\"" + helper.getSchemaColumnName("PARENT") + "\").getValue();"
				+ " if(parentValue == null || parentValue.isEmpty()){"
				+ "	 account.getAttributeByName(\"" + helper.getSchemaColumnName("PARENT") + "\").setValues([\""+treeNodeExistedNode.getId()+"\"]); return Boolean.TRUE;}}"
				+ " \nreturn Boolean.FALSE;");
		syncConfigService.save(syncConfigCustom);

		//
		helper.startSynchronization(syncConfigCustom);
		//
		SysSyncLogFilter logFilter = new SysSyncLogFilter();
		logFilter.setSynchronizationConfigId(syncConfigCustom.getId());
		List<SysSyncLogDto> logs = syncLogService.find(logFilter, null).getContent();
		Assert.assertEquals(1, logs.size());
		SysSyncLogDto log = logs.get(0);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		
		helper.checkSyncLog(syncConfigCustom, SynchronizationActionType.CREATE_ENTITY, 12, OperationResultType.SUCCESS);
		
		// Enable different sync.
		syncConfigCustom.setDifferentialSync(true);
		syncConfigCustom = syncConfigService.save(syncConfigCustom);
		Assert.assertTrue(syncConfigCustom.isDifferentialSync());

		// Start sync with enable different sync - no change was made, so only ignore
		// update should be made.
		helper.startSynchronization(syncConfigCustom);
		log = helper.checkSyncLog(syncConfigCustom, SynchronizationActionType.UPDATE_ENTITY, 12, OperationResultType.IGNORE);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// Change code of node
		IdmTreeNodeDto root = treeNodeService.findRoots(treeType.getId(), null).getContent().get(0);
		List<IdmTreeNodeDto> children = treeNodeService.findChildrenByParent(root.getId(), null).getContent();
		Assert.assertEquals(2, children.size());
		IdmTreeNodeDto child = children.get(0);
		IdmTreeNodeDto child2 = children.get(1);
		Assert.assertEquals(child.getCode(), "1");
		Assert.assertEquals(child2.getCode(), "2");
		child.setCode(helper.createName());
		treeNodeService.save(child);

		// Start sync with enable different sync - Node code value changed, so standard update should be made.
		helper.startSynchronization(syncConfigCustom);
		log = helper.checkSyncLog(syncConfigCustom, SynchronizationActionType.UPDATE_ENTITY, 1, OperationResultType.SUCCESS);
		log = helper.checkSyncLog(syncConfigCustom, SynchronizationActionType.UPDATE_ENTITY, 11, OperationResultType.IGNORE);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// Delete log
		syncLogService.delete(log);
	}
	
	@Test
	public void testTreeWithAutomaticRoles() {
		IdmTreeTypeDto treeType = getHelper().createTreeType();
		//
		// create synchronization 
		AbstractSysSyncConfigDto syncConfigCustom = this.getBean().doCreateSyncConfig(treeType.getCode(), true);
		Assert.assertFalse(syncConfigService.isRunning(syncConfigCustom));
		// We want to sync all account under that node!
		IdmTreeNodeDto treeNodeExistedNode = helper.createTreeNode(treeType, null);

		syncConfigCustom.setRootsFilterScript("if(account){ def parentValue = account.getAttributeByName(\"" + helper.getSchemaColumnName("PARENT") + "\").getValue();"
				+ " if(parentValue == null || parentValue.isEmpty()){"
				+ "	 account.getAttributeByName(\"" + helper.getSchemaColumnName("PARENT") + "\").setValues([\""+treeNodeExistedNode.getId()+"\"]); return Boolean.TRUE;}}"
				+ " \nreturn Boolean.FALSE;");
		syncConfigService.save(syncConfigCustom);
		//
		helper.startSynchronization(syncConfigCustom);
		//
		SysSyncLogFilter logFilter = new SysSyncLogFilter();
		logFilter.setSynchronizationConfigId(syncConfigCustom.getId());
		List<SysSyncLogDto> logs = syncLogService.find(logFilter, null).getContent();
		Assert.assertEquals(1, logs.size());
		SysSyncLogDto log = logs.get(0);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		//
		helper.checkSyncLog(syncConfigCustom, SynchronizationActionType.CREATE_ENTITY, 12, OperationResultType.SUCCESS);
		//
		// prepare contracts 
		IdmTreeNodeFilter nodeFilter = new IdmTreeNodeFilter();
		nodeFilter.setTreeTypeId(treeType.getId());
		nodeFilter.setCode("112");
		List<IdmTreeNodeDto> results =  treeNodeService.find(nodeFilter, null).getContent();
		IdmTreeNodeDto parentNode = results.get(0);
		nodeFilter.setCode("1111");
		IdmTreeNodeDto node = treeNodeService.find(nodeFilter, null).getContent().get(0); // parent will be set by synchronization
		// define automatic role for parent
		IdmRoleDto role = getHelper().createRole();
		IdmRoleTreeNodeDto automaticRole = getHelper().createRoleTreeNode(role, parentNode, RecursionType.DOWN, true);
		// create identity with contract on node
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		getHelper().createContract(identity, node);
		// no role should be assigned now
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		// change tree structure and synchronize
		this.getBean().changeParent("1111", "112");
		helper.startSynchronization(syncConfigCustom);
		log = helper.checkSyncLog(syncConfigCustom, SynchronizationActionType.UPDATE_ENTITY, 12, OperationResultType.SUCCESS);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
	}

	@Transactional
	public AbstractSysSyncConfigDto doCreateSyncConfig(String treeTypeCode, boolean startAutoRoleRec) {
		initData(treeTypeCode);

		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setEntityType(TreeSynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		mappingFilter.setSystemId(system.getId());
		mappingFilter.setOperationType(SystemOperationType.SYNCHRONIZATION);
		List<SysSystemMappingDto> mappings = systemMappingService.find(mappingFilter, null).getContent();
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto mapping = mappings.get(0);
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mapping.getId());

		List<SysSystemAttributeMappingDto> attributes = schemaAttributeMappingService.find(attributeMappingFilter, null)
				.getContent();
		SysSystemAttributeMappingDto uidAttribute = attributes.stream().filter(attribute -> {
			return attribute.isUid();
		}).findFirst().get();

		// Create default synchronization config
		SysSyncTreeConfigDto syncConfigCustom = new SysSyncTreeConfigDto();
		syncConfigCustom.setReconciliation(true);
		syncConfigCustom.setCustomFilter(true);
		syncConfigCustom.setSystemMapping(mapping.getId());
		syncConfigCustom.setCorrelationAttribute(uidAttribute.getId());
		syncConfigCustom.setReconciliation(true);
		syncConfigCustom.setName(helper.createName());
		syncConfigCustom.setLinkedAction(SynchronizationLinkedActionType.UPDATE_ENTITY);
		syncConfigCustom.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		syncConfigCustom.setMissingEntityAction(SynchronizationMissingEntityActionType.CREATE_ENTITY);
		syncConfigCustom.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);
		syncConfigCustom.setStartAutoRoleRec(startAutoRoleRec);
		
		syncConfigCustom = (SysSyncTreeConfigDto) syncConfigService.save(syncConfigCustom);

		SysSyncConfigFilter configFilter = new SysSyncConfigFilter();
		configFilter.setSystemId(system.getId());
		Assert.assertEquals(1, syncConfigService.find(configFilter, null).getTotalElements());
		
		return syncConfigCustom;
	}

	@Transactional
	public void deleteAllResourceData() {
		// Delete all
		Query q = entityManager.createNativeQuery("DELETE FROM test_tree_resource");
		q.executeUpdate();
	}

	private void initData(String treeTypeCode) {

		// create test system
		system = helper.createSystem("test_tree_resource");
		system.setName(helper.createName());
		system = systemService.save(system);
		// key to EAV
		IdmFormDefinitionDto formDefinition = systemService.getConnectorFormDefinition(system);
		formService.saveValues(system, formDefinition, "keyColumn", ImmutableList.of("ID"));

		// generate schema for system
		List<SysSchemaObjectClassDto> objectClasses = systemService.generateSchema(system);

		IdmTreeTypeDto treeType = treeTypeService.getByCode(treeTypeCode);
		if (treeType == null) {
			treeType = new IdmTreeTypeDto();
			treeType.setCode(treeTypeCode);
			treeType.setName(treeTypeCode);
			treeType = treeTypeService.save(treeType);
		}

		// Create synchronization mapping
		SysSystemMappingDto syncSystemMapping = new SysSystemMappingDto();
		syncSystemMapping.setName("default_" + System.currentTimeMillis());
		syncSystemMapping.setEntityType(TreeSynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		syncSystemMapping.setTreeType(treeType.getId());
		syncSystemMapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		syncSystemMapping.setObjectClass(objectClasses.get(0).getId());
		syncSystemMapping.setAccountType(AccountType.PERSONAL);
		final SysSystemMappingDto syncMapping = systemMappingService.save(syncSystemMapping);

		createMapping(system, syncMapping);
		initTreeData();

		syncConfigService.find(null).getContent().forEach(config -> {
			syncConfigService.delete(config);
		});

	}

	private void initTreeData() {
		deleteAllResourceData(); // FIXME: data are not deleted here - DefaultTreeSynchronizationServiceTest

		entityManager.persist(this.createNode("1", null));
		entityManager.persist(this.createNode("2", null));

		entityManager.persist(this.createNode("11", "1"));
		entityManager.persist(this.createNode("12", "1"));
		entityManager.persist(this.createNode("111", "11"));
		entityManager.persist(this.createNode("112", "11"));
		entityManager.persist(this.createNode("1111", "111"));

		entityManager.persist(this.createNode("21", "2"));
		entityManager.persist(this.createNode("22", "2"));
		entityManager.persist(this.createNode("211", "21"));
		entityManager.persist(this.createNode("212", "21"));
		entityManager.persist(this.createNode("2111", "211"));
	}

	private TestTreeResource createNode(String code, String parent) {
		TestTreeResource node = new TestTreeResource();
		node.setCode(code);
		node.setName(code);
		node.setParent(parent);
		node.setId(code);
		return node;
	}
	
	@Transactional
	public void changeParent(String nodeCode, String newParentCode) {
		TestTreeResource one = entityManager.find(TestTreeResource.class, nodeCode);
		one.setParent(newParentCode);
		entityManager.persist(one);
	}

	private void createMapping(SysSystemDto system, final SysSystemMappingDto entityHandlingResult) {
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		Page<SysSchemaAttributeDto> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if (TestHelper.ATTRIBUTE_MAPPING_NAME.equals(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setUid(true);
				attributeHandlingName.setEntityAttribute(false);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				// For provisioning .. we need create UID
				attributeHandlingName.setTransformToResourceScript("if(uid){return uid;}\nreturn entity.getCode();");
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("CODE".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName("code");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("PARENT".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName("parent");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("NAME".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName("name");
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			}
		});
	}

	private TreeSyncTest getBean() {
		return applicationContext.getAutowireCapableBeanFactory().createBean(this.getClass());
	}
}
