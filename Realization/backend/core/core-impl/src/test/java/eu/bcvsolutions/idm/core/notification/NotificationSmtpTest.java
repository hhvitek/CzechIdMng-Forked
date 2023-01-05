package eu.bcvsolutions.idm.core.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.nilhcem.fakesmtp.core.exception.BindPortException;
import com.nilhcem.fakesmtp.core.exception.OutOfRangePortException;
import com.nilhcem.fakesmtp.model.EmailModel;

import eu.bcvsolutions.idm.core.api.config.domain.EmailerConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitTestDataProcessor;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.workflow.config.WorkflowConfig;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.test.api.AbstractNotificationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Testing sending emails via smtp server. Server is running locally.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotificationSmtpTest extends AbstractNotificationTest {

	public static final String TOPIC = "idm:smtpTest";
	public static final String TEST_TEMPLATE = "testTemplate";
	public static final String FROM = "idm-test@bcvsolutions.eu";
	public static final String TO_WF = "testUserSmtpServer1";
	public static final String WF_NAME = "testNotificationSmtp";

	@Autowired
	private TestHelper helper;
	@Autowired
	private NotificationManager notificationManager;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmNotificationConfigurationService notificationConfigurationService;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private WorkflowProcessInstanceService processInstanceService;
	@Autowired
	private IdmNotificationLogService notificationLogService;
	@Autowired
	private AttachmentManager attachmentManager;

	@Before
	public void init() {
		loginAsAdmin();
		//
		initConfiguration();
	}

	@After
	public void stop() {
		configurationService.setValue(EmailerConfiguration.PROPERTY_TEST_ENABLED, Boolean.TRUE.toString());
		configurationService.setValue(WorkflowConfig.SEND_NOTIFICATION_CONFIGURATION_PROPERTY,
				Boolean.FALSE.toString());
		super.logout();
	}

	@Test
	public void A_sendEmailViaSmtpByManager() throws InterruptedException, BindPortException, OutOfRangePortException {
		// in first test start smtp server
		if (!this.isRunning()) {
			this.startSmtpServer();
		}
		
		// create config
		NotificationConfigurationDto config = new NotificationConfigurationDto();
		config.setTopic(TOPIC);
		config.setLevel(NotificationLevel.SUCCESS);
		config.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE);
		config = notificationConfigurationService.save(config);
		
		// init observer for this test only
		NotificationObserver observer = new NotificationObserver(1);
		this.addObserver(observer);

		int currentEmails = observer.getEmails().size();
		assertTrue(this.isRunning());

		String textHtml = "textHtml-" + System.currentTimeMillis();
		String textText = "textText-" + System.currentTimeMillis();
		String subject = "subject-" + System.currentTimeMillis();

		IdmIdentityDto identity = helper.createIdentity();
		identity.setEmail("example@example.tld");
		identity = identityService.save(identity);

		notificationManager.send(TOPIC, new IdmMessageDto.Builder().setTextMessage(textText).setHtmlMessage(textHtml)
				.setSubject(subject).setLevel(NotificationLevel.SUCCESS).build(), identity);

		// email is send by apache camel asynchronously
		if (observer.getEmails().size() == currentEmails) {
			observer.waitForMails();
		}

		assertEquals(currentEmails + 1, observer.getEmails().size());
		assertTrue(this.isRunning());

		for (EmailModel email : observer.getEmails()) {
			assertEquals(FROM, email.getFrom());
			assertTrue(email.getEmailStr().contains(textHtml));
			assertEquals(subject, email.getSubject());
			assertEquals(identity.getEmail(), email.getTo());
		}
		
		notificationConfigurationService.delete(config);
	}

	@Test
	public void B_sendEmailViaSmtpByWf() throws InterruptedException, BindPortException, OutOfRangePortException {
		if (!this.isRunning()) {
			this.startSmtpServer();
		}
		assertTrue(this.isRunning());
		
		// init observer for this test only
		NotificationObserver observer = new NotificationObserver(1);
		this.addObserver(observer);
		int currentEmails = observer.getEmails().size();

		IdmIdentityDto identity = identityService.getByUsername(TO_WF);
		if (identity == null) {
			identity = helper.createIdentity(TO_WF);
		}
		identity.setEmail("example@example.tld");
		identity = identityService.save(identity);

		IdmIdentityDto testUser1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		processInstanceService.startProcess(WF_NAME, null, testUser1.getId().toString(), null);
		// email is send by apache camel asynchronously
		if (observer.getEmails().size() == currentEmails) {
			observer.waitForMails();
		}

		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(identity.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		//
		assertEquals(1, notifications.size());

		assertTrue(this.isRunning());
		assertEquals(currentEmails + 1, observer.getEmails().size());

		for (EmailModel email : observer.getEmails()) {
			assertEquals(FROM, email.getFrom());
			assertEquals(identity.getEmail(), email.getTo());
		}

		// in last test stop smtp server
		this.stopSmtpServer();
	}
	
	/**
	 * Smtp email model don't contain attachments - it's tested, if attachment are not lost diring processing only.
	 */
	@Test
	public void C_sendEmailWithAttachments() {
		// create config
		NotificationConfigurationDto config = new NotificationConfigurationDto();
		config.setTopic(TOPIC);
		config.setLevel(NotificationLevel.SUCCESS);
		config.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE);
		config = notificationConfigurationService.save(config);

		String textHtml = "textHtml-" + System.currentTimeMillis();
		String textText = "textText-" + System.currentTimeMillis();
		String subject = "subject-" + System.currentTimeMillis();

		IdmIdentityDto identity = helper.createIdentity();
		identity.setEmail("example@example.tld");
		identity = identityService.save(identity);
		
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setName("rest2.txt");
		attachment.setInputData(IOUtils.toInputStream("test txt content 1234567899 ě+ščřžýáííéáýžřčšě+;ěščřžýáíééů", AttachableEntity.DEFAULT_CHARSET));
		attachment.setEncoding(AttachableEntity.DEFAULT_ENCODING);
		attachment.setMimetype("text/plain");

		List<IdmNotificationLogDto> send = notificationManager.send(
				TOPIC,
				new IdmMessageDto
					.Builder()
					.setTextMessage(textText)
					.setHtmlMessage(textHtml)
					.setSubject(subject)
					.setLevel(NotificationLevel.SUCCESS)
					.build(),
				null,
				Lists.newArrayList(identity),
				Lists.newArrayList(attachment));
		
		assertEquals(attachment.getName(), send.get(0).getAttachments().get(0).getName());
		
		notificationConfigurationService.delete(config);
	}
	
	/**
	 * Smtp email model don't contain attachments - it's tested, if attachment are not lost diring processing only.
	 */
	@Test
	public void D_sendEmailWithPersistedAttachments() {
		// create config
		NotificationConfigurationDto config = new NotificationConfigurationDto();
		config.setTopic(TOPIC);
		config.setLevel(NotificationLevel.SUCCESS);
		config.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE);
		config = notificationConfigurationService.save(config);

		String textHtml = "textHtml-" + System.currentTimeMillis();
		String textText = "textText-" + System.currentTimeMillis();
		String subject = "subject-" + System.currentTimeMillis();

		IdmIdentityDto identity = helper.createIdentity();
		identity.setEmail("example@example.tld");
		identity = identityService.save(identity);
		
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setName("rest2.txt");
		attachment.setInputData(IOUtils.toInputStream("test txt content 1234567899 ě+ščřžýáííéáýžřčšě+;ěščřžýáíééů", AttachableEntity.DEFAULT_CHARSET));
		attachment.setEncoding(AttachableEntity.DEFAULT_ENCODING);
		attachment.setMimetype("text/plain");
		attachment.setOwnerType("mock");
		attachment.setOwnerId(UUID.randomUUID());
		attachment = attachmentManager.saveAttachment(null, attachment);
		
		assertNull(attachment.getInputData());

		List<IdmNotificationLogDto> send = notificationManager.send(
				TOPIC,
				new IdmMessageDto
					.Builder()
					.setTextMessage(textText)
					.setHtmlMessage(textHtml)
					.setSubject(subject)
					.setLevel(NotificationLevel.SUCCESS)
					.build(),
				null,
				Lists.newArrayList(identity),
				Lists.newArrayList(attachment));
		
		assertEquals(attachment.getName(), send.get(0).getAttachments().get(0).getName());
		
		notificationConfigurationService.delete(config);
	}

	private void initConfiguration() {
		configurationService.setValue(EmailerConfiguration.PROPERTY_FROM, FROM);
		configurationService.setValue(EmailerConfiguration.PROPERTY_PORT,
				String.valueOf(AbstractNotificationTest.DEFAULT_SMTP_PORT));
		configurationService.setValue(EmailerConfiguration.PROPERTY_PROTOCOL, AbstractNotificationTest.PROTOCOL);
		// enable sending emails
		configurationService.setValue(EmailerConfiguration.PROPERTY_TEST_ENABLED, Boolean.FALSE.toString());
		// enable send notification from wf
		configurationService.setValue(WorkflowConfig.SEND_NOTIFICATION_CONFIGURATION_PROPERTY, Boolean.TRUE.toString());
		configurationService.setValue(EmailerConfiguration.PROPERTY_HOST, AbstractNotificationTest.HOST);
	}

}
