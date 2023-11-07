package eu.bcvsolutions.idm.core.api.rest;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;

import eu.bcvsolutions.idm.core.api.dto.AbstractWizardDto;

public interface WizardController<W extends AbstractWizardDto> {

	CollectionModel<W> getSupportedTypes();

	ResponseEntity<W> executeWizardType(W wizardDto);

	ResponseEntity<W> loadWizardType(W wizardDto);
}
