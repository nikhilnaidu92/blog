package com.aexp.gcs.supplierprofile.scriptsupport.scheduler.config;

import com.aexp.gcs.supplierprofile.scriptsupport.constants.ScriptSupportConstants;
import com.aexp.gcs.supplierprofile.scriptsupport.scheduler.jobs.DatabaseMigrationJob;
import com.aexp.gcs.supplierprofile.scriptsupport.util.BatchProcessUtil;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import static com.aexp.gcs.supplierprofile.scriptsupport.constants.ScriptSupportConstants.DATABASE_MIGRATION_CRON;
import static com.aexp.gcs.supplierprofile.scriptsupport.constants.ScriptSupportConstants.PHX_TZ;
import static org.quartz.Trigger.MISFIRE_INSTRUCTION_SMART_POLICY;

@Configuration
public class QuartzJobsConfig {

  @Autowired
  private Scheduler scheduler;

  @Autowired
  private BatchProcessUtil batchProcessUtil;

  @Autowired
  private Environment environment;

  private String groupName() {
    return "DATABASE_MIGRATION_BATCH";
  }

  @Bean
  public JobDetailFactoryBean getDatabaseMigrationJobBean() throws SchedulerException {
    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
    factoryBean.setJobClass(DatabaseMigrationJob.class);
    factoryBean.setDurability(true);
    factoryBean.setGroup(groupName());
    factoryBean.setName("DatabaseMigrationJob");
    factoryBean.setRequestsRecovery(true);

    return factoryBean;
  }

  @PostConstruct
  public void scheduleOffsetJobs() throws SchedulerException {
    int numberOfPods = environment.getProperty("pod-count", int.class, 10);
    int totalDocuments = batchProcessUtil.getProfileCount();
    int batchSize = totalDocuments / numberOfPods;
    for (int i = 0; i < numberOfPods; i++) {
      String jobKeyName = "OffsetJob-" + i;
      JobKey jobKey = new JobKey(jobKeyName, groupName());
      if (scheduler.checkExists(jobKey)) continue;

      int offset = i * batchSize;

      JobDataMap map = new JobDataMap();
      map.put("offset", String.valueOf(offset));
      map.put("batchSize", String.valueOf(batchSize));
      map.put("podIndex", String.valueOf(i));
      map.put("type", "initial");

      JobDetail job = JobBuilder.newJob(DatabaseMigrationJob.class)
        .withIdentity(jobKey)
        .usingJobData(map)
        .storeDurably()
        .build();

      scheduler.addJob(job, true);

      scheduler.triggerJob(jobKey, map);
    }
  }

  @Bean(name = "recurringSyncTrigger")
  public CronTriggerFactoryBean recurringSyncTrigger() {
    CronTriggerFactoryBean factory = new CronTriggerFactoryBean();
    factory.setJobDetail(recurringSyncJob().getObject());
    factory.setCronExpression("0 */5 * * * ?");
    factory.setTimeZone(TimeZone.getTimeZone("UTC"));
    factory.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
    return factory;
  }

  @Bean
  public JobDetailFactoryBean recurringSyncJob() {
    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
    factoryBean.setJobClass(DatabaseMigrationJob.class);
    factoryBean.setDurability(true);
    factoryBean.setGroup("SYNC_GROUP");
    factoryBean.setName("RecurringSyncJob");

    JobDataMap map = new JobDataMap();
    map.put("offset", String.valueOf(0));
    map.put("batchSize", String.valueOf(10_000));
    map.put("type", "sync");
    map.put("batchSequence", String.valueOf(0));

    factoryBean.setJobDataMap(map);
    return factoryBean;
  }
}


//  @Bean(name = "databaseMigrationTrigger")
//  public PersistableCronTriggerFactoryBean databaseMigrationTrigger() throws SchedulerException {
//    PersistableCronTriggerFactoryBean factoryBean = new PersistableCronTriggerFactoryBean();
//    factoryBean.setJobDetail(Objects.requireNonNull(getDatabaseMigrationJobBean().getObject()));
//    factoryBean.setStartDelay(0L);
//    factoryBean.setGroup(groupName());
//    factoryBean.setDescription("Trigger for Business Profile Database migration");
//    factoryBean.setCronExpression(DATABASE_MIGRATION_CRON);
//    factoryBean.setTimeZone(TimeZone.getTimeZone(PHX_TZ));
//    factoryBean.setMisfireInstruction(MISFIRE_INSTRUCTION_SMART_POLICY);
//    return factoryBean;
//  }
//
//  @Bean(name = "DatabaseMigrationJob")
//  public JobDetailFactoryBean getDatabaseMigrationJobBean() throws SchedulerException {
//    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
//    JobKey jobKey = JobKey.jobKey("DatabaseMigrationJob", groupName());
//    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
//    factoryBean.setJobClass(DatabaseMigrationJob.class);
//    factoryBean.setGroup(groupName());
//    factoryBean.setName("DatabaseMigrationJob");
//    factoryBean.setDurability(true);
//    factoryBean.setRequestsRecovery(true);
//    if (jobDetail == null) {
//      Map<String, Object> map = new HashMap<>();
//      map.put("batchOffset", "0");
//      map.put("batchSize", 100000);
//      map.put("offset", 100000);
//      map.put("processing", false);
//      map.put("lastUpdated", 0L);
//      map.put("attempts", 0);
//      factoryBean.setJobDataAsMap(map);
//      factoryBean.afterPropertiesSet();
//    } else {
//      factoryBean.setJobDataAsMap(jobDetail.getJobDataMap());
//    }
//    return factoryBean;
//  }

//  @PostConstruct
//  public void setupJobs() throws Exception {
//    int numberOfPods = environment.getProperty("pod-count", int.class, 10);
//    int totalDocuments = batchProcessUtil.getProfileCount();
//    int batchSize = totalDocuments / numberOfPods;
//
//    for (int i = 0; i < numberOfPods; i++) {
//      String jobKey = "DatabaseMigrationJob_" + i;
//      if (scheduler.checkExists(new JobKey(jobKey))) continue;
//
//      JobDataMap jobDataMap = new JobDataMap();
//      jobDataMap.put("offset", String.valueOf(i * batchSize));
//      jobDataMap.put("batchSize", String.valueOf(batchSize));
//      jobDataMap.put("podIndex", String.valueOf(i));
//
//      JobDetail jobDetail = JobBuilder.newJob(DatabaseMigrationJob.class)
//        .withIdentity(jobKey)
//        .usingJobData(jobDataMap)
//        .storeDurably()
//        .build();
//
//      Trigger trigger = TriggerBuilder.newTrigger()
//        .withIdentity("Trigger_" + jobKey)
//        .forJob(jobDetail)
//        .startNow()
//        .build();
//
//      scheduler.scheduleJob(jobDetail, trigger);
//    }
//  }


//  @Bean(name = "DatabaseMigrationJob")
//  public JobDetailFactoryBean getDatabaseMigrationJobBean() throws SchedulerException {
//    int numberOfPods = environment.getProperty("pod-count", int.class, 10);
//    int totalDocuments = batchProcessUtil.getProfileCount();
//    int batchSize = totalDocuments / numberOfPods;
//
//    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
//    factoryBean.setJobClass(DatabaseMigrationJob.class);
//    factoryBean.setGroup(groupName());
//    factoryBean.setName("DatabaseMigrationJob");
//    factoryBean.setDurability(true);
//    factoryBean.setRequestsRecovery(true);
//
//    for (int i = 0; i < 10; i++) {
//      String jobKey = "DatabaseMigrationJob_" + i;
//      if (scheduler.checkExists(new JobKey(jobKey))) continue;
//
//      JobDataMap jobDataMap = new JobDataMap();
//      jobDataMap.put("type", "initial");
//      jobDataMap.put("offset", String.valueOf(i * batchSize));
//      jobDataMap.put("batchSize", String.valueOf(batchSize));
//      jobDataMap.put("podIndex", String.valueOf(i));
//      jobDataMap.put("processing", false);
//      jobDataMap.put("lastProcessedId", 0L);
//      jobDataMap.put("retryCount", 0);
//
//      JobDetail job = JobBuilder.newJob(DatabaseMigrationJob.class)
//        .withIdentity(jobKey)
//        .withDescription("Initial migration job " + i)
//        .setJobData(jobDataMap)
//        .storeDurably()
//        .build();
//
//      scheduler.addJob(job, true);
//    }
//
//    factoryBean.afterPropertiesSet();
//    return factoryBean;
//  }
//
//  // 2. Incremental Sync Job (runs every 5 minutes)
//  @Bean(name = "IncrementalSyncJob")
//  public JobDetailFactoryBean incrementalSyncJobBean() {
//    JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
//    factoryBean.setJobClass(DatabaseMigrationJob.class);
//    factoryBean.setGroup(groupName());
//    factoryBean.setName("IncrementalSyncJob");
//    factoryBean.setDurability(true);
//
//    JobDataMap map = new JobDataMap();
//    map.put("type", "incremental");
//    map.put("lastRunTime", 0L); // can be updated during runtime
//    factoryBean.setJobDataMap(map);
//
//    return factoryBean;
//  }
//
//  @Bean(name = "incrementalSyncTrigger")
//  public CronTriggerFactoryBean incrementalSyncTrigger(
//    @Qualifier("IncrementalSyncJob") JobDetail jobDetail) {
//    CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
//    trigger.setJobDetail(jobDetail);
//    trigger.setCronExpression("0 0/5 * * * ?"); // Every 5 minutes
//    trigger.setTimeZone(TimeZone.getTimeZone("UTC")); // or your timezone
//    trigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
//    return trigger;
//  }
