package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.model.dto.BaseFilter;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.service.BaseEntityService;
import eu.bcvsolutions.idm.core.model.service.ReadEntityService;

/**
 * Provide additional methods to retrieve entities using the pagination and
 * sorting abstraction.
 * 
 * @author Radek Tomiška
 * @see Sort
 * @see Pageable
 * @see Page
 */
public abstract class AbstractReadEntityService<E extends BaseEntity, F extends BaseFilter> implements ReadEntityService<E, F> {
	
	private final Class<E> entityClass;
	
	@SuppressWarnings("unchecked")
	public AbstractReadEntityService() {
		entityClass = (Class<E>)GenericTypeResolver.resolveTypeArgument(getClass(), BaseEntityService.class);
	}
	
	/**
	 * Returns underlying repository
	 * 
	 * @return
	 */
	protected abstract BaseRepository<E, F> getRepository();

	/**
	 * Returns {@link BaseEntity} type class, which is controlled by this service
	 * 
	 * @return
	 */
	@Override
	public Class<E> getEntityClass() {
		return entityClass;
	}

	@Override
	@Transactional(readOnly = true)
	public E get(Long id) {
		return getRepository().findOne(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<E> find(F filter, Pageable pageable) {
		if (filter == null) {
			return find(pageable);
		}
		return getRepository().find(filter, pageable);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<E> find(Pageable pageable) {
		return getRepository().findAll(pageable);
	}

}
