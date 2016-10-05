package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.model.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.projection.IdmIdentityExcerpt;

/**
 * Repository for identities
 * 
 * @author Radek Tomiška 
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "identities", //
		path = "identities", //
		itemResourceRel = "identity", //
		excerptProjection = IdmIdentityExcerpt.class,
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface IdmIdentityRepository extends BaseRepository<IdmIdentity, QuickFilter> {

	IdmIdentity findOneByUsername(@Param("username") String username);

	@Override
	@Query(value = "select e from IdmIdentity e" +
	        " where" +
	        " lower(e.username) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}" +
	        " or lower(e.firstName) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}" +
	        " or lower(e.lastName) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}" +
	        " or lower(e.email) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}" +
	        " or lower(e.description) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}")
	Page<IdmIdentity> find(QuickFilter filter, Pageable pageable);
	
	@Transactional(timeout = 5)
	@Query(value = "SELECT e FROM IdmIdentity e "
			+ "JOIN e.roles roles "
			+ "WHERE "
	        + "roles.role.id =:roleId")
	List<IdmIdentity> findAllByRole(@Param(value = "roleId") Long roleId);
}
