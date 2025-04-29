package com.aexp.gcs.supplierprofile.scriptsupport.scheduler.jobs;

import com.aexp.gcs.supplierprofile.scriptsupport.service.DatabaseMigrationService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class DatabaseMigrationJob implements Job {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationJob.class);

  @Autowired
  private DatabaseMigrationService databaseMigrationService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    JobDetail jobDetail = context.getJobDetail();
    JobDataMap map = jobDetail.getJobDataMap();
    try {
      int offset = Integer.parseInt(map.getString("offset"));
      int endOffset = Integer.parseInt(map.getString("endOffset"));
      int stepSize = Integer.parseInt(map.getString("stepSize"));
      while (offset <= endOffset) {
        logger.info("Running initial migration job for offset: {} and endOffset of: {}", offset, endOffset);
        databaseMigrationService.migrateDocuments(stepSize, offset);
        offset += stepSize;
        map.put("offset", String.valueOf(offset));
        persistJob(context, jobDetail, map);
      }
      map.put("completed", "true");
      map.put("processing", "false");
      persistJob(context, jobDetail, map);
      logger.info("Initial migration job completed");
    } catch (Exception e) {
      logger.error("Job execution failed", e);
      map.put("processing", "false");
      try {
        persistJob(context, jobDetail, map);
      } catch (SchedulerException ex) {
        throw new RuntimeException(ex);
      }
      throw new JobExecutionException(e);
    }
  }

  private void persistJob(JobExecutionContext context,
                          JobDetail jobDetail,
                          JobDataMap map) throws SchedulerException {
    JobDetail updated = jobDetail.getJobBuilder().usingJobData(map).build();
    context.getScheduler().addJob(updated, true);
  }
}

