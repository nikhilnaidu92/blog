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
public class EmailDataFeedJob implements Job {

  private static final Logger logger = LoggerFactory.getLogger(EmailDataFeedJob.class);

  @Autowired
  private DataFeedService dataFeedService;

  public EmailDataFeedJob() {
    // Default controller required by Quartz.
  }

  @Override
  public void execute(JobExecutionContext arg0) {
    logger.info("Running the Email data batch feed");
    try {
      dataFeedService.generateEmailDataFile();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
