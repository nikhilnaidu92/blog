package com.aexp.commercial.data.scheduler.config;

import com.aexp.commercial.data.constants.BatchConstants;
import com.aexp.commercial.data.scheduler.jobs.DemographicDataFeedJob;
import com.aexp.commercial.data.scheduler.jobs.EmailDataFeedJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import java.util.Objects;
import java.util.TimeZone;

import static com.aexp.commercial.data.constants.BatchConstants.DEMOGRAPHIC_DATA_FEED_CRON;
import static com.aexp.commercial.data.constants.BatchConstants.EMAIL_DATA_FEED_CRON;
import static com.aexp.commercial.data.constants.BatchConstants.PHX_TZ;
import static com.aexp.commercial.data.constants.BatchConstants.REMINDER_EMAIL_CRON;
import static org.quartz.Trigger.MISFIRE_INSTRUCTION_SMART_POLICY;

@Configuration
public class QuartzJobsConfig {

  private String groupName() {
    return BatchConstants.DATA_FEED_BATCH;
  }

  @Bean(name = "demographicDataFeedBeanTrigger")
  public CronTriggerFactoryBean demographicDataFeedBeanTrigger() {
    CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
    factoryBean.setJobDetail(Objects.requireNonNull(getDemographicDataFeedBean().getObject()));
    factoryBean.setStartDelay(0L);
    factoryBean.setGroup(groupName());
    factoryBean.setDescription("Trigger for Demographic data feed to Lumi");
    factoryBean.setCronExpression(DEMOGRAPHIC_DATA_FEED_CRON);
    factoryBean.setTimeZone(TimeZone.getTimeZone(PHX_TZ));
    factoryBean.setMisfireInstruction(MISFIRE_INSTRUCTION_SMART_POLICY);
    return factoryBean;
  }

  @Bean(name = "getDemographicDataFeedBean")
  public JobDetailFactoryBean getDemographicDataFeedBean() {
    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
    factoryBean.setJobClass(DemographicDataFeedJob.class);
    factoryBean.setGroup(groupName());
    factoryBean.setName("getDemographicDataFeedBean");
    factoryBean.setDurability(true);
    factoryBean.setRequestsRecovery(true);
    return factoryBean;
  }

  @Bean(name = "emailDataFeedBeanTrigger")
  public CronTriggerFactoryBean emailDataFeedBeanTrigger() {
    CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
    factoryBean.setJobDetail(Objects.requireNonNull(getEmailDataFeedBean().getObject()));
    factoryBean.setStartDelay(0L);
    factoryBean.setGroup(groupName());
    factoryBean.setDescription("Trigger for Email Data Feed to Lumi");
    factoryBean.setCronExpression(EMAIL_DATA_FEED_CRON);
    factoryBean.setTimeZone(TimeZone.getTimeZone(PHX_TZ));
    factoryBean.setMisfireInstruction(MISFIRE_INSTRUCTION_SMART_POLICY);
    return factoryBean;
  }

  @Bean(name = "getEmailDataFeedBean")
  public JobDetailFactoryBean getEmailDataFeedBean() {
    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
    factoryBean.setJobClass(EmailDataFeedJob.class);
    factoryBean.setGroup(groupName());
    factoryBean.setName("getEmailDataFeedBean");
    factoryBean.setDurability(true);
    factoryBean.setRequestsRecovery(true);
    return factoryBean;
  }

  @Bean(name = "reminderEmailBeanTrigger")
  public CronTriggerFactoryBean reminderEmailBeanTrigger() {
    CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
    factoryBean.setJobDetail(Objects.requireNonNull(getReminderEmailTriggerBean().getObject()));
    factoryBean.setStartDelay(0L);
    factoryBean.setGroup(groupName());
    factoryBean.setDescription("Trigger for Reminder Email");
    factoryBean.setCronExpression(REMINDER_EMAIL_CRON);
    factoryBean.setTimeZone(TimeZone.getTimeZone(PHX_TZ));
    factoryBean.setMisfireInstruction(MISFIRE_INSTRUCTION_SMART_POLICY);
    return factoryBean;
  }

  @Bean(name = "getReminderEmailTriggerBean")
  public JobDetailFactoryBean getReminderEmailTriggerBean() {
    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
    factoryBean.setJobClass(EmailDataFeedJob.class);
    factoryBean.setGroup(groupName());
    factoryBean.setName("getReminderEmailTriggerBean");
    factoryBean.setDurability(true);
    factoryBean.setRequestsRecovery(true);
    return factoryBean;
  }
}
