package eu.bcvsolutions.idm.core.ecm.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.ecm.api.config.AttachmentConfiguration;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.entity.IdmAttachment;
import eu.bcvsolutions.idm.core.ecm.repository.IdmAttachmentRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmProfile;
import eu.bcvsolutions.idm.test.api.AbstractVerifiableUnitTest;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Attachment manager unit tests
 * - test closing streams after attachment is saved or updated
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultAttachmentManagerUnitTest extends AbstractVerifiableUnitTest {

	private final IdmAttachmentRepository repository = mock(IdmAttachmentRepository.class);
	private final LookupService lookupService = mock(LookupService.class);
	private final AttachmentConfiguration attachmentConfiguration = mock(AttachmentConfiguration.class);
	private final ModelMapper modelMapper = spy(ModelMapper.class);

	private final DefaultAttachmentManager attachmentManager = spy(new DefaultAttachmentManager(repository,
			attachmentConfiguration, lookupService));

	@Before
	public void init() {
		when(attachmentConfiguration.getStoragePath()).thenReturn("/idm_data");
		ReflectionTestUtils.setField(attachmentManager, "modelMapper", modelMapper);
	}

	@Test
	public void testAttachmentInputStreamIsClosedAfterSave() throws IOException {		
		when(attachmentConfiguration.getStoragePath()).thenReturn("target");
		InputStream inputStreamSpy = Mockito.spy(IOUtils.toInputStream("mock"));
		//
		IdmProfile owner = new IdmProfile(UUID.randomUUID());
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setName("mock");
		attachment.setInputData(inputStreamSpy);
		//
		attachmentManager.saveAttachment(owner, attachment);
		// 
		Assert.assertNull(attachment.getInputData());
		Mockito.verify(inputStreamSpy).close();
		Mockito.verify(repository).saveAndFlush(Mockito.any(IdmAttachment.class));
		Mockito.verify(attachmentConfiguration, times(2)).getStoragePath();
	}
	
	@Test
	public void testAttachmentInputStreamIsClosedAfterUpdate() throws IOException {	
		when(attachmentConfiguration.getStoragePath()).thenReturn("target");
		when(repository.saveAndFlush(any())).thenReturn(new IdmAttachment());
		InputStream inputStreamSpy = Mockito.spy(IOUtils.toInputStream("mock"));
		//
		IdmProfile owner = new IdmProfile(UUID.randomUUID());
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setId(UUID.randomUUID());
		attachment.setName("mock");
		attachment.setInputData(inputStreamSpy);
		attachment.setOwnerId(owner.getId());
		attachment.setOwnerType("mock");
		//
		attachmentManager.updateAttachment(attachment);
		// 
		Assert.assertNull(attachment.getInputData());
		Mockito.verify(inputStreamSpy).close();
		Mockito.verify(repository).saveAndFlush(Mockito.any(IdmAttachment.class));
		Mockito.verify(repository).findById(Mockito.any(UUID.class));
		Mockito.verify(attachmentConfiguration, times(2)).getStoragePath();
	}
	
	@Test
	public void testAttachmentInputStreamIsClosedAfterIOUtilsToByteArray() throws IOException {		
		InputStream inputStreamSpy = Mockito.spy(IOUtils.toInputStream("mock"));
		// used in acc module internally
		IOUtils.toByteArray(inputStreamSpy);
		IOUtils.closeQuietly(inputStreamSpy);
		//
		Mockito.verify(inputStreamSpy, times(1)).close();
	}
	
	@Test
	public void testAttachmentInputStreamIsClosedAfterIOUtilsToString() throws IOException {		
		InputStream inputStreamSpy = Mockito.spy(IOUtils.toInputStream("mock"));
		// used in acc module internally
		IOUtils.toString(inputStreamSpy);
		IOUtils.closeQuietly(inputStreamSpy);
		//
		Mockito.verify(inputStreamSpy, times(1)).close();
	}
}
