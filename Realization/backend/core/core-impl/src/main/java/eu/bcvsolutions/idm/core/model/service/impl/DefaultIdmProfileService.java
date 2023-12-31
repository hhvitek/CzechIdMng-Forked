package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.domain.PrivateIdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.dto.PanelDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmProfileFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmProfileService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmProfile;
import eu.bcvsolutions.idm.core.model.entity.IdmProfile_;
import eu.bcvsolutions.idm.core.model.repository.IdmProfileRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Operations with profiles.
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
public class DefaultIdmProfileService
		extends AbstractEventableDtoService<IdmProfileDto, IdmProfile, IdmProfileFilter> 
		implements IdmProfileService {
	
	@Autowired private LookupService lookupService;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private PrivateIdentityConfiguration identityConfiguration;
	
	@Autowired
	public DefaultIdmProfileService(
			IdmProfileRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.PROFILE, getEntityClass());
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmProfileDto findOneByIdentity(Serializable identityIdentifier, BasePermission... permission) {
		Assert.notNull(identityIdentifier, "Identity identifier is required.");
		IdmIdentityDto identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identityIdentifier);
		if (identity == null) {
			return null;
		}
		return findOneByIdentity(identity, permission);
	}
	
	@Override
	@Transactional
	public IdmProfileDto findOrCreateByIdentity(Serializable identityIdentifier, BasePermission... permission) {
		Assert.notNull(identityIdentifier, "Identity identifier is required.");
		IdmIdentityDto identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identityIdentifier);
		if (identity == null) {
			return null;
		}
		//
		IdmProfileDto profile = this.findOneByIdentity(identity, permission);
		//
		if (profile != null) {
			return profile;
		}
		// TODO: two profiles can be created in multi thread access (lock by identity before the get)
		profile = new IdmProfileDto();
		profile.setIdentity(identity.getId());
		//
		return save(profile, permission);
	}
	
	@Override
	@Transactional
	public IdmProfileDto uploadImage(IdmProfileDto profile, MultipartFile data, String fileName, BasePermission... permission) {
		Assert.notNull(profile, "Profile is required.");
		Assert.notNull(profile.getId(), "Profile identifier is required.");
		//
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setName(fileName);
		attachment.setMimetype(StringUtils.isBlank(data.getContentType()) ? AttachableEntity.DEFAULT_MIMETYPE : data.getContentType());
		//
		// check required image content type
		String contentType = data.getContentType();
		if (StringUtils.isBlank(contentType) || !contentType.toLowerCase().startsWith("image/")) {
			throw new ResultCodeException(
					CoreResultCode.IDENTITY_PROFILE_IMAGE_WRONG_CONTENT_TYPE, 
					ImmutableMap.of("contentType", String.valueOf(contentType))
			);
		}
		// check maximum image size
		long fileSize = data.getSize();
		long profileImageMaxFileSize = identityConfiguration.getProfileImageMaxFileSize();
		if (fileSize > profileImageMaxFileSize) {
			throw new ResultCodeException(
					CoreResultCode.IDENTITY_PROFILE_IMAGE_MAX_FILE_SIZE_EXCEEDED, 
					ImmutableMap.of(
							"fileSize", FileUtils.byteCountToDisplaySize(fileSize),
							"maxFileSize", FileUtils.byteCountToDisplaySize(profileImageMaxFileSize)
					)
			);
		}
		//
		try {
			attachment.setInputData(data.getInputStream());
			attachment = attachmentManager.saveAttachmentVersion(profile, attachment, profile.getImage());
			//
			profile.setImage(attachment.getId());
			//
			return save(profile, permission);
		} catch (IOException ex) {
			throw new ResultCodeException(CoreResultCode.ATTACHMENT_CREATE_FAILED, ImmutableMap.of(
					"attachmentName", attachment.getName(),
					"ownerType", attachmentManager.getOwnerType(profile),
					"ownerId", profile.getId().toString())
					, ex);
		}
	}
	
	@Override
	@Transactional
	public IdmProfileDto deleteImage(IdmProfileDto profile, BasePermission... permission) {
		Assert.notNull(profile, "Profile is required.");
		Assert.notNull(profile.getId(), "Profile identifier is required.");
		//
		if (profile.getImage() == null) {
			return profile;
		}
		UUID attachmentId = profile.getImage();
		profile.setImage(null);
		// save profile
		profile = save(profile, permission);
		// delete attachment after profile is saved => and persmissions are evaluated 
		attachmentManager.deleteAttachment(attachmentId);
		//
		return profile;
	}
	
	@Override
	@Transactional
	public IdmProfileDto collapsePanel(Serializable identityIdentifier, String panelIdentifier, BasePermission... permission) {
		return setPanelCollapsed(identityIdentifier, panelIdentifier, true, permission);
	}
	
	@Override
	@Transactional
	public IdmProfileDto expandPanel(Serializable identityIdentifier, String panelIdentifier, BasePermission... permission) {
		return setPanelCollapsed(identityIdentifier, panelIdentifier, false, permission);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmProfile> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmProfileFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			throw new UnsupportedOperationException("Filter by text is not supported");
		}
		// Identity first name
		UUID identityId = filter.getIdentityId();
		if (identityId != null) {
			predicates.add(builder.equal(root.get(IdmProfile_.identity).get(IdmIdentity_.id), identityId));
		}
		//
		return predicates;
	}
	
	private IdmProfileDto findOneByIdentity(IdmIdentityDto identity, BasePermission... permission) {
		Assert.notNull(identity, "Identity is required.");
		//
		IdmProfileFilter filter = new IdmProfileFilter();
		filter.setIdentityId(identity.getId());
		List<IdmProfileDto> profiles = find(filter, null, permission).getContent();
		//
		return profiles.isEmpty() ? null : profiles.get(0);
	}

	private IdmProfileDto setPanelCollapsed(
			Serializable identityIdentifier, 
			String panelIdentifier, 
			boolean collapsed, 
			BasePermission... permission) {
		IdmProfileDto profile = findOrCreateByIdentity(identityIdentifier, permission);
		//
		ConfigurationMap setting = profile.getSetting();
		PanelDto panel = (PanelDto) setting.get(panelIdentifier);
		if (panel == null) {
			panel = new PanelDto(panelIdentifier);
		}
		panel.setCollapsed(collapsed);
		setting.put(panelIdentifier, panel);
		profile.setSetting(setting);
		//
		return save(profile, permission);
	}
}
