package com.aexp.gcs.supplierprofile.scriptsupport.scheduler.jobs;

import com.aexp.gcs.supplierprofile.scriptsupport.service.DatabaseMigrationService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Configuration
public class DatabaseMigrationJob /*extends QuartzJobBean*/ implements Job {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationJob.class);

  @Autowired
  private DatabaseMigrationService databaseMigrationService;

//  @Autowired
//  private Scheduler scheduler;
//
//  @Autowired
//  private Environment environment;

//  public DatabaseMigrationJob() {
//    // Default controller required by Quartz.
//  }

//  @Override
//  public void execute(JobExecutionContext context) throws JobExecutionException {
//    JobDataMap dataMap = context.getJobDetail().getJobDataMap();
//    int offset = Integer.parseInt(dataMap.getString("offset"));
//    int batchSize = Integer.parseInt(dataMap.getString("batchSize"));
//    int podIndex = Integer.parseInt(dataMap.getString("podIndex"));
//
////    String podName = environment.getProperty("HOSTNAME", "Pod-" + podIndex);
////    JobKey jobKey = context.getJobDetail().getKey();
//
////    logger.info("Pod {} is processing job {} with offset {} and batch size {}", podName, jobKey, offset, batchSize);
//
//    try {
//      databaseMigrationService.migrateDocuments(offset, batchSize);
//    } catch (Exception e) {
//      throw new JobExecutionException("Error during database migration", e);
//    }
//  }

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//    JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
    JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();

    int offset = Integer.parseInt(dataMap.getString("offset"));
    int batchSize = Integer.parseInt(dataMap.getString("batchSize"));
//    int podIndex = Integer.parseInt(dataMap.getString("podIndex"));
    String type = dataMap.getString("type");

    if("initial".equalsIgnoreCase(type)) {
      logger.info("Running the Database migration job for the first time");
      databaseMigrationService.migrateDocuments(offset, batchSize);
    } else {
      logger.info("Running the Database migration job for the subsequent time");
    }
    String batchSequenceStr = dataMap.getString("batchOffset");
    int batchSequence = batchSequenceStr != null ? Integer.parseInt(batchSequenceStr) : 0;
    batchSequence++;
    dataMap.put("batchOffset", String.valueOf(batchSequence));
    logger.info("Running the Database migration batch with offset number: {}", batchSequence);
    try {
      jobExecutionContext.getScheduler().addJob(jobExecutionContext.getJobDetail(), true, true);
//      databaseMigrationService.migrateDocuments();
    } catch (SchedulerException e) {
      throw new JobExecutionException("Failed to update batch offset number", e);
//    } catch (UnknownHostException e) {
//      throw new RuntimeException(e);
    }
  }
}
