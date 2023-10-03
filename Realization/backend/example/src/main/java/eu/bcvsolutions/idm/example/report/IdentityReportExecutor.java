package eu.bcvsolutions.idm.example.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportGenerateException;
import eu.bcvsolutions.idm.rpt.api.executor.AbstractReportExecutor;

/**
 * Example report with identities
 * - filter for enabled / disabled identities
 * - uses json stream to save a lot of identities (+creates temporary file)
 * 
 * @author Radek Tomiška
 *
 */
@Enabled(ExampleModuleDescriptor.MODULE_ID)
@Component("exampleIdentityReportExecutor")
@Description("Identities - example")
public class IdentityReportExecutor extends AbstractReportExecutor {
	
	public static final String REPORT_NAME = "example-identity-report"; // report ~ executor name
	//
	@Autowired private IdmIdentityService identityService;
	
	/**
	 * Report ~ executor name
	 */
	@Override
	public String getName() {
		return REPORT_NAME;
	}

	/**
	 * Filter form attributes:
	 * - enabled / disabled identities
	 */
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto disabled = new IdmFormAttributeDto(
				IdmIdentityFilter.PARAMETER_DISABLED, 
				"Disabled identities", 
				PersistentType.BOOLEAN);
		// we want select box instead simple checkbox (null value is needed)
		disabled.setFaceType(BaseFaceType.BOOLEAN_SELECT);
		disabled.setPlaceholder("All identities or select ...");
		return Lists.newArrayList(disabled);
	}
	
	@Override
	protected IdmAttachmentDto generateData(RptReportDto report) {
		// prepare temp file for json stream
		File temp = getAttachmentManager().createTempFile();
		//
		try (FileOutputStream outputStream = new FileOutputStream(temp)) {
	        // write into json stream
			JsonGenerator jGenerator = getMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
			try {
				// json will be array of identities
				jGenerator.writeStartArray();		
				// form instance has useful methods to transform form values
				IdmFormInstanceDto formInstance = new IdmFormInstanceDto(report, getFormDefinition(), report.getFilter());
				// initialize filter by given form - transform to multi value map
				// => form attribute defined above will be automaticaly mapped to identity filter
				IdmIdentityFilter filter = new IdmIdentityFilter(formInstance.toMultiValueMap());
				// report extends long running task - show progress by count and counter lrt attributes
				counter = 0L;
				// find a first page of identities
				Pageable pageable = PageRequest.of(0, 100, Sort.by(Direction.ASC, IdmIdentity_.username.getName()));
				do {
					Page<IdmIdentityDto> identities = identityService.find(filter, pageable, IdmBasePermission.READ);
					if (count == null) {
						// report extends long running task - show progress by count and counter lrt attributes
						count = identities.getTotalElements();
					}
					boolean canContinue = true;
					for (Iterator<IdmIdentityDto> i = identities.iterator(); i.hasNext() && canContinue;) {
						// write single identity into json
						getMapper().writeValue(jGenerator, i.next());
						//
						// supports cancel report generating (report extends long running task)
						++counter;
						canContinue = updateState();
					}		
					// iterate while next page of identities is available
					pageable = identities.hasNext() && canContinue ? identities.nextPageable() : null;
				} while (pageable != null);
				//
				// close array of identities
				jGenerator.writeEndArray();
			} finally {
				// close json stream
				jGenerator.close();
			}
			// save create temp file with array of identities in json as attachment
			return createAttachment(report, new FileInputStream(temp));
		} catch (IOException ex) {
			throw new ReportGenerateException(report.getName(), ex);
		} finally {
			FileUtils.deleteQuietly(temp);
		}
	}

}
