package com.aexp.gcs.supplierprofile.scriptsupport.service;

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
import com.aexp.gcs.supplierprofile.profilecommons.util.OneSupplierLogFacade;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.Action;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.BankAccountType;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.BusinessType;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.CompanySource;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.OnboardingStatus;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.PaymentMethod;
import com.aexp.gcs.supplierprofile.profiledatamodel.constants.SPFIdType;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.Profile;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfileCompany;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfileCompanyDetail;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfileContact;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfileDemographics;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfileEvent;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfilePaymentAccount;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity.ProfileRelationship;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.db.CompanyAlias;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.db.CompanyDetails;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.db.CompanyProfile;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.db.CompanyProfileEvent;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.Event;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.Adba;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.BusinessName;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.CompanyContact;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.CompanyId;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.Contact;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.ContactDestination;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.Demographics;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.DirectContact;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.GenericContact;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.Location;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.PaymentAccount;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.PaymentMethodDetails;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.VerificationMeta;
import com.aexp.gcs.supplierprofile.profiledatamodel.domain.model.company.verification.VerificationDetail;
import com.aexp.gcs.supplierprofile.scriptsupport.exception.AssertionValidationException;
import com.aexp.gcs.supplierprofile.scriptsupport.model.BusinessProfile;
import com.aexp.gcs.supplierprofile.scriptsupport.model.MigrationResult;
import com.aexp.gcs.supplierprofile.scriptsupport.repository.CouchbaseRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.aexp.gcs.supplierprofile.scriptsupport.constants.ScriptSupportConstants.CM11_TYPE;
import static com.aexp.gcs.supplierprofile.scriptsupport.constants.ScriptSupportConstants.CM15_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

@Data
@Service
@Transactional
@RequiredArgsConstructor
public class MigratedDocumentValidationService {
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
  private String className = this.getClass().getSimpleName();
  private String appName = "PostgresMigrationBatch";
  private ObjectMapper objectMapper = new ObjectMapper();

  public List<MigrationResult> fetchMigratedDocumentFromPostgres(List<String> profileIds) throws Exception {
    List<MigrationResult> responseList = new ArrayList<>();
    if (!profileIds.isEmpty()) {
      for (String id : profileIds) {
        UUID profileId = UUID.fromString(id);
        Optional<Profile> profile = profileRepository.fetchFullProfile(profileId);
        MigrationResult migrationResult = new MigrationResult();
        if (profile.isPresent()) {
          Set<ProfileCompanyDetail> profileCompanyDetailList = profile.get().getProfileCompanyDetail();
          Set<ProfileCompany> profileCompanyList = profile.get().getProfileCompanyList();
          Set<ProfileContact> profileContactList = profile.get().getProfileContactList();
          Set<ProfileDemographics> profileDemographicsList = profile.get().getProfileDemographics();
          Set<ProfileEvent> profileEventList = profile.get().getProfileEventList();

          migrationResult.setId(String.valueOf(profileId));
          migrationResult.setBusinessProfile(getBusinessProfile(profile.get(), profileCompanyDetailList,
            profileCompanyList, profileContactList, profileDemographicsList, profileEventList));
          responseList.add(migrationResult);
        }
      }
    }
    return responseList;
  }

  public List<MigrationResult> fetchDocumentsFromCouchbase(List<String> profileIds) {
    List<MigrationResult> responseList = new ArrayList<>();
    if (!profileIds.isEmpty()) {
      for (String id : profileIds) {
        MigrationResult existingCBResults = couchbaseRepo.getProfileDocumentById(String.valueOf(id));
        responseList.add(existingCBResults);
      }
    }
    return responseList;
  }

  public void validateMigratedDocuments(UUID profileId) {
    String methodName = "validateMigratedDocuments";
    MigrationResult migrationResult = new MigrationResult();
    String errorMessage;
    try {
      Optional<Profile> profile = profileRepository.fetchFullProfile(profileId);
      if (profile.isPresent()) {
        Set<ProfileCompanyDetail> profileCompanyDetailList = profile.get().getProfileCompanyDetail();
        Set<ProfileCompany> profileCompanyList = profile.get().getProfileCompanyList();
        Set<ProfileContact> profileContactList = profile.get().getProfileContactList();
        Set<ProfileDemographics> profileDemographicsList = profile.get().getProfileDemographics();
        Set<ProfileEvent> profileEventList = profile.get().getProfileEventList();

        migrationResult.setId(String.valueOf(profileId));
        migrationResult.setBusinessProfile(getBusinessProfile(profile.get(), profileCompanyDetailList,
          profileCompanyList, profileContactList, profileDemographicsList, profileEventList));

        MigrationResult existingCBResults = couchbaseRepo.getProfileDocumentById(String.valueOf(profileId));
        assertDocumentObjects(migrationResult, existingCBResults);
        OneSupplierLogFacade.info(appName, profileId, "correlationId", className, methodName,
          "Assertion is successful for Profile ID:" + profileId);
      } else {
        OneSupplierLogFacade.error(appName, profileId, "correlationId", className, methodName,
          "Couldn't assert, profile not found in database" + profileId);
      }
    } catch (AssertionError | Exception e) {
      errorMessage = "Assertion failed for:" + profileId + ", due to " + OneSupplierLogFacade.getShortTrace(e);
      throw new AssertionValidationException(profileId, errorMessage);
    }
  }

  public void assertDocumentObjects(MigrationResult migrationResult, MigrationResult existingCBResult) {

    CompanyProfile migratedCompanyProfile = migrationResult.getBusinessProfile().getCompanyProfile();
    CompanyProfile cbCompanyProfile = existingCBResult.getBusinessProfile().getCompanyProfile();

    List<CompanyId> migratedCompanyIds = migratedCompanyProfile.getCompanyId().stream()
      .filter(id -> !CM11_TYPE.equalsIgnoreCase(id.getType()) && !CM15_TYPE.equalsIgnoreCase(id.getType())).toList();
    List<CompanyId> cbCompanyIds = cbCompanyProfile.getCompanyId().stream()
      .filter(id -> !CM11_TYPE.equalsIgnoreCase(id.getType()) && !CM15_TYPE.equalsIgnoreCase(id.getType())).toList();
    cbCompanyIds = cbCompanyIds.stream().filter(id -> !Strings.isNullOrEmpty(id.getValue())).toList();
    List<CompanyId> couchbaseCompanyIds = new ArrayList<>(cbCompanyIds);
    List<CompanyId> taxIdList = cbCompanyIds.stream().filter(id -> "tax_id".equalsIgnoreCase(id.getType())).toList();
    if (!taxIdList.isEmpty()) {
      CompanyId taxId = taxIdList.get(0);
      couchbaseCompanyIds.removeAll(taxIdList);
      couchbaseCompanyIds.add(taxId);
    }

    if (cbCompanyProfile.getCounterpartyIds() != null && !cbCompanyProfile.getCounterpartyIds().isEmpty()) {
      assertThat(cbCompanyProfile.getCounterpartyIds())
        .containsAllEntriesOf(migratedCompanyProfile.getCounterpartyIds());
    }

    if (cbCompanyProfile.getDemographics() != null) {
      assertThat(cbCompanyProfile.getDemographics())
        .usingRecursiveComparison()
        .ignoringActualNullFields()
        .ignoringExpectedNullFields()
        .isEqualTo(migratedCompanyProfile.getDemographics());
    }

    if (cbCompanyProfile.getCompanyDetails() != null && migratedCompanyProfile.getCompanyDetails() != null) {
      List<CompanyId> cbCompanyIdList = cbCompanyProfile.getCompanyId();
      List<CompanyId> cbCorrelatedCompanyIds = getCompanyIDListOfTypeProfileCorrelationId(cbCompanyIdList);

      List<CompanyId> migratedCompanyIdList = migratedCompanyProfile.getCompanyId();
      List<CompanyId> migratedCorrelatedCompanyIds = getCompanyIDListOfTypeProfileCorrelationId(migratedCompanyIdList);

      List<CompanyId> matchedCompanyIds = cbCorrelatedCompanyIds.stream().filter(profileCompanyId ->
          migratedCorrelatedCompanyIds.stream().anyMatch(correlationCompanyId ->
            correlationCompanyId.getValue().equals(profileCompanyId.getValue())))
        .toList();
      for (CompanyId companyId : matchedCompanyIds) {
        CompanyDetails cbCompanyDetails = cbCompanyProfile.getCompanyDetails().get(companyId.getValue());
        CompanyDetails migratedCompanyDetails = migratedCompanyProfile.getCompanyDetails().get(companyId.getValue());
        if (cbCompanyDetails.getCounterpartyCorrelationId() != null) {
          assertThat(cbCompanyDetails.getCounterpartyCorrelationId())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .ignoringExpectedNullFields()
            .isEqualTo(migratedCompanyDetails.getCounterpartyCorrelationId());
        }
        if (cbCompanyDetails.getDemographics() != null) {
          if (Objects.nonNull(cbCompanyDetails.getDemographics().getCompanyContact())) {
            if (Objects.nonNull(cbCompanyDetails.getDemographics().getCompanyContact().getGenericContact())
              && !cbCompanyDetails.getDemographics().getCompanyContact().getGenericContact().getContactDestination()
              .isEmpty()) {
              List<ContactDestination> contactDestinationList = cbCompanyDetails.getDemographics().getCompanyContact()
                .getGenericContact().getContactDestination().stream()
                .filter(contactDestination -> Objects.nonNull(contactDestination.getDestination()))
                .toList();
              cbCompanyDetails.getDemographics().getCompanyContact().getGenericContact().setContactDestination(
                contactDestinationList);
            }
            if (Objects.nonNull(cbCompanyDetails.getDemographics().getCompanyContact().getDirectContact())
              && !cbCompanyDetails.getDemographics().getCompanyContact().getDirectContact().isEmpty()) {
              for (DirectContact directContact : cbCompanyDetails.getDemographics()
                .getCompanyContact().getDirectContact()) {
                List<ContactDestination> contactDestinationList = new ArrayList<>();
                for (ContactDestination contactDestination : directContact.getContactDestination()) {
                  if (Objects.nonNull(contactDestination.getDestination())) {
                    contactDestinationList.add(contactDestination);
                  }
                }
                directContact.setContactDestination(contactDestinationList);
              }
            }
          }

          assertThat(cbCompanyDetails.getDemographics())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .ignoringExpectedNullFields()
            .isEqualTo(migratedCompanyDetails.getDemographics());
        }
        if (cbCompanyDetails.getContacts() != null && !cbCompanyDetails.getContacts().isEmpty()) {
          if (Objects.isNull(migratedCompanyDetails.getContacts()) || migratedCompanyDetails.getContacts().isEmpty()) {
            migratedCompanyDetails.setContacts(Collections.singletonList(null));
          }
          assertThat(cbCompanyDetails.getContacts())
            .hasSameSizeAs(migratedCompanyDetails.getContacts());
          assertThat(cbCompanyDetails.getContacts().get(0))
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .ignoringExpectedNullFields()
            .isEqualTo(migratedCompanyDetails.getContacts().get(0));
        }
        if (cbCompanyDetails.getPaymentAccounts() != null && migratedCompanyDetails.getPaymentAccounts() != null) {
          if (cbCompanyDetails.getPaymentAccounts().size() > 1) {
            assertThat(cbCompanyDetails.getPaymentAccounts().stream().filter(
              paymentAccount -> paymentAccount.getAction() != Action.DELETE
            ).toList()).hasSameSizeAs(migratedCompanyDetails.getPaymentAccounts());
            cbCompanyDetails.getPaymentAccounts().stream().filter(
              paymentAccount -> paymentAccount.getAction() != Action.DELETE
            ).forEach(cbPaymentAccount ->
              migratedCompanyDetails.getPaymentAccounts()
                .stream()
                .filter(pgPaymentAccount -> (
                    Objects.equals(cbPaymentAccount.getProfilePaymentReferenceId(),
                      pgPaymentAccount.getProfilePaymentReferenceId())
                  ) && cbPaymentAccount.getPaymentMethod() == pgPaymentAccount.getPaymentMethod()
                )
                .forEach(pgPaymentAccount ->
                  assertThat(cbPaymentAccount)
                    .usingRecursiveComparison()
                    .ignoringActualNullFields()
                    .ignoringExpectedNullFields()
                    .ignoringFields("paymentInstrument")
                    .isEqualTo(pgPaymentAccount)));
          } else {
            assertThat(cbCompanyDetails.getPaymentAccounts().get(0))
              .usingRecursiveComparison()
              .ignoringActualNullFields()
              .ignoringExpectedNullFields()
              .ignoringFields("paymentInstrument")
              .isEqualTo(migratedCompanyDetails.getPaymentAccounts().get(0));
          }
        }
        if (cbCompanyDetails.getBusinessType() != null) {
          assertThat(cbCompanyDetails.getBusinessType())
            .isEqualTo(migratedCompanyDetails.getBusinessType());
        }
      }
    }

    List<Event> filteredCbEvents = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(existingCBResult.getBusinessProfile().getCompanyProfileEvent().getEvents())) {
      List<CompanyId> correlatedCompanyIds = getCompanyIDListOfTypeProfileCorrelationId(cbCompanyIds);
      existingCBResult.getBusinessProfile().getCompanyProfileEvent().getEvents().forEach(event -> {
        List<CompanyId> profileCompanyDetailId = getCompanyIDListOfTypeProfileCorrelationId(event.getCompanyId());
        List<CompanyId> matchedCompanyIds = profileCompanyDetailId.stream().filter(profileCompanyId ->
            correlatedCompanyIds.stream().anyMatch(correlationCompanyId ->
              correlationCompanyId.getValue().equals(profileCompanyId.getValue())))
          .toList();
        if (!matchedCompanyIds.isEmpty()) {
          filteredCbEvents.add(event);
        }
      });
    }
    assertThat(filteredCbEvents.size())
      .isEqualTo(migrationResult.getBusinessProfile().getCompanyProfileEvent().getEvents().size());

    assertThat(couchbaseCompanyIds).isNotEmpty()
      .hasSameSizeAs(migratedCompanyIds);

    assertThat(couchbaseCompanyIds)
      .usingRecursiveFieldByFieldElementComparator()
      .containsAll(migratedCompanyIds);
  }

  private BusinessProfile getBusinessProfile(Profile profile, Set<ProfileCompanyDetail> profileCompanyDetailList,
                                             Set<ProfileCompany> profileCompanyList,
                                             Set<ProfileContact> profileContactList,
                                             Set<ProfileDemographics> profileDemographicsList,
                                             Set<ProfileEvent> profileEventList)
    throws JsonProcessingException {
    Map<String, CompanyDetails> companyDetailsMap = new HashMap<>();
    CompanyProfile companyProfile = new CompanyProfile();
    Map<String, Set<String>> counterpartyIds = new LinkedHashMap<>();
    List<CompanyAlias> companyAliasList = new ArrayList<>();
    UUID counterPartyProfileId;
    Set<String> value = new HashSet<>();
    CompanyAlias companyAlias = new CompanyAlias();
    if (profileCompanyDetailList != null && !profileCompanyDetailList.isEmpty()) {
      for (ProfileCompanyDetail profileCompanyDetail : profileCompanyDetailList) {
        CompanyDetails companyDetails = new CompanyDetails();
        if (profileCompanyDetail.getBusinessTypeText() != null) {
          companyDetails.setBusinessType(BusinessType.valueOf(profileCompanyDetail.getBusinessTypeText()));
        }
        if (profileContactList != null && !profileContactList.isEmpty()) {
          List<Contact> companyIdContactList = profileContactList.stream()
            .filter(profileContact -> profileContact.getProfileCompanyDetailId() != null
              && profileContact.getTypeTx() == null
              && profileCompanyDetail.getProfileCompanyDetailId().equals(
              profileContact.getProfileCompanyDetailId())
            ).map(this::getContact)
            .collect(Collectors.toList());
          companyDetails.setContacts(companyIdContactList);
        }
        if (profileCompanyDetail.getOnboardingStatusDescription() != null) {
          companyDetails.setOnboardingStatus(OnboardingStatus
            .getOnboardingStatusByDescription(profileCompanyDetail.getOnboardingStatusDescription()).get());
        }
        companyDetails.setOnboardingStatusCode(profileCompanyDetail.getOnboardingStatusCode());
        if (profileCompanyDetail.getCreateSourceText() != null) {
          companyDetails.setCreateSource(CompanySource.valueOf(profileCompanyDetail.getCreateSourceText()));
        }
        companyDetails.setDeleteStatus(profileCompanyDetail.isDeleteStatusIndicator());
        companyDetails.setCreateTime(String.valueOf(profileCompanyDetail.getCreateTimestamp()));
        companyDetails.setSchemaVersion(profileCompanyDetail.getVersionNumber());
        companyDetails.setCreateSource(CompanySource.valueOf(profileCompanyDetail.getCreateSourceText()));

        List<ProfilePaymentAccount> profilePaymentAccountList = profilePaymentAccountRepository
          .findByProfileCompanyDetailId(profileCompanyDetail.getProfileCompanyDetailId());
        companyDetails.setPaymentAccounts(getPaymentAccounts(profilePaymentAccountList));

        CompanyContact companyContact = new CompanyContact();
        if (CollectionUtils.isNotEmpty(profileContactList)) {
          profileContactList.stream().filter(profileContact -> profileContact.getProfileCompanyDetailId() != null
              && profileContact.getTypeTx() != null
              && profileCompanyDetail.getProfileCompanyDetailId().equals(
              profileContact.getProfileCompanyDetailId()))
            .forEach(profileContact -> {
              if (profileContact.getTypeTx().equals("generic")) {
                companyContact.setGenericContact(getGenericContact(profileContact));
              }
              if (profileContact.getTypeTx().equals("direct")) {
                companyContact.setDirectContact(getDirectContact(profileContact));
              }
            });
        }
        if (profileDemographicsList != null && !profileDemographicsList.isEmpty()) {
          for (ProfileDemographics companyDetailDemographics : profileDemographicsList) {
            if (companyDetailDemographics.getProfileCompanyDetailId() != null
              && companyDetailDemographics.getProfilePaymentAccountId() == null
              && profileCompanyDetail.getProfileCompanyDetailId()
              .equals(companyDetailDemographics.getProfileCompanyDetailId())) {
              companyDetails.setDemographics(getDemographics(companyDetailDemographics, companyContact, null, null));
            }
          }
        }
        if (Objects.isNull(companyDetails.getDemographics())) {
          companyDetails.setDemographics(new Demographics());
        }

        List<ProfileRelationship> profileRelationshipList = profileRelationshipRepository
          .findByProfileCompanyDetailIdOrCounterProfileCompanyDetailsId(profileCompanyDetail
            .getProfileCompanyDetailId());
        if (!profileRelationshipList.isEmpty()) {
          for (ProfileRelationship profileRelationship : profileRelationshipList) {
            UUID correlationId = profileRelationship.getCounterProfileCompanyDetailsId();
            Optional<ProfileCompanyDetail> counterProfileCompanyDetails = profileCompanyDetailRepository
              .findByProfileCompanyDetailId(correlationId);
            Optional<ProfileCompany> profileCompanyOptional = profileCompanyRepository
              .findAllByProfileCompanyDetailIdAndProfileId(correlationId,
                counterProfileCompanyDetails.get().getProfileId());
            CompanyId companyId = new CompanyId();
            companyId.setValue(String.valueOf(profileRelationship.getCounterProfileCompanyDetailsId()));
            companyId.setSource(CompanySource.valueOf(profileRelationship.getCreateBySourceName()));
            if (profileCompanyOptional.isPresent()) {
              value.add(String.valueOf(profileCompanyOptional.get().getProfileCompanyDetailId())
                + profileCompanyOptional.get().getSourceCode());
              companyId.setFlag(profileCompanyOptional.get().isUniqueFlagIndicator());
              companyId.setType(profileCompanyOptional.get().getTypeText());
            }
            companyDetails.setCounterpartyCorrelationId(companyId);

            counterPartyProfileId = counterProfileCompanyDetails.get().getProfileId();
            companyAlias.setProfileId(counterPartyProfileId.toString());
            companyAlias.setSource(String.valueOf(profileCompanyOptional.get().getSourceCode()));
            companyAlias.setProfileCorrelationId(String.valueOf(correlationId));
            companyAlias.setIdType(profileCompanyOptional.get().getTypeText());
            companyAliasList.add(companyAlias);
            counterpartyIds.put(String.valueOf(profileRelationship.getProfileCompanyDetailId()), value);
          }
          value = new HashSet<>();
        }
        companyDetailsMap.put(profileCompanyDetail.getProfileCompanyDetailId().toString(), companyDetails);
      }
    }
    companyProfile.setCounterpartyIds(counterpartyIds);
    companyProfile.setCompanyDetails(companyDetailsMap);

    CompanyContact profileLevelContact = new CompanyContact();
    if (CollectionUtils.isNotEmpty(profileContactList)) {
      profileContactList.stream().filter(profileContact -> profileContact.getProfileCompanyDetailId() == null)
        .forEach(profileContact -> {
          if (profileContact.getTypeTx().equals("generic")) {
            profileLevelContact.setGenericContact(getGenericContact(profileContact));
          }
          if (profileContact.getTypeTx().equals("direct")) {
            profileLevelContact.setDirectContact(getDirectContact(profileContact));
          }
        });
    }
    companyProfile.setProfileId(String.valueOf(profile.getProfileId()));
    companyProfile.setCompanyId(getCompanyIdsList(profileCompanyList));

    if (profileDemographicsList != null && !profileDemographicsList.isEmpty()) {
      for (ProfileDemographics profileDemographics : profileDemographicsList) {
        if (profileDemographics.getProfileCompanyDetailId() == null
          && profileDemographics.getProfilePaymentAccountId() == null) {
          companyProfile.setDemographics(getDemographics(profileDemographics, profileLevelContact,
            profile.getConfidenceCode(), profile.getConfidenceSourceTx()));
        }
      }
    }

    if (Objects.isNull(companyProfile.getDemographics())) {
      companyProfile.setDemographics(new Demographics());
    }

    /* Set company profile event */
    CompanyProfileEvent companyProfileEvent = new CompanyProfileEvent();
    List<Event> events = new ArrayList<>();
    if (profileEventList != null && !profileEventList.isEmpty()) {
      for (ProfileEvent profileEvent : profileEventList) {
        Event event = CouchbasejsonUtils.convertJsonToJava(profileEvent.getEventJsonData(), Event.class);
        events.add(event);
      }
    }
    companyProfileEvent.setDocumentId(String.valueOf(profile.getProfileId()));
    companyProfileEvent.setEvents(events);

    BusinessProfile businessProfile = new BusinessProfile();
    businessProfile.setCompanyProfile(companyProfile);
    businessProfile.setCompanyProfileEvent(companyProfileEvent);
    businessProfile.setCompanyAliasList(companyAliasList);
    return businessProfile;
  }

  private List<PaymentAccount> getPaymentAccounts(List<ProfilePaymentAccount> profilePaymentAccountList)
    throws JsonProcessingException {
    List<PaymentAccount> paymentAccountList = new ArrayList<>();
    for (ProfilePaymentAccount profilePaymentAccount : profilePaymentAccountList) {
      PaymentAccount paymentAccount = new PaymentAccount();
      if (profilePaymentAccount.getLogicActionText() != null) {
        paymentAccount.setAction(Action.valueOf(profilePaymentAccount.getLogicActionText().toUpperCase()));
      }
      paymentAccount.setName(profilePaymentAccount.getPaymentAccountName());
      if (profilePaymentAccount.getPaymentMethodCode() != null) {
        paymentAccount.setPaymentMethod(PaymentMethod.getEnumByValue(profilePaymentAccount.getPaymentMethodCode()));
      }

      paymentAccount.setDuplicate(profilePaymentAccount.isDuplicateIndicator());
      PaymentMethodDetails paymentMethodDetails = new PaymentMethodDetails();
      paymentMethodDetails.setBankName(profilePaymentAccount.getBankName());
      paymentMethodDetails.setSortCode(profilePaymentAccount.getSortCode());

      Optional<ProfileDemographics> profileDemographics = profileDemographicsRepository
        .findByProfilePaymentAccountId(profilePaymentAccount.getProfilePaymentAccountId());
      profileDemographics.ifPresent(demographics ->
        paymentMethodDetails.setLocation(getLocation(demographics, null, null)));
      paymentMethodDetails.setAccountNumber(profilePaymentAccount.getPaymentAccountNumber());
      if (profilePaymentAccount.getAccountTypeText() != null) {
        paymentMethodDetails.setBankAccountType(BankAccountType.valueOf(profilePaymentAccount.getAccountTypeText()));
      }

      paymentMethodDetails.setBankNickname(profilePaymentAccount.getBankNickName());
      paymentMethodDetails.setInternationalBankAccountNumber(profilePaymentAccount.getIbanNumber());
      paymentMethodDetails.setNameOnBankAccount(profilePaymentAccount.getNmOnBankAccountText());
      paymentMethodDetails.setRoutingNumber(profilePaymentAccount.getPaymentRoutingNumber());
      paymentMethodDetails.setSwiftAccount(profilePaymentAccount.getSwiftAccountNumber());
      paymentAccount.setPaymentdetails(paymentMethodDetails);
      if (profilePaymentAccount.getVerificationDetailJsonData() != null) {
        paymentAccount.setVerificationDetail(objectMapper
          .readValue(profilePaymentAccount.getVerificationDetailJsonData(), VerificationDetail.class));
      }
      if (profilePaymentAccount.getTransactionId() != null) {
        VerificationMeta verificationMeta = VerificationMeta.builder()
          .transactionId(profilePaymentAccount.getTransactionId())
          .ipAddress(profilePaymentAccount.getIpAddressExtensionText())
          .uploadedBy(profilePaymentAccount.getUploadedByName())
          .uploadedType(profilePaymentAccount.getUploadTypeText())
          .build();
        paymentAccount.setVerificationMeta(verificationMeta);
      }
      paymentAccount.setProfilePaymentReferenceId(profilePaymentAccount.getReferenceId());
      paymentAccountList.add(paymentAccount);
    }
    return paymentAccountList;
  }

  private List<CompanyId> getCompanyIdsList(Set<ProfileCompany> profileCompanyList) {
    List<CompanyId> companyIdList = new ArrayList<>();
    if (profileCompanyList != null && !profileCompanyList.isEmpty()) {
      for (ProfileCompany profileCompany : profileCompanyList) {
        CompanyId companyId = new CompanyId();
        companyId.setSource(profileCompany.getSourceCode());
        companyId.setValue(profileCompany.getValueText());
        companyId.setFlag(profileCompany.isUniqueFlagIndicator());
        companyId.setType(profileCompany.getTypeText());
        companyIdList.add(companyId);
      }
    }
    return companyIdList;
  }

  private Demographics getDemographics(ProfileDemographics profileDemographics, CompanyContact companyContact,
                                       String confidenceCode, String confidenceSource) throws JsonProcessingException {
    Demographics demographics = new Demographics();
    BusinessName businessName = new BusinessName();
    businessName.setName(profileDemographics.getBusinessName());
    if (profileDemographics.getAdbaNameJsonData() != null) {
      businessName.setAdba(List.of(objectMapper.readValue(profileDemographics.getAdbaNameJsonData(), Adba[].class)));
    }
    demographics.setBusinessName(businessName);
    demographics.setLocation(getLocation(profileDemographics, confidenceCode, confidenceSource));
    if (companyContact != null) {
      demographics.setCompanyContact(companyContact);
    }
    return demographics;
  }

  private Contact getContact(ProfileContact profileContact) {
    Contact contact = new Contact();
    contact.setFirstName(profileContact.getFirstName());
    contact.setPhone(profileContact.getTelephoneNumber());
    contact.setRole(profileContact.getRoleName());
    contact.setDescription(profileContact.getContactDescription());
    contact.setLastName(profileContact.getLastName());
    contact.setEmail(profileContact.getEmailAddressText());
    contact.setFax(profileContact.getFaxNumber());
    return contact;
  }

  private Location getLocation(ProfileDemographics profileDemographics, String confidenceCode,
                               String confidenceSource) {
    Location location = new Location();
    location.setConfidenceCode(confidenceCode);
    location.setCountryCode(profileDemographics.getCountryCode());
    location.setCountry(profileDemographics.getCountryName());
    location.setPostalCode(profileDemographics.getPostalCode());
    location.setAddressLine1(profileDemographics.getAddressLine1Text());
    location.setAddressLine2(profileDemographics.getAddressLine2Text());
    location.setAddressLine3(profileDemographics.getAddressLine3Text());
    location.setAddressType(profileDemographics.getAddressTypeName());
    location.setCity(profileDemographics.getCityName());
    if (confidenceSource != null) {
      location.setConfidenceSource(CompanySource.valueOf(confidenceSource));
    }
    if (profileDemographics.getProfileEmailAddressJsonData() != null) {
      location.setEmail(CouchbasejsonUtils
        .convertJsonToJava(profileDemographics.getProfileEmailAddressJsonData(), String.class));
    }
    location.setFullAddress(profileDemographics.getProfileFullAddressText());
    location.setPhone(profileDemographics.getTelephoneNumber());
    location.setState(profileDemographics.getRegionName());
    location.setStateCode(profileDemographics.getRegionCode());
    location.setSource(profileDemographics.getCreateBySourceName());
    return location;
  }

  private List<DirectContact> getDirectContact(ProfileContact profileContact) {
    List<DirectContact> directContactList = new ArrayList<>();
    DirectContact directContact = new DirectContact();
    directContact.setPersonName(profileContact.getFullName());
    directContact.setRole(profileContact.getRoleName());
    directContact.setContactDestination(getContacts(profileContact));
    directContactList.add(directContact);
    return directContactList;
  }

  private GenericContact getGenericContact(ProfileContact profileContact) {
    GenericContact genericContact = new GenericContact();
    genericContact.setDescription(profileContact.getTypeTx());
    genericContact.setContactDestination(getContacts(profileContact));
    return genericContact;
  }

  private List<ContactDestination> getContacts(ProfileContact profileContact) {
    ContactDestination contactDestination;
    List<ContactDestination> contactDestinationList = new ArrayList<>();
    if (profileContact.getTelephoneNumber() != null) {
      contactDestination = new ContactDestination();
      contactDestination.setDestinationType("phone");
      contactDestination.setDestination(profileContact.getTelephoneNumber());
      contactDestinationList.add(contactDestination);
    }
    if (profileContact.getEmailAddressText() != null) {
      contactDestination = new ContactDestination();
      contactDestination.setDestinationType("email");
      contactDestination.setDestination(profileContact.getEmailAddressText());
      contactDestinationList.add(contactDestination);
    }
    if (profileContact.getUrlText() != null) {
      contactDestination = new ContactDestination();
      contactDestination.setDestinationType("url");
      contactDestination.setDestination(profileContact.getUrlText());
      contactDestinationList.add(contactDestination);
    }
    if (profileContact.getFaxNumber() != null) {
      contactDestination = new ContactDestination();
      contactDestination.setDestinationType("fax");
      contactDestination.setDestination(profileContact.getFaxNumber());
      contactDestinationList.add(contactDestination);
    }
    return contactDestinationList;
  }

  private List<CompanyId> getCompanyIDListOfTypeProfileCorrelationId(List<CompanyId> companyIds) {
    return companyIds.stream()
      .filter(companyId -> companyId.getType().equalsIgnoreCase(SPFIdType.PROFILE_CORRELATION_ID.getIdType()))
      .collect(Collectors.toList());
  }

}
