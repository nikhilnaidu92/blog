package com.aexp.commercial.data.scheduler.jobs;

import com.aexp.commercial.data.service.DataFeedService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Configuration
public class DemographicDataFeedJob implements Job {

  private static final Logger logger = LoggerFactory.getLogger(DemographicDataFeedJob.class);

  @Autowired
  private DataFeedService dataFeedService;

  public DemographicDataFeedJob() {
    // Default controller required by Quartz.
  }

  @Override
  public void execute(JobExecutionContext arg0) {
    logger.info("Running the Demographic data batch feed");
    try {
      dataFeedService.generateDemographicDataFile();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
