package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorityChange;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorityChangeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.security.api.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.core.security.service.impl.IdmAuthorityHierarchy;

/**
 * Event processor to check if authorities were removed from identity.
 * 
 * @author Jan Helbich
 *
 */
@Component
@Description("Checks modifications in identity authorities after role removal.")
public class IdentityRoleDeleteAuthoritiesProcessor extends CoreEventProcessor<IdmIdentityRoleDto> {

	public static final String PROCESSOR_NAME = "identity-role-delete-authorities-processor";

	private final IdmAuthorityChangeRepository repository;
	private final IdmIdentityRoleService identityRoleService;
	private final GrantedAuthoritiesFactory authoritiesFactory;
	private final IdmIdentityContractRepository contractRepository;
	private final IdmAuthorityHierarchy authorityHierarchy;
	
	@Autowired
	public IdentityRoleDeleteAuthoritiesProcessor(
			IdmAuthorityChangeRepository repository,
			IdmIdentityRoleService identityRoleService,
			GrantedAuthoritiesFactory authoritiesFactory,
			IdmIdentityContractRepository contractRepository,
			IdmAuthorityHierarchy authorityHierarchy) {
		super(IdentityRoleEventType.DELETE);
		//
		Assert.notNull(repository);
		Assert.notNull(identityRoleService);
		Assert.notNull(authoritiesFactory);
		Assert.notNull(contractRepository);
		Assert.notNull(authorityHierarchy);
		//
		this.repository = repository;
		this.authoritiesFactory = authoritiesFactory;
		this.identityRoleService = identityRoleService;
		this.contractRepository = contractRepository;
		this.authorityHierarchy = authorityHierarchy;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		return Integer.MAX_VALUE;
	}

	@Override
	public EventResult<IdmIdentityRoleDto> process(EntityEvent<IdmIdentityRoleDto> event) {
		// check authorities may be skipped
		Serializable serializableValue = event.getProperties().get(IdmIdentityRoleService.SKIP_CHECK_AUTHORITIES);
		if (serializableValue != null && BooleanUtils.toBoolean(serializableValue.toString())) {
			return new DefaultEventResult<>(event, this);
		}
		checkRevokedPermissions(event.getContent());
		return new DefaultEventResult<>(event, this);
	}

	private void checkRevokedPermissions(IdmIdentityRoleDto identityRole) {
		IdmIdentityContract contract = contractRepository.findOne(identityRole.getIdentityContract());
		IdmIdentity identity = contract.getIdentity(); 
		
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		roles.remove(identityRole);

		// represents the final authorities set after role removal
		Collection<? extends GrantedAuthority> withoutDeleted = authorityHierarchy.getReachableGrantedAuthorities(
				authoritiesFactory.getGrantedAuthoritiesForValidRoles(identity.getId(), roles));
		Collection<? extends GrantedAuthority> deletedAuthorities = authorityHierarchy.getReachableGrantedAuthorities(
				authoritiesFactory.getGrantedAuthoritiesForValidRoles(identity.getId(),
						Collections.singletonList(identityRole)));

		if (!authoritiesFactory.containsAllAuthorities(withoutDeleted, deletedAuthorities)) {
			// authorities were changed, update identity flag
			IdmAuthorityChange ac = repository.findOneByIdentity_Id(identity.getId());
			if (ac == null) {
				ac = new IdmAuthorityChange();
				ac.setIdentity(identity);
			}
			ac.authoritiesChanged();
			repository.save(ac);
		}
	}

}