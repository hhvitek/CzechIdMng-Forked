package eu.bcvsolutions.idm.document.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.document.domain.DocumentState;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.document.entity.DocumentEntity;
import eu.bcvsolutions.idm.document.entity.DocumentEntity_;
import eu.bcvsolutions.idm.document.repository.DocumentRepository;

@Component
@Description("State by document state")
public class StateDocumentFilter extends AbstractFilterBuilder<DocumentEntity, DocumentFilter> {

	public StateDocumentFilter(DocumentRepository repository) {
		super(repository);
	}

	@Override
	public String getName() {
		return DocumentFilter.PARAMETER_STATE;
	}

	@Override
	public Predicate getPredicate(Root<DocumentEntity> root, AbstractQuery<?> query, CriteriaBuilder builder, DocumentFilter filter) {
		DocumentState state = filter.getState();
		if (state == null) {
			return null;
		}

		return builder.equal(root.get(DocumentEntity_.state), state);
	}
}
