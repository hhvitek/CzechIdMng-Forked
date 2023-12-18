package eu.bcvsolutions.idm.document.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.document.domain.DocumentType;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.document.entity.DocumentEntity;
import eu.bcvsolutions.idm.document.entity.DocumentEntity_;
import eu.bcvsolutions.idm.document.repository.DocumentRepository;

@Component
@Description("Filter by document type")
public class TypeDocumentFilter extends AbstractFilterBuilder<DocumentEntity, DocumentFilter> {

	public TypeDocumentFilter(DocumentRepository repository) {
		super(repository);
	}

	@Override
	public String getName() {
		return DocumentFilter.PARAMETER_TYPE;
	}

	@Override
	public Predicate getPredicate(Root<DocumentEntity> root, AbstractQuery<?> query, CriteriaBuilder builder, DocumentFilter filter) {
		DocumentType type = filter.getType();
		if (type == null) {
			return null;
		}

		return builder.equal(root.get(DocumentEntity_.type), type);
	}
}
