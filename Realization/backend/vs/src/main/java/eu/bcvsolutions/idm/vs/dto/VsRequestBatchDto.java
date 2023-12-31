package eu.bcvsolutions.idm.vs.dto;

import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for batch request (relation between request) in virtual system
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "batches")
@Schema(description = "Request batch in virtual system")
public class VsRequestBatchDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	
}
