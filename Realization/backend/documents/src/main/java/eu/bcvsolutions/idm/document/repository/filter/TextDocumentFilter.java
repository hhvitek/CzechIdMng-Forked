package eu.bcvsolutions.idm.document.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.document.entity.DocumentEntity;
import eu.bcvsolutions.idm.document.entity.DocumentEntity_;
import eu.bcvsolutions.idm.document.repository.DocumentRepository;

/**
 * Document filter by text cloned from example module
 *
 */
@Component
@Description("Document filter - by text. Search as \"like\" in fields - lower, case insensitive.")
public class TextDocumentFilter extends AbstractFilterBuilder<DocumentEntity, DocumentFilter> {

	public TextDocumentFilter(DocumentRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return DataFilter.PARAMETER_TEXT; // "text"
	}
	
	@Override
	public Predicate getPredicate(Root<DocumentEntity> root, AbstractQuery<?> query, CriteriaBuilder builder, DocumentFilter filter) {
		String text = filter.getText();
		if (StringUtils.isEmpty(filter.getText())) {
			return null;
		}
		//
		text = text.toLowerCase();
		return builder.or(
				builder.like(builder.lower(root.get(DocumentEntity_.FIRST_NAME)), "%" + text + "%"),
				builder.like(builder.lower(root.get(DocumentEntity_.LAST_NAME)), "%" + text + "%"),
				builder.like(builder.lower(root.get(DocumentEntity_.STATE)), "%" + text + "%"),
				builder.like(builder.lower(root.get(DocumentEntity_.TYPE)), "%" + text + "%")
				);
	}
}