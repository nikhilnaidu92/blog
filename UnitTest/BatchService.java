package com.aexp.gcs.supplierprofile.scriptsupport.service;

import com.aexp.bta.onexp.domain.FeatureFlag;
import com.aexp.bta.onexp.service.FeatureToggles;
import com.aexp.gcs.supplierprofile.profilecommons.util.OneSupplierLogFacade;
import com.aexp.gcs.supplierprofile.scriptsupport.model.BatchParameter;
import com.aexp.gcs.supplierprofile.scriptsupport.model.FeatureFlagConstants;
import com.aexp.gcs.supplierprofile.scriptsupport.model.MigrationResult;
import com.aexp.gcs.supplierprofile.scriptsupport.repository.CouchbaseRepo;
import com.aexp.gcs.supplierprofile.scriptsupport.util.BatchProcessUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class BatchService {

  private final String className = this.getClass().getSimpleName();

  private final Environment environment;
  private final BatchProcessUtil batchProcessUtil;
  private final CouchbaseRepo couchbaseRepo;
  private final DatabaseMigrationService databaseMigrationService;

  public void migrateDocuments() throws UnknownHostException {
    String methodName = "migrateDocuments";
    String correlationId = UUID.randomUUID().toString();
    String appName = "PostgresMigrationBatch";
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
          String errorMessage;
          String trackingCorrelationId = "batch-";
          UUID profileId = null;
          for (String id : profileIds) {
            try {
              MigrationResult profile = couchbaseRepo.getProfileDocumentById(id);
              if (nonNull(profile.getBusinessProfile())
                && !"migrated-11".equals(profile.getBusinessProfile().getDocumentStatus())) {
                profileId = UUID.fromString(id);
                trackingCorrelationId = "batch-" + UUID.randomUUID();
                databaseMigrationService.validateAndInsert(profile, String.valueOf(UUID.fromString(profile.getId())),
                  trackingCorrelationId);
                OneSupplierLogFacade.info(appName, profileId, trackingCorrelationId, className, methodName,
                  "Successfully migrated Profile ID:" + profileId);
                processed.getAndIncrement();
              } else {
                OneSupplierLogFacade.info(appName, profileId, trackingCorrelationId, className, methodName,
                  "Profile ID:" + profileId + " is already migrated");
              }
            } catch (Exception ex) {
              errorMessage = "Failed to migrate document:" + profileId
                + ", due to " + OneSupplierLogFacade.getCompleteTrace(ex);
              OneSupplierLogFacade.error(appName, profileId, trackingCorrelationId, className, methodName,
                errorMessage, ex);
            }
          }
          OneSupplierLogFacade.info(appName, profileId, correlationId, className, methodName,
            "Total processed profiles: " + processed);
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
}
