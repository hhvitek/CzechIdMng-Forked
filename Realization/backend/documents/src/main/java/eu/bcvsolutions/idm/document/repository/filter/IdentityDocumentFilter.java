package eu.bcvsolutions.idm.document.repository.filter;

import java.util.UUID;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.document.entity.DocumentEntity;
import eu.bcvsolutions.idm.document.entity.DocumentEntity_;
import eu.bcvsolutions.idm.document.repository.DocumentRepository;

@Component
@Description("Filter by document identity")
public class IdentityDocumentFilter extends AbstractFilterBuilder<DocumentEntity, DocumentFilter> {

	public IdentityDocumentFilter(DocumentRepository repository) {
		super(repository);
	}

	@Override
	public String getName() {
		return DocumentFilter.PARAMETER_IDENTITY;
	}

	@Override
	public Predicate getPredicate(Root<DocumentEntity> root, AbstractQuery<?> query, CriteriaBuilder builder, DocumentFilter filter) {
		UUID identity = filter.getIdentity();
		if (identity == null) {
			return null;
		}

		return builder.equal(root.get(DocumentEntity_.identity).get(AbstractEntity_.id), identity);
	}
}
