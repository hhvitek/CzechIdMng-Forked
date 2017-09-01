package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Schema attributes
 * 
 * @author Svanda
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "schemaAttributes", //
		path = "schema-attributes", //
		itemResourceRel = "schemaAttribute", //
		exported = false // we are using repository metadata, but we want expose
							// rest endpoint manually
)
public interface SysSchemaAttributeRepository extends AbstractEntityRepository<SysSchemaAttribute, SchemaAttributeFilter> {

	@Override
	@Query(value = "select e from SysSchemaAttribute e" 
			+ " where"
			+ " (?#{[0].text} is null or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})"
	        + " and"
			+ " (?#{[0].objectClassId} is null or e.objectClass.id = ?#{[0].objectClassId})"
		    + " and"
	        + " (?#{[0].systemId} is null or e.objectClass.system.id = ?#{[0].systemId})"
			+ " and"
			+ " (?#{[0].name} is null or e.name = ?#{[0].name})")
	Page<SysSchemaAttribute> find(SchemaAttributeFilter filter, Pageable pageable);
}
