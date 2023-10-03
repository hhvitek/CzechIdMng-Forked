package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.forest.index.service.api.ForestIndexService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultForestIndexService;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Rebuild forest index for tree type
 * 
 * @author Radek Tomiška
 *
 */
@Component(RebuildTreeNodeIndexTaskExecutor.TASK_NAME)
public class RebuildTreeNodeIndexTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RebuildTreeNodeIndexTaskExecutor.class);
	public static final String TASK_NAME = "core-rebuild-tree-node-index-long-running-task";
	private static final String PARAMETER_TREE_TYPE = "Tree type code";
	//
	@Autowired private IdmTreeTypeService treeTypeService;
	@Autowired private IdmTreeNodeRepository treeNodeRepository;
	@Autowired private ForestIndexService<IdmForestIndexEntity, UUID> forestIndexService;
	@Autowired private ConfigurationService configurationService;
	//
	private String treeTypeCode;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		treeTypeCode = getParameterConverter().toString(properties, PARAMETER_TREE_TYPE);
		// validation
		getTreeType();
	}
	
	@Override
	public Boolean process() {
		if (!configurationService.getBooleanValue(DefaultForestIndexService.PROPERTY_INDEX_ENABLED, true)) {
			throw new ResultCodeException(CoreResultCode.FOREST_INDEX_DISABLED, ImmutableMap.of("property", DefaultForestIndexService.PROPERTY_INDEX_ENABLED));
		}
		IdmTreeTypeDto treeType = getTreeType();
		String longRunningTaskId = configurationService.getValue(treeTypeService.getConfigurationPropertyName(treeTypeCode, IdmTreeTypeService.CONFIGURATION_PROPERTY_REBUILD));
		if (StringUtils.hasLength(longRunningTaskId) && !longRunningTaskId.equals(getLongRunningTaskId().toString())) {
			throw new ResultCodeException(CoreResultCode.FOREST_INDEX_RUNNING, ImmutableMap.of("type", IdmTreeNode.toForestTreeType(treeType.getId())));
		}
		//
		LOG.info("Starting rebuilding tree node index for tree type code [{}].", treeTypeCode);
		//
		// clear all rgt, lft
		try {
			forestIndexService.dropIndexes(IdmTreeNode.toForestTreeType(treeType.getId()));
		} finally {
			configurationService.setBooleanValue(treeTypeService.getConfigurationPropertyName(treeTypeCode, IdmTreeTypeService.CONFIGURATION_PROPERTY_VALID), false);
		}
		try {
			configurationService.setValue(treeTypeService.getConfigurationPropertyName(treeTypeCode, IdmTreeTypeService.CONFIGURATION_PROPERTY_REBUILD), getLongRunningTaskId().toString());
			//
			count = treeNodeRepository.findByTreeType_Id(treeType.getId(), PageRequest.of(0, 1)).getTotalElements();
			counter = 0L;
			boolean canContinue = true;
			Page<IdmTreeNode> roots = treeNodeRepository.findRoots(
					treeType.getId(), 
					PageRequest.of(
							0, 
							100,
							Sort.by(Direction.ASC, AbstractEntity_.id.getName())
					)
			);
			while (canContinue) {
				canContinue = processChildren(roots.getContent());
				if (!canContinue) {
					break;
				}
				if (!roots.hasNext()) {
					break;
				}
				roots = treeNodeRepository.findRoots(treeType.getId(), roots.nextPageable());
			}
			//
			if (count.equals(counter)) {
				configurationService.deleteValue(treeTypeService.getConfigurationPropertyName(treeTypeCode, IdmTreeTypeService.CONFIGURATION_PROPERTY_VALID));
				LOG.info("Tree node index for tree type code [{}] was successfully rebuilt (index size [{}]).", treeTypeCode, counter);
				return Boolean.TRUE;
			} 
			//
			LOG.warn("Tree node index for tree type code [{}] rebuild was canceled (index size [{}]).", treeTypeCode, counter);
			return Boolean.FALSE;
		} finally {
			configurationService.deleteValue(treeTypeService.getConfigurationPropertyName(treeTypeCode, IdmTreeTypeService.CONFIGURATION_PROPERTY_REBUILD));
		}
	}
	
	private boolean processChildren(List<IdmTreeNode> nodes) {
		boolean canContinue = true;
		for(IdmTreeNode node : nodes) {
			if (node.getForestIndex() == null) {
				forestIndexService.index(node.getForestTreeType(), node.getId(), node.getParentId());
			}
			counter++;
			canContinue = updateState();
			if (!canContinue) {
				break;
			}
			// proces nodes childred
			canContinue = processChildren(treeNodeRepository.findDirectChildren(node, null).getContent());
			if (!canContinue) {
				break;
			}
		};
		return canContinue;
	}
	
	private IdmTreeTypeDto getTreeType() {
		IdmTreeTypeDto treeType = getLookupService().lookupDto(IdmTreeTypeDto.class, treeTypeCode);
		if(treeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,
					ImmutableMap.of("entity", treeTypeCode));
		}
		return treeType;
	}
	
	public void setTreeTypeCode(String treeTypeCode) {
		this.treeTypeCode = treeTypeCode;
	}
	
	public String getTreeTypeCode() {
		return treeTypeCode;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_TREE_TYPE);
		return parameters;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto treeType = new IdmFormAttributeDto(
				PARAMETER_TREE_TYPE,
				PARAMETER_TREE_TYPE, 
				PersistentType.UUID);
		treeType.setRequired(true);
		treeType.setFaceType(BaseFaceType.TREE_TYPE_SELECT);
		//
		return Lists.newArrayList(treeType);
	}
	
	@Override
    public boolean isRecoverable() {
    	return true;
    }
}
