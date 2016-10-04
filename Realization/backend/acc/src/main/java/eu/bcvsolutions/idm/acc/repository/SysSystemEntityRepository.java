package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.repository.projection.SysSystemEntityExcerpt;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;

/**
 * Entity on target system
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "systemEntities", //
		path = "systemEntities", //
		itemResourceRel = "systemEntity", //
		excerptProjection = SysSystemEntityExcerpt.class,
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface SysSystemEntityRepository extends BaseRepository<SysSystemEntity> {

	SysSystem findOneByUidAndEntityType(@Param("uid") String uid, @Param("entityType") SystemEntityType entityType);
	
	@Query(value = "select e from SysSystemEntity e" +
	        " where" +
	        " (?#{[0]} is null or e.system.id = ?#{[0]})" +
	        " and" +
	        " (lower(e.uid) like ?#{[1] == null ? '%' : '%'.concat([1].toLowerCase()).concat('%')})" +
	        " and" + 
	        " (?#{[2]} is null or e.entityType = ?#{[2]})")
	Page<SysSystemEntity> findQuick(
			@Param(value = "systemId") Long systemId,
			@Param(value = "uid") String uid,
			@Param(value = "entityType") SystemEntityType entityType,
			Pageable pageable);
	
}
