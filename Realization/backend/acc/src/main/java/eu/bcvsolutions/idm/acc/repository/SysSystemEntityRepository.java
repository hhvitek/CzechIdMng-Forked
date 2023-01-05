package eu.bcvsolutions.idm.acc.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Entity on target system.
 * 
 * @author Radek Tomiška
 *
 */
public interface SysSystemEntityRepository extends AbstractEntityRepository<SysSystemEntity> {
	
	/**
	 * @deprecated use service layer instead.
	 */
	@Deprecated(since = "12.0.0")
	@Query(value = "select e from SysSystemEntity e" +
	        " where" +
	        " (lower(e.uid) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})" +
	        " and" + 
	        " (?#{[0].systemId} is null or e.system.id = ?#{[0].systemId})" +
	        " and" +
	        " (?#{[0].uid} is null or e.uid = ?#{[0].uid})"+
	        " and" + 
	        " (?#{[0].entityType} is null or e.entityType = ?#{[0].entityType})")
	Page<SysSystemEntity> find(SysSystemEntityFilter filter, Pageable pageable);
	
	SysSystemEntity findOneBySystem_IdAndEntityTypeAndUid(@Param("systemId") UUID systemId, @Param("entityType") String entityType, @Param("uid") String uid);
	
	Long countBySystem_Id(@Param("systemId") UUID systemId);	
}
