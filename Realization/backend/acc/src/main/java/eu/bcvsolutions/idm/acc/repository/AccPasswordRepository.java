package eu.bcvsolutions.idm.acc.repository;

import eu.bcvsolutions.idm.acc.entity.AccPassword;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Storing crypted account passwords to IdM
 * 
 * @author Jirka Koula
 *
 */
public interface AccPasswordRepository extends AbstractEntityRepository<AccPassword> {
	
}
