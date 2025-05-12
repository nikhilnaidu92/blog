package com.aexp.gcs.supplierprofile.scriptsupport.service;

import com.aexp.bta.onexp.domain.FeatureFlag;
import com.aexp.bta.onexp.service.FeatureFlagService;
import com.aexp.bta.onexp.service.FeatureToggles;
import com.aexp.gcs.supplierprofile.profilecommons.repository.app.ProfileAcceptanceRepository;
import com.aexp.gcs.supplierprofile.profilecommons.repository.app.ProfileCompanyDetailRepository;
import com.aexp.gcs.supplierprofile.profilecommons.repository.app.ProfileCompanyRepository;
import com.aexp.gcs.supplierprofile.profilecommons.repository.app.ProfileContactRepository;
import com.aexp.gcs.supplierprofile.profilecommons.repository.app.ProfileDemographicsRepository;
import com.aexp.gcs.supplierprofile.profilecommons.repository.app.ProfileEventRepository;
import com.aexp.gcs.supplierprofile.profilecommons.repository.app.ProfilePaymentAccountRepository;
import com.aexp.gcs.supplierprofile.profilecommons.repository.app.ProfileRelationshipRepository;
import com.aexp.gcs.supplierprofile.profilecommons.repository.app.ProfileRepository;
import com.aexp.gcs.supplierprofile.profilecommons.util.CouchbasejsonUtils;
import com.aexp.gcs.supplierprofile.profilecommons.util.EncryptDecryptUtil;
import com.aexp.gcs.supplierprofile.profilecommons.util.OneSupplierLogFacade;
import com.aexp.gcs.supplierprofile.profilecommons.util.ProfileCommonUtil;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.Action;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.ActorSystem;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.BusinessType;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.CompanySource;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.CouchbaseDocumentType;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.EventType;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.OnboardingStatus;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.Profile;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfileAcceptance;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfileCompany;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfileCompanyDetail;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfileContact;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfileDemographics;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfileEvent;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfilePaymentAccount;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfileRelationship;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.db.AcceptanceDetail;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.db.AcceptanceType;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.db.CompanyAlias;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.db.CompanyDetails;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.db.CompanyProfile;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.db.CompanyProfileEvent;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.Event;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.Acceptance;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.CompanyContact;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.CompanyId;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.Contact;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.ContactDestination;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.CounterpartyDetails;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.CounterpartyId;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.Demographics;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.GenericContact;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.Location;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.PaymentAccount;
import com.aexp.gcs.supplierprofile.scriptsupport.model.BatchParameter;
import com.aexp.gcs.supplierprofile.scriptsupport.model.FeatureFlagConstants;
import com.aexp.gcs.supplierprofile.scriptsupport.model.MigrationResult;
import com.aexp.gcs.supplierprofile.scriptsupport.model.QueuedDocs;
import com.aexp.gcs.supplierprofile.scriptsupport.repository.CouchbaseRepo;
import com.aexp.gcs.supplierprofile.scriptsupport.repository.RedisRepository;
import com.aexp.gcs.supplierprofile.scriptsupport.util.BatchProcessUtil;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import jakarta.persistence.PersistenceException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.aexp.gcs.supplierprofile.profilecommons.util.ProfileCommonUtil.createAliasDocName;
import static com.aexp.gcs.supplierprofile.profilecommons.util.ProfileCommonUtil.findByIdAndSource;
import static com.aexp.gcs.supplierprofile.profiledatamodel.constants.CouchbaseDocumentType.company_alias;
import static com.aexp.gcs.supplierprofile.profiledatamodel.constants.CouchbaseDocumentType.company_profile;
import static com.aexp.gcs.supplierprofile.profiledatamodel.constants.SPFIdType.PROFILE_CORRELATION_ID;
import static com.aexp.gcs.supplierprofile.scriptsupport.model.Constants.CM11_TYPE;
import static com.aexp.gcs.supplierprofile.scriptsupport.model.Constants.CM15_TYPE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.IterableUtils.isEmpty;

@Data
@Service
@RequiredArgsConstructor
public class DatabaseMigrationService {

  private final Environment environment;
  private final CouchbaseRepo couchbaseRepo;
  private final ProfileRepository profileRepository;
  private final ProfileAcceptanceRepository profileAcceptanceRepository;
  private final ProfileCompanyDetailRepository profileCompanyDetailRepository;
  private final ProfileCompanyRepository profileCompanyRepository;
  private final ProfileContactRepository profileContactRepository;
  private final ProfileDemographicsRepository profileDemographicsRepository;
  private final ProfileEventRepository profileEventRepository;
  private final ProfilePaymentAccountRepository profilePaymentAccountRepository;
  private final ProfileRelationshipRepository profileRelationshipRepository;
  private final MigratedDocumentValidationService migratedDocumentValidationService;
  private final BatchProcessUtil batchProcessUtil;
  private final FeatureFlagService featureFlagService;
  private final RedisRepository redisRepository;
  private String className = this.getClass().getSimpleName();
  private String appName = "PostgresMigrationBatch";

  @Transactional
  public boolean migrateDocumentById(List<String> profileIds) {
    String methodName = "migrateDocumentById";
    String correlationId = UUID.randomUUID().toString();
    String errorMessage;
    String profileCorrelationId = null;
    boolean status = false;
    try {
      for (String profileId : profileIds) {
        profileCorrelationId = profileId;
        MigrationResult migrationResult = couchbaseRepo.getProfileDocumentById(profileId);
        OneSupplierLogFacade.info(appName, UUID.fromString(profileId), correlationId, className, methodName,
          "Processing data insertion for profileId:" + profileId);
        if (migrationResult != null) {
          validateAndInsert(migrationResult, profileId, correlationId);
          couchbaseRepo.updateDocumentStatus(profileId);
          status = true;
        }
      }
    } catch (Exception ex) {
      errorMessage = "Failed to migrate document:" + profileCorrelationId + ", due to "
        + OneSupplierLogFacade.getCompleteTrace(ex);
      redisRepository.upsertFailedMigrationDocIdWithError(String.valueOf(profileCorrelationId), errorMessage);
      OneSupplierLogFacade.error(appName, UUID.fromString(profileCorrelationId), correlationId, className,
        methodName, errorMessage, ex);
    }
    return status;
  }

  public void migrateQueuedDocuments(String queuedId) {
    QueuedDocs queuedDocs = couchbaseRepo.getQueuedDocsById(queuedId);
    queuedDocs.getInQueueEvents().forEach(event ->
      insertProfileEvent(event, null, UUID.fromString(queuedId.toLowerCase().replace("_queue", "")),
        true));
  }

  @Transactional
  public void migrateQueuedDocuments() {
    int queuedDocsCount = couchbaseRepo.queueProfileCount().rowsAs(Integer.class).get(0);
    int limit = 50;
    for (int offset = 0; offset < queuedDocsCount; offset += limit) {
      List<QueuedDocs> queuedDocs = couchbaseRepo
        .getQueuedDocumentsByLimitAndOffset(limit, offset)
        .stream()
        .filter(events -> !events.getInQueueEvents().isEmpty() && events.getId().toLowerCase().contains("_queue"))
        .toList();

      queuedDocs.forEach(docs -> docs.getInQueueEvents().forEach(event ->
        insertProfileEvent(event, null, UUID.fromString(docs.getId().toLowerCase().replace("_queue", "")),
          true)));
    }
  }

  public void migrateDocuments() throws UnknownHostException {
    String methodName = "migrateDocuments";
    String correlationId = UUID.randomUUID().toString();
    OneSupplierLogFacade.info(appName, null, correlationId, className, methodName,
      methodName + "called to migrate data");
    FeatureFlag dbMigrationFeatureFlag = FeatureToggles.get(FeatureFlagConstants.DB_MIGRATION);
    OneSupplierLogFacade.info(appName, null, correlationId, className, methodName,
      "Flag value for dbMigrationFeatureFlag is: " + dbMigrationFeatureFlag.getProperties().get("script-center"));
    boolean isDbMigrationActive = dbMigrationFeatureFlag.isEnabled()
      && Boolean.parseBoolean(dbMigrationFeatureFlag.getProperties().get("script-center").get());
    OneSupplierLogFacade.info(appName, null, correlationId, className, methodName,
      "Flag value for db migration is: " + isDbMigrationActive);
    if (Boolean.parseBoolean(environment.getProperty("isMigrationScriptEnabled"))) {
      String hostName = InetAddress.getLocalHost().getHostName();
      OneSupplierLogFacade.info(appName, null, correlationId, className, methodName,
        methodName + "called to migrate data for host" + hostName);
      BatchParameter batchParameterDetails = batchProcessUtil.getBatchForDataMigrationPerPod(hostName);
      int limit = 100;
      int offset;
      OneSupplierLogFacade.info(appName, null, correlationId, className, methodName,
        "Start time for script center pod: " + hostName + " is " + LocalDateTime.now());
      int count = batchParameterDetails.getBatchUpperLimit();
      OneSupplierLogFacade.info(appName, null, correlationId, className, methodName,
        "Total records of 36 char length to migrate by script center pod" + hostName + " is " + count);
      AtomicInteger processed = new AtomicInteger();
      for (offset = batchParameterDetails.getOffset(); offset < count; offset += limit) {
        if (offset + limit > count) {
          limit = count - offset;
        }
        try {
          List<String> profileIds = couchbaseRepo.loadProfileIds(limit, offset);
          if (profileIds == null || profileIds.isEmpty()) {
            break;
          }
          migrateDocuments(profileIds, processed);
        } catch (Exception ex) {
          OneSupplierLogFacade.error(appName, null, correlationId, className, methodName,
            "error loading profile from Couchbase", ex);
        }
      }
      OneSupplierLogFacade.info(appName, null, correlationId, className, methodName,
        "Total profiles migrated by script center pod" + hostName + " is " + processed);
      OneSupplierLogFacade.info(appName, null, correlationId, className, methodName,
        "End time for script center pod:" + hostName + " is " + LocalDateTime.now());
    }
  }

  public void migrateDocuments(List<String> profileIds, AtomicInteger processed) {
    String errorMessage;
    String methodName = "migrateDocuments";
    String correlationId = "batch-";
    UUID profileId = null;
    for (String id : profileIds) {
      try {
        MigrationResult profile = couchbaseRepo.getProfileDocumentById(id);
        if (nonNull(profile.getBusinessProfile())
          && !"migrated-11".equals(profile.getBusinessProfile().getDocumentStatus())) {
          profileId = UUID.fromString(id);
          correlationId = "batch-" + UUID.randomUUID();
          validateAndInsert(profile, String.valueOf(UUID.fromString(profile.getId())),
            correlationId);
          OneSupplierLogFacade.info(appName, profileId, correlationId, className, methodName,
            "Successfully migrated Profile ID:" + profileId);
          processed.getAndIncrement();
        }
      } catch (Exception ex) {
        errorMessage = "Failed to migrate document:" + profileId
          + ", due to " + OneSupplierLogFacade.getCompleteTrace(ex);
        OneSupplierLogFacade.error(appName, profileId, correlationId, className, methodName, errorMessage, ex);
      }
    }
    OneSupplierLogFacade.info(appName, profileId, correlationId, className, methodName,
      "Total processed profiles: " + processed);
  }

  @Transactional
  public List<UUID> migrateDocumentsScheduler() {
    String errorMessage;
    String methodName = "migrateDocuments";
    String correlationId = "batch-" + UUID.randomUUID();
    QueryResult queryResult = couchbaseRepo.profileCount();
    int size = queryResult.rowsAs(Integer.class).get(0);
    int limit = 100;
    int offset = Integer.parseInt(redisRepository.getOffsetValue());
    UUID profileId = null;
    List<UUID> profileIds = new ArrayList<>();
    if (offset <= size) {
      try {
        List<MigrationResult> profileList = couchbaseRepo
          .getDocumentsByLimitAndOffset(limit, offset)
          .stream()
          .filter(profile -> !"migrated-11".equals(profile.getBusinessProfile().getDocumentStatus()))
          .toList();
        Set<MigrationResult> uniqueValues = new HashSet<>();
        if (!profileList.isEmpty()) {
          profileIds = profileList.stream().filter(uniqueValues::add)
            .map(result -> UUID.fromString(result.getId())).collect(Collectors.toList());
          offset += limit;
          for (MigrationResult migrationResult : profileList) {
            profileId = UUID.fromString(migrationResult.getId());
            validateAndInsert(migrationResult, String.valueOf(profileId), correlationId);
            OneSupplierLogFacade.info(appName, profileId, correlationId, className, methodName,
              "Successfully migrated Profile ID:" + profileId + " for offset:" + offset);
          }
        }
      } catch (Exception ex) {
        errorMessage = "Failed to migrate document:" + profileId
          + ", for Offset: " + offset + ", due to " + OneSupplierLogFacade.getCompleteTrace(ex);
        redisRepository.upsertFailedMigrationDocIdWithError(String.valueOf(profileId), errorMessage);
        OneSupplierLogFacade.error(appName, profileId, correlationId, className, methodName, errorMessage, ex);
      } finally {
        redisRepository.upsertOffsetValue(String.valueOf(offset));
      }
    }
    return profileIds;
  }

  @Transactional(rollbackFor = Exception.class)
  @Retryable(retryFor = {DataAccessException.class, PersistenceException.class, TransactionException.class,
    CannotCreateTransactionException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
  public void validateAndInsert(MigrationResult migrationResult, String id, String trackingCorrelationId) {
    String methodName = "validateAndInsert";
    UUID profileId = UUID.fromString(id);
    if (migrationResult != null && migrationResult.getBusinessProfile() != null
      && migrationResult.getBusinessProfile().getCompanyProfile() != null) {
      OneSupplierLogFacade.info(appName, profileId, trackingCorrelationId, className, methodName,
        "Processing data insertion for profileId:" + profileId);
      CompanyProfile companyProfile = migrationResult.getBusinessProfile().getCompanyProfile();
      CompanyProfileEvent companyProfileEvent = migrationResult.getBusinessProfile().getCompanyProfileEvent();
      List<CompanyId> companyIdList = companyProfile.getCompanyId();
      filterAndRemoveDuplicateTaxId(companyIdList);
      Map<String, CompanyDetails> companyDetailsMap = companyProfile.getCompanyDetails();
      List<CompanyId> correlatedCompanyIds = getCompanyIDListOfTypeProfileCorrelationId(companyIdList);
      String profileCreateTimestamp = companyProfile.getUpdateTime();
      insertDocuments(companyProfile, profileId, trackingCorrelationId, companyProfileEvent,
        companyDetailsMap, correlatedCompanyIds, profileCreateTimestamp);
      migratedDocumentValidationService.validateMigratedDocuments(profileId);
      couchbaseRepo.updateDocumentStatus(profileId.toString());
    } else {
      boolean isEventsNotEmpty = migrationResult != null && migrationResult.getBusinessProfile() != null
        && nonNull(migrationResult.getBusinessProfile().getCompanyProfileEvent())
        && !migrationResult.getBusinessProfile().getCompanyProfileEvent().getEvents().isEmpty();

      if (isEventsNotEmpty && migrationResult.getBusinessProfile().getCompanyProfileEvent().getEvents().size() > 1) {
        // (1) Get correlation ids from events
        Set<CompanyId> correlationCompanyIdSet =
          migrationResult.getBusinessProfile().getCompanyProfileEvent().getEvents().stream()
            .map(event -> event.getCompanyId().stream()
              .filter(companyId -> companyId.getType()
                .equals(PROFILE_CORRELATION_ID.getIdType()))
              .findFirst().orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        OneSupplierLogFacade.info(appName, profileId, trackingCorrelationId, className, methodName,
          "Encountered correlationIds with missing company_profile:" + correlationCompanyIdSet);
        // (2) Run profile restoration process
        correlationCompanyIdSet.forEach(this::restoreCompanyProfileSubdocIfMissing);
        migrateDocumentById(Collections.singletonList(profileId.toString()));
      } else if (isEventsNotEmpty
        && migrationResult.getBusinessProfile().getCompanyProfileEvent().getEvents().size() == 1) {
        OneSupplierLogFacade.warn(appName, profileId, trackingCorrelationId, className, methodName,
          "Company profile having event size 1 and is probably de-duped:" + profileId);
      } else {
        OneSupplierLogFacade.error(appName, profileId, trackingCorrelationId, className, methodName,
          "Company profile is empty for profileId:" + profileId);
      }
    }
  }

  /**
   * Checks if a given Alias document exists and that its associated Profile document does <em>not</em> have a
   * company_profile subdocument. This scenario indicates that this profile was incorrectly deduped at some point and
   * the company_profile erroneously deleted. This method will trigger the restoration of this corrupted profile.
   *
   * @param profileCorrelationId profileCorrelationId
   */
  protected void restoreCompanyProfileSubdocIfMissing(CompanyId profileCorrelationId) {
    CompanyAlias companyAlias = getAliasDocument(profileCorrelationId);
    if (nonNull(companyAlias)) {
      String profileId = companyAlias.getProfileId();
      if (!couchbaseRepo.exists(profileId, company_profile)) {
        restoreMissingCompanyProfileSubdoc(profileId);
      }
    }
  }

  public CompanyAlias getAliasDocument(final CompanyId companyId) {
    if (companyId != null && couchbaseRepo.exists(createAliasDocName(companyId))) {
      return getCompanyAlias(createAliasDocName(companyId), company_alias);
    }
    return null;
  }

  public CompanyAlias getCompanyAlias(String aliasDocName, CouchbaseDocumentType couchbaseDocumentType) {
    return CouchbasejsonUtils
      .convertJsonToJava(couchbaseRepo.get(aliasDocName, couchbaseDocumentType).toString(),
        CompanyAlias.class
      );
  }

  /**
   * Method that restores missing company_profile subdoc. The company_profile_event subdoc is retrieved for the
   * profileId parameter. Iterating through the event list, information is extracted from the events and used to
   * recreate company_profile. Finally the restored company_profile is upserted.
   *
   * @param profileId profileId
   * @return CompanyProfile
   */
  public CompanyProfile restoreMissingCompanyProfileSubdoc(String profileId) {
    OneSupplierLogFacade.info(appName, UUID.fromString(profileId), null, className,
      "restoreMissingCompanyProfileSubdoc",
      "Attempting to restore company_profile from event subdoc for" + profileId);
    JsonObject companyProfileEventJson = couchbaseRepo.get(profileId, CouchbaseDocumentType.company_profile_event);
    if (isNull(companyProfileEventJson)) {
      OneSupplierLogFacade.error(appName, UUID.fromString(profileId), null, className,
        "restoreMissingCompanyProfileSubdoc",
        "No company_profile_event found for" + profileId);
      return null;
    }

    JsonArray eventList = companyProfileEventJson.getArray("events");
    if (!isEmpty(eventList)) {
      // Initialize fields/objects needed for company_profile
      Map<String, CompanyDetails> companyDetailsMap = new HashMap<>();
      CounterpartyId counterpartyId = null;
      List<AcceptanceDetail> acceptance = new ArrayList<>();
      Demographics c360Demographics = null;
      List<CompanyId> companyIdList = new ArrayList<>();

      // Iterate through each event in event list and insert/update company_details map and set company_profile fields
      // using event info
      for (int index = 0; index < eventList.size(); index++) {
        JsonObject event = eventList.getObject(index);

        // Get the correlation id from company id list
        JsonArray companyIdJsonArray = event.getArray("company_id");
        if (isNull(companyIdJsonArray)) {
          OneSupplierLogFacade.warn(appName, UUID.fromString(profileId), null, className,
            "restoreMissingCompanyProfileSubdoc",
            "Attempting to restore company_profile - continuing to next event because unable to get"
              + "correlation id from  null company_id");
          continue;
        }

        List<CompanyId> companyIds = convertJsonArrayToList(companyIdJsonArray, CompanyId.class);

        ActorSystem actorSystem = ActorSystem
          .valueOf(event.getObject("envelope").getObject("actor").getString("system"));
        CompanySource source = ProfileCommonUtil.actorSystemToCompanySource(actorSystem);
        Optional<CompanyId> correlationIdOptional =
          findByIdAndSource(companyIds, PROFILE_CORRELATION_ID.getIdType(), source);

        // Initialize default company details to be updated with data from events
        CompanyDetails companyDetails = new CompanyDetails();
        companyDetails.setSchemaVersion(2);
        companyDetails.setCounterpartyCorrelationId(null);
        companyDetails.setOnboardingStatus(OnboardingStatus.CREATE_INITIATED);
        companyDetails.setDeleteStatus(false);
        companyDetails.setBusinessType(null);
        companyDetails.setContacts(null);
        companyDetails.setDemographics(null);
        companyDetails.setPaymentAccounts(null);
        companyDetails.setCreateTime(LocalDateTime.now().toString());

        if (correlationIdOptional.isPresent()) {
          String eventCorrelationId = correlationIdOptional.get().getValue();
          companyDetailsMap.putIfAbsent(eventCorrelationId, companyDetails);

          // Combine ids from previous event and dedupe
          companyIdList = Stream.concat(companyIdList.stream(), companyIds.stream())
            .distinct()
            .collect(Collectors.toList());

          // Get acceptance from event and add to acceptance for company_profile
          if (nonNull(event.getObject("acceptance"))
            && EventType.PROFILE_ACCEPTANCE_EVENT.getEventType().equals(event.getString("event_type"))) {
            Acceptance acceptanceFromEvent = CouchbasejsonUtils
              .convertJsonToJava(event.getObject("acceptance").toString(), Acceptance.class);
            acceptance.add(new AcceptanceDetail(
              nonNull(acceptanceFromEvent.getBuyBusId()) ? AcceptanceType.RELATIONSHIP : AcceptanceType.SUPPLIER,
              acceptanceFromEvent.getBuyBusId(),
              acceptanceFromEvent
            ));
          }

          // If present, get c360 demographics from event and populate for company_profile
          if (nonNull(event.getObject("c360demographics"))) {
            c360Demographics = CouchbasejsonUtils
              .convertJsonToJava(event.getObject("c360demographics").toString(), Demographics.class);
          }

          // Get correlation id from notification event and set counterparty_id for company_details in company_profile
          if (nonNull(event.getObject("counterparty_id"))) {
            List<CompanyId> counterpartyIdCompanyIdList = convertJsonArrayToList(
              event.getObject("counterparty_id").getArray("company_id"), CompanyId.class
            );
            Optional<CompanyId> counterpartyCorrelationId = findByIdAndSource(counterpartyIdCompanyIdList,
              PROFILE_CORRELATION_ID.getIdType(), source);
            counterpartyCorrelationId.ifPresent(companyId -> companyDetailsMap.get(eventCorrelationId)
              .setCounterpartyCorrelationId(companyId));
          }

          // Get payment accounts from event and update payment accounts in company details
          if (!EventType.PROFILE_NOTIFICATION_EVENT.getEventType().equals(event.getString("event_type"))
            && (nonNull(event.getArray("paymentAccounts")) || nonNull(event.getArray("payment_accounts")))) {
            JsonArray paymentAccounts = nonNull(event.getArray("paymentAccounts"))
              ? event.getArray("paymentAccounts")
              : event.getArray("payment_accounts");
            List<PaymentAccount> paymentAccountList = convertJsonArrayToList(paymentAccounts, PaymentAccount.class);
            companyDetailsMap.get(eventCorrelationId).setPaymentAccounts(paymentAccountList);
          }

          // Get business_type from event and update business_type in company details
          if (nonNull(event.getString("business_type"))) {
            companyDetailsMap.get(eventCorrelationId).
              setBusinessType(BusinessType.valueOf(event.getString("business_type")));
          }

          // Get contacts from event and update contacts in company details
          if (nonNull(event.getArray("contacts"))) {
            List<Contact> contacts = convertJsonArrayToList(event.getArray("contacts"), Contact.class);
            companyDetailsMap.get(eventCorrelationId).setContacts(contacts);
          }

          // Get demographics from event and update demographics in company details
          if (nonNull(event.getObject("demographics"))) {
            Demographics demographics = CouchbasejsonUtils
              .convertJsonToJava(event.getObject("demographics").toString(), Demographics.class);
            companyDetailsMap.get(eventCorrelationId).setDemographics(demographics);
          }
        }
      }

      // Upsert company_profile with populated fields (counterpartyId default to null)
      CompanyProfile companyProfile = new CompanyProfile();
      companyProfile.setCompanyId(companyIdList);
      companyProfile.setDemographics(c360Demographics);
      companyProfile.setAcceptanceDetails(acceptance.isEmpty() ? null : acceptance);
      companyProfile.setProfileId(companyProfileEventJson.getString("id"));
      companyProfile.setCouchbaseDocumentType(company_profile);
      companyProfile.setCompanyDetails(companyDetailsMap);
      upsertCompanyProfile(companyProfile.getProfileId(), companyProfile);
      return companyProfile;
    }
    OneSupplierLogFacade.error(appName, UUID.fromString(profileId), null, className,
      "restoreMissingCompanyProfileSubdoc",
      "No events stored in couchbase to restore company_profile with");
    return null;
  }

  public void upsertCompanyProfile(String profileId, CompanyProfile companyProfile) {
    companyProfile.setUpdateTime(LocalDateTime.now().toString());
    couchbaseRepo.upsert(profileId, company_profile,
      JsonObject.fromJson(CouchbasejsonUtils.convertJavaToJson(companyProfile)),
      10
    );
  }

  /**
   * Helper method for converting JsonArray to List with null values excluded
   *
   * @param jsonArray to be converted
   * @param cls       class of objects in the returned List
   * @return converted List
   */
  private <T> List<T> convertJsonArrayToList(JsonArray jsonArray, Class<T> cls) {
    List<T> convertedList = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      JsonObject ele = jsonArray.getObject(i);
      if (ele != null) {
        convertedList.add(CouchbasejsonUtils.convertJsonToJava(ele.toString(), cls));
      }
    }
    return convertedList;
  }

  public static void filterAndRemoveDuplicateTaxId(List<CompanyId> companyIdList) {
    List<CompanyId> taxIdList = companyIdList.stream().filter(id -> "tax_id".equalsIgnoreCase(id.getType())).toList();
    if (!taxIdList.isEmpty()) {
      CompanyId taxId = taxIdList.get(0);
      companyIdList.removeAll(taxIdList);
      companyIdList.add(taxId);
    }
  }

  public void insertDocuments(CompanyProfile companyProfile, UUID profileId, String trackingCorrelationId,
                              CompanyProfileEvent companyProfileEvent, Map<String, CompanyDetails> companyDetailsMap,
                              List<CompanyId> correlatedCompanyIds,
                              String profileCreateTimestamp) {

    /* For some documents, company IDs are not present in the companyId list inside company profile
     * but those IDs still have a map of company details. So if we don't store, profile_event table insertion
     * will fail since events contain these IDs (Foreign Key failures). */
    String methodName = "insertDocuments";
    if (nonNull(companyDetailsMap) && !companyDetailsMap.isEmpty() && !correlatedCompanyIds.isEmpty()) {
      if (correlatedCompanyIds.size() != companyDetailsMap.size() && nonNull(companyProfileEvent)) {
        OneSupplierLogFacade.info(appName, profileId, trackingCorrelationId, className, methodName,
          "Correlated company ids and company details not equal for :" + profileId + " trying to restore");
        //identify which correlated ids are not there in company details map
        Set<CompanyId> companyIdsNotInProfile =
          correlatedCompanyIds.stream().filter(
            companyId -> !companyDetailsMap.containsKey(companyId.getValue())).collect(Collectors.toSet());
        restoreDetailsForMissingCompanyIds(companyIdsNotInProfile, companyProfileEvent, companyDetailsMap, profileId,
          trackingCorrelationId);
        //remove those ids from correlated company ids
        correlatedCompanyIds = correlatedCompanyIds.stream().filter(
          id -> !companyDetailsMap.containsKey(id.getValue())).toList();
      }
      if (!companyDetailsMap.isEmpty() && !correlatedCompanyIds.isEmpty()) {
        Optional<Profile> profile = profileRepository.findByProfileId(profileId);
        List<UUID> alreadyMigratedProfileCorrelationIds = new ArrayList<>();
        if (profile.isEmpty()) {
          insertProfileAndDemographics(profileId, companyProfile, profileCreateTimestamp);
          insertProfileCompany(profileId, companyProfile);
        } else {
          alreadyMigratedProfileCorrelationIds = profile.get().getProfileCompanyDetail().stream().map(
            ProfileCompanyDetail::getProfileCompanyDetailId).toList();
        }
        List<UUID> finalAlreadyMigratedProfileCorrelationIds = alreadyMigratedProfileCorrelationIds;
        correlatedCompanyIds = correlatedCompanyIds.stream().filter(
          companyId -> !finalAlreadyMigratedProfileCorrelationIds.contains(
            UUID.fromString(companyId.getValue()))).toList();
        correlatedCompanyIds.forEach(companyId -> {
          migrateCompanyDetails(companyProfile, profileId, trackingCorrelationId, companyProfileEvent,
            companyDetailsMap, profileCreateTimestamp, companyId, methodName);
        });
        if (!finalAlreadyMigratedProfileCorrelationIds.isEmpty()) {
          //migrated events only if not already migrated
          List<Event> filteredEvents = companyProfileEvent.getEvents().stream().filter(
            event ->
              event.getCompanyId().stream().noneMatch(
                companyId -> companyId.getType().equals(PROFILE_CORRELATION_ID.getIdType())
                  && finalAlreadyMigratedProfileCorrelationIds.contains(UUID.fromString(companyId.getValue()))
              )).toList();
          companyProfileEvent.setEvents(filteredEvents);
        }
        insertEventDetails(profileId, companyProfileEvent, correlatedCompanyIds);
      } else {
        OneSupplierLogFacade.warn(appName, profileId, trackingCorrelationId, className, methodName,
          "Could not migrate document:" + profileId + " due to missing company details");
      }
    } else {
      OneSupplierLogFacade.warn(appName, profileId, trackingCorrelationId, className, methodName,
        "Could not migrate document:" + profileId + " due to missing company details");
    }
  }

  public void migrateCompanyDetails(CompanyProfile companyProfile, UUID profileId, String trackingCorrelationId,
                                    CompanyProfileEvent companyProfileEvent,
                                    Map<String, CompanyDetails> companyDetailsMap, String profileCreateTimestamp,
                                    CompanyId companyId, String methodName) {
    //check if company id was inserted or not, if not proceed, otherwise ignore
    insertProfileCompanyDemographicsAndPayment(profileId, companyProfileEvent, companyDetailsMap,
      profileCreateTimestamp, companyId);
    UUID profileCompanyDetailId = UUID.fromString(companyId.getValue());
    CompanyDetails companyDetails = companyDetailsMap.get(String.valueOf(profileCompanyDetailId));
    if (nonNull(companyDetails)) {
      if (companyDetails.getCounterpartyCorrelationId() != null) {
        CompanyId counterCompanyId = companyDetails.getCounterpartyCorrelationId();
        String counterPartyCorrelationId = counterCompanyId.getValue();
        String counterPartySourceName = String.valueOf(counterCompanyId.getSource());
        MigrationResult counterpartyMigrationResult = couchbaseRepo
          .getAliasDocument(counterPartyCorrelationId + counterPartySourceName);
        if (nonNull(counterpartyMigrationResult)) {
          CompanyAlias counterCompanyAlias = counterpartyMigrationResult.getBusinessProfile().getCompanyAlias();
          if (counterCompanyAlias != null) {
            insertProfileCounterPartyDocument(
              UUID.fromString(counterCompanyAlias.getProfileId()),
              UUID.fromString(counterPartyCorrelationId),
              profileCompanyDetailId,
              counterPartySourceName,
              trackingCorrelationId,
              companyProfileEvent
            );
          }
        }
      } else if (companyProfile.getCounterpartyIds() != null && !companyProfile.getCounterpartyIds().isEmpty()) {
        companyProfile.getCounterpartyIds()
          .forEach((key, value) -> value.forEach(counterPartyId -> {
            String counterPartySourceName = counterPartyId.substring(36);
            if (counterPartySourceName.isEmpty()) {
              counterPartySourceName = companyDetails.getCreateSource().getSourceName();
            }
            MigrationResult counterpartyMigrationResult = couchbaseRepo.getAliasDocument(counterPartyId);
            if (counterpartyMigrationResult != null) {
              CompanyAlias counterCompanyAlias = counterpartyMigrationResult.getBusinessProfile()
                .getCompanyAlias();
              if (counterCompanyAlias != null) {
                insertProfileCounterPartyDocument(
                  UUID.fromString(counterCompanyAlias.getProfileId()),
                  UUID.fromString(counterCompanyAlias.getProfileCorrelationId()),
                  UUID.fromString(key.substring(0, 36)),
                  counterPartySourceName,
                  trackingCorrelationId,
                  companyProfileEvent
                );
              }
            }
          }));
      }
    } else {
      OneSupplierLogFacade.warn(appName, profileId, trackingCorrelationId, className, methodName,
        "Could not migrate company details: " + trackingCorrelationId + "of document: " + profileId
          + " due to missing company details");
    }
  }

  private void restoreDetailsForMissingCompanyIds(Set<CompanyId> companyIdsNotInProfile,
                                                  CompanyProfileEvent companyProfileEvent,
                                                  Map<String, CompanyDetails> companyDetailsMap, UUID profileId,
                                                  String migrationTrackingId) {
    String methodName = "restoreDetailsForMissingCompanyIds";
    companyIdsNotInProfile.forEach(companyId -> {
      MigrationResult migrationResult = couchbaseRepo.getAliasDocument(companyId.getValue() + companyId.getSource());
      if (nonNull(migrationResult)
        && nonNull(migrationResult.getBusinessProfile())
        && nonNull(migrationResult.getBusinessProfile().getCompanyAlias())
        && Objects.equals(profileId.toString(), migrationResult.getBusinessProfile().getCompanyAlias().getProfileId())
      ) {
        CompanyDetails companyDetails = new CompanyDetails();
        List<Event> eventList = companyProfileEvent.getEvents().stream()
          .filter(event -> event.getCompanyId().stream()
            .allMatch(companyId1 -> companyId1.getValue().equals(companyId.getValue())))
          .toList();
        if (!eventList.isEmpty()) {
          Event lastEvent = org.springframework.util.CollectionUtils.lastElement(eventList);
          if (nonNull(lastEvent)) {
            BeanUtils.copyProperties(lastEvent, companyDetails);
          }
          List<PaymentAccount> paymentAccounts = eventList.stream()
            .map(Event::getPaymentAccounts)
            .flatMap(Collection::stream)
            .toList();
          companyDetails.setPaymentAccounts(paymentAccounts);
          OneSupplierLogFacade.info(appName, profileId, migrationTrackingId, className, methodName,
            "Successfully restored company details for :" + profileId + ", correlation id: "
              + companyId.getValue() + " trying to restore");
        }
        companyDetailsMap.put(companyId.getValue(), companyDetails);
      }
    });
  }

  /**
   * Inserts new rows for a counterparty document, including a relationship row with its counter/primary document
   *
   * @param counterpartyProfileId            The counterparty's profile id
   * @param counterpartyProfileCorrelationId The counterparty profile correlation ID in question, if there is one
   * @param primaryProfileCorrelationId      The other document's (i.e. primary) profile correlation ID
   * @param counterPartySourceName           The source name for the counterparty
   * @param correlationId                    A correlation ID for the purposes of logging (e.g. batch-xx)
   */
  private void insertProfileCounterPartyDocument(
    UUID counterpartyProfileId,
    UUID counterpartyProfileCorrelationId,
    UUID primaryProfileCorrelationId,
    String counterPartySourceName,
    String correlationId,
    CompanyProfileEvent primaryPartyEvent
  ) {
    String methodName = "insertProfileCounterPartyDocument";
    MigrationResult migrationResult = couchbaseRepo.getProfileDocumentById(String.valueOf(counterpartyProfileId));
    if (nonNull(migrationResult)
      && nonNull(migrationResult.getBusinessProfile())
      && nonNull(migrationResult.getBusinessProfile().getCompanyProfile())
    ) {
      CompanyProfile companyProfile = migrationResult.getBusinessProfile().getCompanyProfile();
      CompanyProfileEvent companyProfileEvent = migrationResult.getBusinessProfile().getCompanyProfileEvent();
      Map<String, CompanyDetails> companyDetailsMap = companyProfile.getCompanyDetails();
      Optional<Profile> profileOptional = profileRepository.findByProfileId(counterpartyProfileId);

      if (profileOptional.isEmpty()) {
        OneSupplierLogFacade.info(appName, counterpartyProfileId, correlationId, className, methodName,
          "Processing data insertion for counterProfileId:" + counterpartyProfileId);
        if (!companyDetailsMap.isEmpty() && nonNull(
          companyDetailsMap.get(counterpartyProfileCorrelationId.toString()))) {
          insertNewCounterpartyIntoSQLDatabase(counterpartyProfileId, counterpartyProfileCorrelationId,
            primaryProfileCorrelationId, counterPartySourceName, companyProfileEvent,
            companyDetailsMap, companyProfile);
          insertProfileCompany(counterpartyProfileId, companyProfile);
        } else { //missing counterparty CompanyDetails, attempt to restore
          restoreCounterPartyCompanyDetails(counterpartyProfileId,
            counterpartyProfileCorrelationId,
            primaryProfileCorrelationId, counterPartySourceName,
            correlationId,
            companyProfile,
            companyProfileEvent,
            companyDetailsMap,
            false,
            true,
            primaryPartyEvent
          );
        }
      } else {
        OneSupplierLogFacade.info(appName, counterpartyProfileId, correlationId, className, methodName,
          "Processing data insertion for existing counterProfileId:" + counterpartyProfileId);
        Set<ProfileCompanyDetail> profileCompanyDetails = profileOptional.get().getProfileCompanyDetail();
        boolean isCounterPartyCompanyExists = false;
        if (nonNull(profileCompanyDetails) && !profileCompanyDetails.isEmpty()) {
          isCounterPartyCompanyExists = profileCompanyDetails.stream().anyMatch(
            profileCompanyDetail -> profileCompanyDetail.getProfileCompanyDetailId().equals(
              counterpartyProfileCorrelationId));
        }
        if (companyDetailsMap.isEmpty() || isNull(
          companyDetailsMap.get(counterpartyProfileCorrelationId.toString()))) {
          restoreCounterPartyCompanyDetails(counterpartyProfileId,
            counterpartyProfileCorrelationId,
            primaryProfileCorrelationId,
            counterPartySourceName,
            correlationId,
            companyProfile,
            companyProfileEvent,
            companyDetailsMap,
            true,
            isCounterPartyCompanyExists,
            primaryPartyEvent
          );
        } else {
          updateExistingCounterpartyInSQLDatabase(
            counterpartyProfileCorrelationId,
            primaryProfileCorrelationId,
            counterPartySourceName,
            companyProfile,
            counterpartyProfileId,
            companyProfileEvent,
            companyDetailsMap,
            isCounterPartyCompanyExists
          );
        }
      }
    }
  }

  private void restoreCounterPartyCompanyDetails(UUID counterpartyProfileId,
                                                 UUID counterpartyProfileCorrelationId,
                                                 UUID primaryProfileCorrelationId,
                                                 String counterPartySourceName,
                                                 String correlationId,
                                                 CompanyProfile companyProfile,
                                                 CompanyProfileEvent companyProfileEvent,
                                                 Map<String, CompanyDetails> companyDetailsMap,
                                                 boolean isUpdateFlow,
                                                 boolean isCounterPartyCompanyExists,
                                                 CompanyProfileEvent primaryPartyEvent
  ) {
    String methodName = "insertProfileCounterPartyDocument";
    Map<String, Set<String>> primaryPartyIdMap = companyProfile.getCounterpartyIds();
    if (nonNull(primaryPartyIdMap)
      && primaryPartyIdMap.containsKey(counterpartyProfileCorrelationId.toString())) {
      Set<String> primaryPartyIdSet =
        primaryPartyIdMap.get(counterpartyProfileCorrelationId.toString());
      Optional<CounterpartyDetails> counterpartyDetails =
        getCounterpartyDetails(primaryPartyIdSet, counterpartyProfileCorrelationId);
      if (counterpartyDetails.isPresent()) {
        restoreCounterPartyDetailsAndStoreInRelationalDb(
          counterpartyProfileId,
          counterpartyProfileCorrelationId,
          primaryProfileCorrelationId, counterPartySourceName,
          correlationId,
          companyProfile,
          companyProfileEvent,
          companyDetailsMap,
          isUpdateFlow,
          isCounterPartyCompanyExists,
          counterpartyDetails);
      } else {
        OneSupplierLogFacade.warn(appName, counterpartyProfileId, correlationId, className, methodName,
          "Could not migrate counterparty document:" + counterpartyProfileId
            + " due to missing company details AND no counterparty details present in primary party events");
      }
    } else if (nonNull(primaryPartyEvent) && nonNull(primaryPartyEvent.getEvents())
      && !primaryPartyEvent.getEvents().isEmpty()) {
      List<Event> filteredEvents = primaryPartyEvent.getEvents().stream()
        .filter(event -> event.getCounterpartyDetails() != null)
        .filter(event -> event.getCounterpartyDetails().getProfileCorrelationId().isPresent())
        .filter(
          event ->
            event.getCounterpartyDetails()
              .getProfileCorrelationId().get().getValue().equals(counterpartyProfileCorrelationId.toString())
        )
        .toList();
      Optional<CounterpartyDetails> counterpartyDetails = generateCounterPartyDetailsUsingEvents(filteredEvents);
      if (counterpartyDetails.isPresent()) {
        restoreCounterPartyDetailsAndStoreInRelationalDb(
          counterpartyProfileId,
          counterpartyProfileCorrelationId,
          primaryProfileCorrelationId, counterPartySourceName,
          correlationId,
          companyProfile,
          companyProfileEvent,
          companyDetailsMap,
          isUpdateFlow,
          isCounterPartyCompanyExists,
          counterpartyDetails);
      } else {
        OneSupplierLogFacade.warn(appName, counterpartyProfileId, correlationId, className, methodName,
          "Could not migrate counterparty document:" + counterpartyProfileId
            + " due to missing company details AND no counterparty details present in primary party events");
      }
    } else {
      OneSupplierLogFacade.warn(appName, counterpartyProfileId, correlationId, className, methodName,
        "Could not migrate counterparty document:" + counterpartyProfileId
          + " due to missing company details");
    }
  }

  private void restoreCounterPartyDetailsAndStoreInRelationalDb(UUID counterpartyProfileId,
                                                                UUID counterpartyProfileCorrelationId,
                                                                UUID primaryProfileCorrelationId,
                                                                String counterPartySourceName,
                                                                String correlationId,
                                                                CompanyProfile companyProfile,
                                                                CompanyProfileEvent companyProfileEvent,
                                                                Map<String, CompanyDetails> companyDetailsMap,
                                                                boolean isUpdateFlow,
                                                                boolean isCounterPartyCompanyExists,
                                                                Optional<CounterpartyDetails> counterpartyDetails) {
    String methodName = "restoreCounterPartyDetailsAndStoreInRelationalDb";
    OneSupplierLogFacade.warn(appName, counterpartyProfileId, correlationId, className, methodName,
      "Restoring company details for counterparty document:" + counterpartyProfileId);
    CompanyDetails reconstructedCounterpartyCompanyDetails = new CompanyDetails();
    reconstructCounterpartyCompanyDetails(reconstructedCounterpartyCompanyDetails, counterpartyDetails.get(),
      companyProfile, counterpartyProfileCorrelationId);
    if (isUpdateFlow) {
      updateExistingCounterpartyInSQLDatabase(
        counterpartyProfileCorrelationId,
        primaryProfileCorrelationId,
        counterPartySourceName,
        companyProfile,
        counterpartyProfileId,
        companyProfileEvent,
        companyDetailsMap,
        isCounterPartyCompanyExists
      );
    } else {
      insertNewCounterpartyIntoSQLDatabase(counterpartyProfileId, counterpartyProfileCorrelationId,
        primaryProfileCorrelationId, counterPartySourceName, companyProfileEvent,
        companyDetailsMap, companyProfile);
    }
  }

  private void insertProfileCompany(UUID counterpartyProfileId, CompanyProfile companyProfile) {
    List<CompanyId> companyIdsList = getCompanyIDListOfNotProfileCorrelationIdType(companyProfile.getCompanyId());
    companyIdsList.forEach(companyId -> {
      if (isProfileCompanyIdValid(companyId)) {
        insertProfileCompany(companyId, counterpartyProfileId, null, companyProfile.getUpdateTime());
      }
    });
  }

  private void insertNewCounterpartyIntoSQLDatabase(
    UUID counterpartyProfileId,
    UUID counterpartyProfileCorrelationId,
    UUID primaryProfileCorrelationId,
    String counterPartySourceName,
    CompanyProfileEvent companyProfileEvent,
    Map<String, CompanyDetails> companyDetailsMap,
    CompanyProfile companyProfile
  ) {
    String profileCreateTimestamp = companyProfile.getUpdateTime();
    List<CompanyId> companyIdList = companyProfile.getCompanyId();
    filterAndRemoveDuplicateTaxId(companyIdList);
    List<CompanyId> correlatedCompanyIds = getCompanyIDListOfTypeProfileCorrelationId(companyIdList);


    insertProfileAndDemographics(counterpartyProfileId, companyProfile, profileCreateTimestamp);
    insertProfileCompanyDemographicsAndPayment(
      counterpartyProfileId,
      companyProfileEvent,
      companyDetailsMap,
      profileCreateTimestamp,
      correlatedCompanyIds.stream().filter(i ->
        i.getValue().equals(counterpartyProfileCorrelationId.toString())
      ).findFirst().orElse(new CompanyId(
        PROFILE_CORRELATION_ID.getIdType(),
        counterpartyProfileCorrelationId.toString(),
        CompanySource.valueOf(counterPartySourceName),
        true
      )));
    insertProfileRelationship(
      primaryProfileCorrelationId,
      counterpartyProfileCorrelationId,
      counterPartySourceName,
      profileCreateTimestamp
    );
    List<Event> filteredEvents = companyProfileEvent.getEvents().stream().filter(
      event ->
        event.getCompanyId().stream().anyMatch(
          companyId -> companyId.getValue().equals(counterpartyProfileCorrelationId.toString()))
    ).toList();
    companyProfileEvent.setEvents(filteredEvents);
    insertEventDetails(counterpartyProfileId, companyProfileEvent,
      correlatedCompanyIds);
  }

  private void updateExistingCounterpartyInSQLDatabase(
    UUID counterpartyProfileCorrelationId,
    UUID primaryProfileCorrelationId,
    String counterPartySourceName,
    CompanyProfile companyProfile,
    UUID counterpartyProfileId,
    CompanyProfileEvent companyProfileEvent,
    Map<String, CompanyDetails> companyDetailsMap,
    boolean isCounterPartyCompanyExists
  ) {
    String profileCreateTimestamp = companyProfile.getUpdateTime();
    List<CompanyId> companyIdList = companyProfile.getCompanyId();
    filterAndRemoveDuplicateTaxId(companyIdList);
    List<CompanyId> correlatedCompanyIds = getCompanyIDListOfTypeProfileCorrelationId(companyIdList);
    if (!isCounterPartyCompanyExists) {
      insertProfileCompanyDemographicsAndPayment(
        counterpartyProfileId,
        companyProfileEvent,
        companyDetailsMap,
        profileCreateTimestamp,
        correlatedCompanyIds.stream().filter(i ->
          i.getValue().equals(counterpartyProfileCorrelationId.toString())
        ).findFirst().orElse(new CompanyId(
          PROFILE_CORRELATION_ID.getIdType(),
          counterpartyProfileCorrelationId.toString(),
          CompanySource.valueOf(counterPartySourceName),
          true
        )));
    }
    insertProfileRelationship(
      primaryProfileCorrelationId,
      counterpartyProfileCorrelationId,
      counterPartySourceName,
      profileCreateTimestamp
    );
    List<Event> filteredEvents = companyProfileEvent.getEvents().stream().filter(
      event ->
        event.getCompanyId().stream().allMatch(
          companyId -> companyId.getValue().equals(counterpartyProfileCorrelationId.toString()))
    ).toList();
    companyProfileEvent.setEvents(filteredEvents);
    insertEventDetails(counterpartyProfileId, companyProfileEvent,
      correlatedCompanyIds);
  }

  private Optional<CounterpartyDetails> getCounterpartyDetails(
    Set<String> primaryPartyIds,
    UUID counterpartyProfileCorrelationId
  ) {
    List<Event> eventList =
      primaryPartyIds.stream()
        .map(id -> couchbaseRepo.getAliasDocument(id))
        .map(aliasDocMigrationResult -> aliasDocMigrationResult.getBusinessProfile().getCompanyAlias())
        .map(CompanyAlias::getProfileId)
        .map(profileId -> couchbaseRepo.getProfileDocumentById(profileId))
        .map(
          primaryPartyProfileMigrationResult ->
            primaryPartyProfileMigrationResult.getBusinessProfile().getCompanyProfileEvent().getEvents()
        )
        .flatMap(Collection::stream)
        .filter(event -> event.getCounterpartyDetails() != null)
        .filter(event -> event.getCounterpartyDetails().getProfileCorrelationId().isPresent())
        .filter(
          event ->
            event.getCounterpartyDetails()
              .getProfileCorrelationId().get().getValue().equals(counterpartyProfileCorrelationId.toString())
        )
        .toList();
    return generateCounterPartyDetailsUsingEvents(eventList);
  }

  private static Optional<CounterpartyDetails> generateCounterPartyDetailsUsingEvents(List<Event> eventList) {
    if (!eventList.isEmpty()) {
      CounterpartyDetails counterpartyDetails = eventList.get(0).getCounterpartyDetails();
      Set<PaymentAccount> paymentAccounts = eventList.stream()
        .map(Event::getCounterpartyDetails)
        .filter(Objects::nonNull)
        .map(CounterpartyDetails::getPaymentAccounts)
        .filter(Objects::nonNull)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
      if (counterpartyDetails != null && !paymentAccounts.isEmpty()) {
        counterpartyDetails.setPaymentAccounts(paymentAccounts.stream().toList());
        return Optional.of(counterpartyDetails);
      }
    }
    return Optional.empty();
  }

  private void reconstructCounterpartyCompanyDetails(
    CompanyDetails companyDetails,
    CounterpartyDetails counterpartyDetails,
    CompanyProfile companyProfile,
    UUID counterpartyProfileCorrelationId
  ) {
    companyDetails.setDemographics(counterpartyDetails.getDemographics());
    companyDetails.setContacts(counterpartyDetails.getContacts());
    companyDetails.setBusinessType(counterpartyDetails.getBusinessType());
    companyDetails.setPaymentAccounts(counterpartyDetails.getPaymentAccounts());
    companyProfile.getCompanyDetails().put(counterpartyProfileCorrelationId.toString(), companyDetails);
  }

  private void insertProfileAndDemographics(UUID profileId, CompanyProfile companyProfile,
                                            String profileCreateTimestamp) {
    insertProfile(companyProfile, profileId, profileCreateTimestamp);
    if (companyProfile.getDemographics() != null) {
      insertProfileDemographics(companyProfile.getDemographics().getLocation(), profileId, null, null,
        companyProfile.getDemographics().getLocation().getSource() != null
          ? companyProfile.getDemographics().getLocation().getSource() : CompanySource.C360.getSourceName(),
        getBusinessName(companyProfile.getDemographics()),
        getAdbaName(companyProfile.getDemographics()), profileCreateTimestamp,
        companyProfile.getDemographics().getSupplierCurrency());
      if (companyProfile.getDemographics().getCompanyContact() != null) {
        CompanyContact companyContact = companyProfile.getDemographics().getCompanyContact();
        insertDirectContact(companyContact, profileId, null, "CPG", profileCreateTimestamp);
        insertGenericContact(companyContact, profileId, null, "CPG", profileCreateTimestamp);
      }
    }
  }

  private void insertProfileCompanyDemographicsAndPayment(UUID profileId, CompanyProfileEvent companyProfileEvent,
                                                          Map<String, CompanyDetails> companyDetailsMap,
                                                          String profileCreateTimestamp, CompanyId companyId) {
    UUID profileCompanyDetailId = UUID.fromString(companyId.getValue());
    CompanyDetails companyDetails = companyDetailsMap.get(String.valueOf(profileCompanyDetailId));
    if (nonNull(companyDetails)) {
      String createTimestamp = companyDetails.getCreateTime();
      String sourceName = getSourceName(companyId, companyDetails);

      insertProfileCompanyDetail(companyDetails, profileId, profileCompanyDetailId, sourceName, createTimestamp);
      insertProfileCompany(companyId, profileId, profileCompanyDetailId, profileCreateTimestamp);
      if (CollectionUtils.isNotEmpty(companyDetails.getPaymentAccounts())) {
        insertProfilePaymentAccounts(companyDetails.getPaymentAccounts(), profileId,
          profileCompanyDetailId, sourceName, createTimestamp);
      }
      if (companyDetails.getDemographics() != null) {
        insertProfileDemographics(companyDetails.getDemographics().getLocation(), profileId,
          profileCompanyDetailId, null, sourceName,
          getBusinessName(companyDetails.getDemographics()),
          getAdbaName(companyDetails.getDemographics()), createTimestamp,
          companyDetails.getDemographics().getSupplierCurrency());
      }
      if (CollectionUtils.isNotEmpty(companyDetails.getContacts())) {
        companyDetails.getContacts().forEach(contact -> insertProfileContact(contact, profileId, profileCompanyDetailId,
          sourceName, createTimestamp));
      }
      if (companyDetails.getDemographics() != null
        && companyDetails.getDemographics().getCompanyContact() != null) {
        CompanyContact companyContact = companyDetails.getDemographics().getCompanyContact();
        insertDirectContact(companyContact, profileId, profileCompanyDetailId, sourceName, createTimestamp);
        insertGenericContact(companyContact, profileId, profileCompanyDetailId, sourceName, createTimestamp);
      }
      insertProfileAcceptance(companyProfileEvent, profileCompanyDetailId, sourceName);
    }
  }

  private String getBusinessName(Demographics demographics) {
    return demographics.getBusinessName() != null ? demographics.getBusinessName().getName() : null;
  }

  private String getAdbaName(Demographics demographics) {
    return demographics.getBusinessName() != null && demographics.getBusinessName().getAdba() != null
      ? CouchbasejsonUtils.convertJavaToJson(demographics.getBusinessName().getAdba()) : null;
  }

  public void insertProfile(CompanyProfile companyProfile, UUID profileId, String createTimestamp) {
    Profile profile = new Profile();
    profile.setProfileId(profileId);
    profile.setCreateTimestamp(LocalDateTime.parse(createTimestamp));
    profile.setLastUpdateTimestamp(LocalDateTime.now());
    if (companyProfile != null && companyProfile.getDemographics() != null
      && companyProfile.getDemographics().getLocation() != null) {
      if (companyProfile.getDemographics().getLocation().getConfidenceCode() != null) {
        profile.setConfidenceCode(companyProfile.getDemographics().getLocation().getConfidenceCode());
      }
      if (companyProfile.getDemographics().getLocation().getConfidenceSource() != null) {
        profile.setConfidenceSourceTx(companyProfile.getDemographics().getLocation()
          .getConfidenceSource().getSourceName());
      }
    }
    profileRepository.save(profile);
  }

  //TODO: pass this
  private void insertProfileCompany(CompanyId companyId, UUID profileId, UUID profileCompanyDetailId,
                                    String createTimestamp) {
    String value = companyId.getValue();
    if (companyId.getType().equals(CM15_TYPE) || companyId.getType().equals(CM11_TYPE)) {
      value = new EncryptDecryptUtil(environment).getEncryptedText(companyId.getValue());
    }
    ProfileCompany profileCompany = new ProfileCompany();
    profileCompany.setProfileCompanyId(UUID.randomUUID());
    profileCompany.setProfileId(profileId);
    profileCompany.setProfileCompanyDetailId(profileCompanyDetailId);
    profileCompany.setUniqueFlagIndicator(companyId.isFlag());
    profileCompany.setSourceCode(companyId.getSource());
    profileCompany.setTypeText(companyId.getType());
    profileCompany.setValueText(value);
    profileCompany.setCreateBySourceName(companyId.getSource().getSourceName());
    profileCompany.setLastUpdateBySourceName(companyId.getSource().getSourceName());
    profileCompany.setCreateTimestamp(LocalDateTime.parse(createTimestamp));
    profileCompany.setLastUpdateTimestamp(LocalDateTime.now());
    profileCompanyRepository.save(profileCompany);
  }

  private void insertProfileAcceptance(CompanyProfileEvent companyProfileEvent, UUID profileCompanyDetailId,
                                       String sourceName) {
    String acceptanceFromEvent = null;
    if (companyProfileEvent != null && CollectionUtils.isNotEmpty(companyProfileEvent.getEvents())) {
      for (Event event : companyProfileEvent.getEvents()) {
        if (event.getEventType().equals(EventType.PROFILE_ACCEPTANCE_EVENT)) {
          acceptanceFromEvent = CouchbasejsonUtils.convertJavaToJson(event);
        }
      }
      ProfileAcceptance profileAcceptance = new ProfileAcceptance();
      profileAcceptance.setProfileAcceptanceId(UUID.randomUUID());
      profileAcceptance.setProfileCompanyDetailId(profileCompanyDetailId);
      profileAcceptance.setAcceptanceJsonData(acceptanceFromEvent);
      profileAcceptance.setCreateBySourceName(sourceName);
      profileAcceptance.setLastUpdateBySourceName(sourceName);
      profileAcceptance.setCreateTimestamp(LocalDateTime.parse(companyProfileEvent.getUpdateTime()));
      profileAcceptance.setLastUpdateTimestamp(LocalDateTime.now());
      profileAcceptanceRepository.save(profileAcceptance);
    }
  }

  private void insertProfileCompanyDetail(CompanyDetails companyDetails, UUID profileId, UUID profileCompanyDetailId,
                                          String sourceName, String createTimestamp) {
    ProfileCompanyDetail profileCompanyDetail = new ProfileCompanyDetail();
    profileCompanyDetail.setVersionNumber(1);
    profileCompanyDetail.setCreateSourceText(sourceName);
    profileCompanyDetail.setProfileId(profileId);
    profileCompanyDetail.setProfileCompanyDetailId(profileCompanyDetailId);
    if (companyDetails != null) {
      profileCompanyDetail.setOnboardingStatusCode(companyDetails.getOnboardingStatusCode() != null
        ? companyDetails.getOnboardingStatusCode() : null);
      profileCompanyDetail.setOnboardingStatusDescription(companyDetails.getOnboardingStatus() != null
        ? companyDetails.getOnboardingStatus().getOnboardingStatus() : null);
      profileCompanyDetail.setDeleteStatusIndicator(companyDetails.isDeleteStatus());
      profileCompanyDetail.setBusinessTypeText(companyDetails.getBusinessType() != null
        ? companyDetails.getBusinessType().getValue() : null);
      profileCompanyDetail.setCreateTimestamp(companyDetails.getCreateTime() != null
        ? LocalDateTime.parse(companyDetails.getCreateTime()) : LocalDateTime.now());
    }
    profileCompanyDetail.setCreateBySourceName(sourceName);
    profileCompanyDetail.setLastUpdateBySourceName(sourceName);
    profileCompanyDetail.setCreateTimestamp(createTimestamp != null
      ? LocalDateTime.parse(createTimestamp) : LocalDateTime.now());
    profileCompanyDetail.setLastUpdateTimestamp(LocalDateTime.now());

    profileCompanyDetailRepository.save(profileCompanyDetail);
  }

  private void insertEventDetails(UUID profileId, CompanyProfileEvent companyProfileEvent,
                                  List<CompanyId> correlatedCompanyIds) {
    if (CollectionUtils.isNotEmpty(companyProfileEvent.getEvents())) {
      companyProfileEvent.getEvents().forEach(event -> {
        List<CompanyId> profileCompanyDetailId = getCompanyIDListOfTypeProfileCorrelationId(event.getCompanyId());
        List<CompanyId> matchedCompanyIds = profileCompanyDetailId.stream().filter(profileCompanyId ->
            correlatedCompanyIds.stream().anyMatch(correlationCompanyId ->
              correlationCompanyId.getValue().equals(profileCompanyId.getValue())))
          .toList();
        if (!matchedCompanyIds.isEmpty()) {
          insertProfileEvent(event, profileId, UUID.fromString(profileCompanyDetailId.get(0).getValue()), false);
        }
      });
    }
  }

  private boolean isProfileCompanyIdValid(CompanyId companyId) {
    return companyId.getValue() != null;
  }

  private void insertProfilePaymentAccounts(List<PaymentAccount> paymentAccounts, UUID profileId,
                                            UUID profileCompanyDetailId, String sourceName, String createTimestamp) {
    paymentAccounts.forEach(paymentAccount -> {
      if (paymentAccount.getAction() != Action.DELETE) {
        ProfilePaymentAccount profilePaymentAccount = new ProfilePaymentAccount();
        profilePaymentAccount.setProfilePaymentAccountId(UUID.randomUUID());
        profilePaymentAccount.setProfileCompanyDetailId(profileCompanyDetailId);
        profilePaymentAccount.setReferenceId(paymentAccount.getProfilePaymentReferenceId());
        if (paymentAccount.getVerificationDetail() != null) {
          profilePaymentAccount.setVerificationDetailJsonData(
            CouchbasejsonUtils.convertJavaToJson(paymentAccount.getVerificationDetail()));
          profilePaymentAccount.setVerificationDetailCaseId(paymentAccount.getVerificationDetail().getCaseIdentifier());
          profilePaymentAccount.setVerificationDetailRecommendationText(
            paymentAccount.getVerificationDetail().getRecommendation().getRecommendation());
        }
        if (paymentAccount.getVerificationMeta() != null) {
          profilePaymentAccount.setIpAddressExtensionText(paymentAccount.getVerificationMeta().getIpAddress());
          profilePaymentAccount.setTransactionId(paymentAccount.getVerificationMeta().getTransactionId());
          profilePaymentAccount.setUploadTypeText(paymentAccount.getVerificationMeta().getUploadedType());
          profilePaymentAccount.setUploadedByName(paymentAccount.getVerificationMeta().getUploadedBy());
        }
        if (paymentAccount.getPaymentInstrument() != null) {
          profilePaymentAccount.setPaymentInstrumentId(paymentAccount.getPaymentInstrument().getPaymentInstrumentId());
          profilePaymentAccount.setPaymentInstrumentSourceName(paymentAccount.getPaymentInstrument().getSource()
            .getType());
        }
        profilePaymentAccount.setPaymentAccountName(paymentAccount.getName());
        if (paymentAccount.getPaymentdetails() != null) {
          profilePaymentAccount.setPaymentRoutingNumber(paymentAccount.getPaymentdetails().getRoutingNumber());
          profilePaymentAccount.setPaymentAccountNumber(paymentAccount.getPaymentdetails().getAccountNumber());
          profilePaymentAccount.setBankName(paymentAccount.getPaymentdetails().getBankName());
          profilePaymentAccount.setBankNickName(paymentAccount.getPaymentdetails().getBankNickname());
          profilePaymentAccount.setNmOnBankAccountText(paymentAccount.getPaymentdetails().getNameOnBankAccount());
          if (paymentAccount.getPaymentdetails().getBankAccountType() != null) {
            profilePaymentAccount.setAccountTypeText(
              paymentAccount.getPaymentdetails().getBankAccountType().getValue());
          }
          profilePaymentAccount.setIbanNumber(paymentAccount.getPaymentdetails().getInternationalBankAccountNumber());
          profilePaymentAccount.setSortCode(paymentAccount.getPaymentdetails().getSortCode());
          profilePaymentAccount.setSwiftAccountNumber(paymentAccount.getPaymentdetails().getSwiftAccount());
        }
        if (paymentAccount.getAction() != null) {
          profilePaymentAccount.setLogicActionText(paymentAccount.getAction().getAction());
        }
        profilePaymentAccount.setPaymentMethodCode(paymentAccount.getPaymentMethod() != null
          ? paymentAccount.getPaymentMethod().getValue() : null);
        profilePaymentAccount.setDuplicateIndicator(paymentAccount.isDuplicate());
        profilePaymentAccount.setCreateBySourceName(sourceName);
        profilePaymentAccount.setLastUpdateBySourceName(sourceName);
        profilePaymentAccount.setCreateTimestamp(createTimestamp != null
          ? LocalDateTime.parse(createTimestamp) : LocalDateTime.now());
        profilePaymentAccount.setLastUpdateTimestamp(LocalDateTime.now());
        profilePaymentAccountRepository.save(profilePaymentAccount);
        if (paymentAccount.getPaymentdetails() != null && paymentAccount.getPaymentdetails().getLocation() != null) {
          insertProfileDemographics(paymentAccount.getPaymentdetails().getLocation(), profileId, profileCompanyDetailId,
            profilePaymentAccount.getProfilePaymentAccountId(), sourceName, null, null, createTimestamp,
            null);
        }
      }
    });
  }

  private void insertProfileDemographics(Location location, UUID profileId, UUID profileCompanyDetailId,
                                         UUID profilePaymentAccountId, String sourceName,
                                         String businessName, String adbaName, String createTimestamp,
                                         String supplierCurrency) {
    ProfileDemographics profileDemographics = new ProfileDemographics();
    profileDemographics.setProfileDemographicsId(UUID.randomUUID());
    profileDemographics.setProfileId(profileId);
    profileDemographics.setProfileCompanyDetailId(profileCompanyDetailId);
    profileDemographics.setProfilePaymentAccountId(profilePaymentAccountId);
    profileDemographics.setApplicationSourceName(sourceName);
    if (nonNull(location)) {
      profileDemographics.setAddressLine1Text(location.getAddressLine1());
      profileDemographics.setAddressLine2Text(location.getAddressLine2());
      profileDemographics.setAddressLine3Text(location.getAddressLine3());
      profileDemographics.setAddressTypeName(location.getAddressType());
      profileDemographics.setProfileFullAddressText(location.getFullAddress());
      profileDemographics.setCityName(location.getCity());
      profileDemographics.setCountryName(location.getCountry());
      profileDemographics.setCountryCode(location.getCountryCode());
      profileDemographics.setRegionCode(location.getStateCode());
      profileDemographics.setRegionName(location.getState());
      profileDemographics.setPostalCode(location.getPostalCode());
      profileDemographics.setTelephoneNumber(location.getPhone());
      profileDemographics.setProfileEmailAddressJsonData(location.getEmail() != null
        ? CouchbasejsonUtils.convertJavaToJson(location.getEmail()) : null);
    }
    profileDemographics.setBusinessName(businessName);
    profileDemographics.setAdbaNameJsonData(adbaName);
    profileDemographics.setCreateBySourceName(sourceName);
    profileDemographics.setLastUpdateBySourceName(sourceName);
    profileDemographics.setCreateTimestamp(createTimestamp != null
      ? LocalDateTime.parse(createTimestamp) : LocalDateTime.now());
    profileDemographics.setLastUpdateTimestamp(LocalDateTime.now());
    profileDemographics.setSupplierCurrencyName(supplierCurrency);
    profileDemographicsRepository.save(profileDemographics);
  }

  private void insertProfileEvent(Event event, UUID profileId, UUID profileCompanyDetailId, boolean isQueuedEvent) {
    ProfileEvent profileEvent = new ProfileEvent();
    profileEvent.setProfileEventId(UUID.randomUUID());
    profileEvent.setProfileId(profileId);
    profileEvent.setProfileCompanyDetailId(profileCompanyDetailId);
    profileEvent.setEventJsonData(convertAndEncryptData(event));
    profileEvent.setEventType(event.getEventType().getEventType());
    profileEvent.setEventSubType(event.getEventSubType() != null
      ? event.getEventSubType().getEventSubType() : null);
    profileEvent.setEventQueueIndicator(isQueuedEvent);
    profileEvent.setCreateTimestamp(event.getConsumeTime() != null
      ? LocalDateTime.parse(event.getConsumeTime()) : LocalDateTime.now());
    profileEvent.setLastUpdateTimestamp(event.getTimestamp() != null
      ? LocalDateTime.parse(event.getTimestamp()) : LocalDateTime.now());
    profileEvent.setCreateBySourceName(event.getEnvelope().getActor().getSystem().getName());
    profileEvent.setLastUpdateBySourceName(event.getEnvelope().getActor().getSystem().getName());
    profileEventRepository.save(profileEvent);
  }

  private String convertAndEncryptData(Event event) {
    event.getCompanyId().stream()
      .filter(companyId -> CM11_TYPE.equals(companyId.getType()) || CM15_TYPE.equals(companyId.getType()))
      .forEach(companyId ->
        companyId.setValue(new EncryptDecryptUtil(environment).getEncryptedText(companyId.getValue())));

    return CouchbasejsonUtils.convertJavaToJson(event);
  }

  private void insertProfileRelationship(UUID profileCompanyDetailId, UUID counterPartyProfileCompanyDetailId,
                                         String sourceName, String createTimestamp) {
    ProfileRelationship profileRelationship = new ProfileRelationship();
    profileRelationship.setProfileRelationshipId(UUID.randomUUID());
    profileRelationship.setProfileCompanyDetailId(profileCompanyDetailId);
    profileRelationship.setCounterProfileCompanyDetailsId(counterPartyProfileCompanyDetailId);
    profileRelationship.setCreateBySourceName(sourceName);
    profileRelationship.setLastUpdateBySourceName(sourceName);
    profileRelationship.setCreateTimestamp(createTimestamp != null
      ? LocalDateTime.parse(createTimestamp) : LocalDateTime.now());
    profileRelationship.setLastUpdateTimestamp(LocalDateTime.now());
    profileRelationshipRepository.save(profileRelationship);
  }

  private void insertProfileContact(Contact contact, UUID profileId, UUID profileCompanyDetailId,
                                    String sourceName, String createTimestamp) {
    if (contact == null) {
      return;
    }
    ProfileContact profileContact = setContactMetaData(profileId, profileCompanyDetailId,
      sourceName, createTimestamp);
    profileContact.setFirstName(contact.getFirstName());
    profileContact.setLastName(contact.getLastName());
    profileContact.setRoleName(contact.getRole());
    profileContact.setTelephoneNumber(contact.getPhone());
    profileContact.setContactDescription(contact.getDescription());
    profileContact.setFaxNumber(contact.getFax());
    profileContact.setEmailAddressText(contact.getEmail());
    profileContactRepository.save(profileContact);
  }

  private void insertGenericContact(CompanyContact companyContact, UUID profileId, UUID profileCompanyDetailId,
                                    String sourceName, String createTimestamp) {
    if (companyContact.getGenericContact() != null) {
      GenericContact genericContact = companyContact.getGenericContact();
      ProfileContact profileContact = setContactMetaData(profileId, profileCompanyDetailId,
        sourceName, createTimestamp);
      profileContact.setTypeTx("generic");
      if (CollectionUtils.isNotEmpty(genericContact.getContactDestination())) {
        genericContact.getContactDestination().forEach(contactDestination -> setContacts(profileContact,
          contactDestination));
      }
      profileContactRepository.save(profileContact);
    }
  }

  private void insertDirectContact(CompanyContact companyContact, UUID profileId, UUID profileCompanyDetailId,
                                   String sourceName, String createTimestamp) {
    if (CollectionUtils.isNotEmpty(companyContact.getDirectContact())) {
      companyContact.getDirectContact().forEach(directContact -> {
        ProfileContact profileContact = setContactMetaData(profileId, profileCompanyDetailId,
          sourceName, createTimestamp);
        profileContact.setFullName(directContact.getPersonName());
        profileContact.setRoleName(directContact.getRole());
        profileContact.setTypeTx("direct");
        if (CollectionUtils.isNotEmpty(directContact.getContactDestination())) {
          for (ContactDestination contactDestination : directContact.getContactDestination()) {
            setContacts(profileContact, contactDestination);
          }
        }
        profileContactRepository.save(profileContact);
      });
    }
  }

  private void setContacts(ProfileContact profileContact, ContactDestination contactDestination) {
    if (contactDestination.getDestinationType().contains("url")) {
      profileContact.setUrlText(contactDestination.getDestination());
    }
    if (contactDestination.getDestinationType().contains("fax")) {
      profileContact.setFaxNumber(contactDestination.getDestination());
    }
    if (contactDestination.getDestinationType().contains("phone")) {
      profileContact.setTelephoneNumber(contactDestination.getDestination());
    }
    if (contactDestination.getDestinationType().contains("email")) {
      profileContact.setEmailAddressText(contactDestination.getDestination());
    }
  }

  private ProfileContact setContactMetaData(UUID profileId, UUID profileCompanyDetailId, String sourceName,
                                            String createTimestamp) {
    ProfileContact profileContact = new ProfileContact();
    profileContact.setProfileId(profileId);
    profileContact.setProfileCompanyDetailId(profileCompanyDetailId);
    profileContact.setCreateBySourceName(sourceName);
    profileContact.setLastUpdateBySourceName(sourceName);
    profileContact.setCreateTimestamp(createTimestamp != null
      ? LocalDateTime.parse(createTimestamp) : LocalDateTime.now());
    profileContact.setLastUpdateTimestamp(LocalDateTime.now());
    profileContact.setProfileContactId(UUID.randomUUID());
    return profileContact;
  }

  private List<CompanyId> getCompanyIDListOfTypeProfileCorrelationId(List<CompanyId> companyIds) {
    return companyIds.stream()
      .filter(companyId -> companyId.getType().equalsIgnoreCase(PROFILE_CORRELATION_ID.getIdType()))
      .collect(Collectors.toList());
  }

  private List<CompanyId> getCompanyIDListOfNotProfileCorrelationIdType(List<CompanyId> companyIds) {
    return companyIds.stream()
      .filter(companyId -> !companyId.getType().equalsIgnoreCase(PROFILE_CORRELATION_ID.getIdType()))
      .collect(Collectors.toList());
  }

  private String getSourceName(CompanyId companyId, CompanyDetails companyDetails) {
    if (companyDetails.getCreateSource() != null) {
      return companyDetails.getCreateSource().getSourceName();
    } else {
      return companyId.getSource().getSourceName();
    }
  }

}
