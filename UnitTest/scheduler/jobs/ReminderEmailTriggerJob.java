package com.aexp.commercial.data.scheduler.jobs;

import com.aexp.commercial.data.service.ReminderEmailService;
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
public class ReminderEmailTriggerJob implements Job {

  private static final Logger logger = LoggerFactory.getLogger(ReminderEmailTriggerJob.class);

  @Autowired
  private ReminderEmailService reminderEmailService;

  public ReminderEmailTriggerJob() {
    // Default controller required by Quartz.
  }

  @Override
  public void execute(JobExecutionContext arg0) {
    logger.info("Running the Reminder Email job");
    try {
      reminderEmailService.sendReminderEmails();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
