package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.AbstractPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.filter.AbstractPasswordHistoryFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractPasswordHistoryService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Default common implementation of service that validate same password trough history.
 *
 * @author Jirka Koula
 *
 */
public class AbstractIdmPasswordHistoryService<D extends AbstractPasswordHistoryDto, E extends AbstractEntity, E_ extends AbstractEntity_, F extends AbstractPasswordHistoryFilter>
		extends AbstractReadWriteDtoService<D, E, F>
		implements AbstractPasswordHistoryService<D, F> {

	private final F filter;

	public AbstractIdmPasswordHistoryService(AbstractEntityRepository<E> repository, F filter) {
		super(repository);
		this.filter = filter;
	}

	@Override
	public boolean checkHistory(UUID entityId, int countOfIteration, GuardedString newPassword) {
		Assert.notNull(entityId, "Entity id can't be null.");
		Assert.notNull(newPassword, "New password can't be null.");
		//
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
		//
		for (D passwordHistory : getPasswordHistoryForEntity(entityId, countOfIteration)) {
			boolean matches = encoder.matches(newPassword.asString(), passwordHistory.getPassword());
			if (matches) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void deleteAllByEntity(UUID entityId) {
		Assert.notNull(entityId, "Entity id can't be null.");
		//
		filter.setEntityId(entityId);
		//
		for (D passwordHistory : this.find(filter, null)) {
			this.delete(passwordHistory);
		}
	}

	/**
	 * Return list of {@link IdmPasswordHistoryDto} for given identity id.
	 *
	 * @param entityId
	 * @param count
	 * @return
	 */
	private List<D> getPasswordHistoryForEntity(UUID entityId, int count) {
		filter.setEntityId(entityId);
		return this.find(filter, PageRequest.of(0, count, new Sort(Direction.DESC, E_.created.getName()))).getContent();
	}

}
