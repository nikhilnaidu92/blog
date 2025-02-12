package com.aexp.commercial.data.service;

import com.aexp.commercial.data.config.BatchPropertyConfig;
import com.aexp.commercial.data.model.db.EmailRequestEntity;
import com.aexp.commercial.data.model.raven.RavenAddress;
import com.aexp.commercial.data.model.raven.RavenChannelType;
import com.aexp.commercial.data.model.raven.RavenEvent;
import com.aexp.commercial.data.model.raven.RavenRecipient;
import com.aexp.commercial.data.model.raven.RavenRequest;
import com.aexp.commercial.data.repository.EmailRequestRepository;
import com.aexp.commercial.data.triggers.DataFeedTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ReminderEmailService {

  private static final Logger log = LoggerFactory.getLogger(DataFeedTrigger.class);
  private final BatchPropertyConfig batchPropertyConfig;
  private final EmailRequestRepository emailRequestRepository;
  private final RavenCommunicationService ravenCommunicationService;

  public ReminderEmailService(EmailRequestRepository emailRequestRepository,
                              RavenCommunicationService ravenCommunicationService, BatchPropertyConfig batchPropertyConfig) {
    this.emailRequestRepository = emailRequestRepository;
    this.ravenCommunicationService = ravenCommunicationService;
    this.batchPropertyConfig = batchPropertyConfig;
  }

  public void sendReminderEmails() {
    // Fetch all the email requests that are in pending status for more than 7 days
    List<EmailRequestEntity> emailRequests =
      emailRequestRepository.findEmailRequestEntitiesByStatusTextAndLastUpdateTimestampIsLessThan("PENDING",
        LocalDateTime.now().minusDays(7));
    log.info("Processing reminder emails for the size: {}", emailRequests.size());

    for (EmailRequestEntity emailRequestEntity : emailRequests) {
      if (emailRequestEntity != null) {
        log.info("Sending reminder email for email request id: {}", emailRequestEntity.getEmailRequestIdentifier());

        //Create Raven request
        RavenRequest ravenRequest = new RavenRequest();

        RavenEvent ravenEvent = new RavenEvent();
        ravenEvent.setId("Reminder_information_requested_doddfrank");
        String formUrl = batchPropertyConfig.getRavensBaseFormUrl();
        if (emailRequestEntity.getNonSensitiveDataIndicator()) {
          log.info("Non sensitive data indicator is true");
          formUrl = batchPropertyConfig.getRavenExtendedFormUrl();
        }
        ravenEvent.getParams().put("dynamicUrl", formUrl + emailRequestEntity.getEmailRequestIdentifier());
        if (emailRequestEntity.getCustomerFirstNm() != null && emailRequestEntity.getCustomerLastNm() != null) {
          ravenEvent.getParams().put("firstName", emailRequestEntity.getCustomerFirstNm());
          ravenEvent.getParams().put("lastName", emailRequestEntity.getCustomerLastNm());
          ravenEvent.getParams().put("customerRequestType", "customerName");
        } else if (emailRequestEntity.getCompanyName() != null) {
          ravenEvent.getParams().put("companyName", emailRequestEntity.getCompanyName());
          ravenEvent.getParams().put("customerRequestType", "companyName");
        }
        ravenEvent.getParams().put("eventType", "Initial");
        ravenRequest.setEvent(ravenEvent);

        RavenRecipient recipient = new RavenRecipient();
        recipient.setSchema("CREDITPROSPECT");
        recipient.setId(String.valueOf(emailRequestEntity.getEmailRequestIdentifier()));
        ravenRequest.setRecipient(recipient);

        List<RavenAddress> addresses = new ArrayList<>();
        RavenAddress ravenAddress = new RavenAddress();
        ravenAddress.setType(RavenChannelType.EMAIL.name());
        ravenAddress.setTo(Collections.singletonList(emailRequestEntity.getEmailAddressText()));
        addresses.add(ravenAddress);
        ravenRequest.setAddresses(addresses);

        // Send a reminder email to the applicant
        ravenCommunicationService.sendEmail(ravenRequest, emailRequestEntity.getEmailRequestIdentifier(),
          emailRequestEntity.getEmailTrackingIdentifier());

        // Update the email request status to sent
        emailRequestEntity.setLastUpdateTimestamp(LocalDateTime.now());
//        emailRequestRepository.save(emailRequestEntity);

        // Log the email request and application details
        log.info("Reminder email sent to: {} and for email request id: {}", emailRequestEntity.getEmailAddressText(),
          emailRequestEntity.getEmailRequestIdentifier());
      }
    }
  }
}
