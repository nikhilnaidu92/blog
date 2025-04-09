package com.aexp.gcs.supplierprofile.scriptsupport.config;

import com.aexp.gcs.supplierprofile.scriptsupport.constants.ScriptSupportConstants;
import com.aexp.gcs.supplierprofile.scriptsupport.scheduler.config.AutowiringSpringBeanJobFactory;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Properties;

import static com.aexp.gcs.supplierprofile.scriptsupport.constants.ScriptSupportConstants.SCHEDULER_NM;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
  basePackages = "com.aexp.gcs.supplierprofile.profilecommons.repository.quartz",
  entityManagerFactoryRef = "quartzEntityManagerFactory",
  transactionManagerRef = "quartzTransactionManager"
)
public class QuartzDatabaseConfig {

  @Autowired
  private QuartzProperties quartzProperties;
  private static final Logger logger = LoggerFactory.getLogger(QuartzDatabaseConfig.class);

  @Bean
  @QuartzDataSource
  @ConfigurationProperties(prefix = "spring.datasource.quartz")
  public DataSource quartzDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  public JobFactory jobFactory(ApplicationContext applicationContext) {
    AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
    jobFactory.setApplicationContext(applicationContext);
    return jobFactory;
  }

  @Bean(name = "quartzSchedulerFactoryBean")
  public SchedulerFactoryBean schedulerFactoryBean(ApplicationContext applicationContext,
                                                   @Qualifier("quartzDataSource") DataSource quartzDataSource,
                                                   @Qualifier("quartzTransactionManager")
                                                   PlatformTransactionManager quartzTransactionManager,
                                                   List<Trigger> listOfTrigger) {
    SchedulerFactoryBean factory = new SchedulerFactoryBean();
    try {
      factory.setDataSource(quartzDataSource);
      factory.setTransactionManager(quartzTransactionManager);
      factory.setOverwriteExistingJobs(true);
      factory.setJobFactory(jobFactory(applicationContext));
      factory.setQuartzProperties(quartzProperties());
      if ((System.getenv(ScriptSupportConstants.EPAAS_ENV) != null
        && System.getenv(ScriptSupportConstants.EPAAS_VERSION) != null)) {
        factory.setSchedulerName(SCHEDULER_NM.concat(System.getenv(ScriptSupportConstants.EPAAS_ENV)).toUpperCase());
      } else {
        factory.setSchedulerName(
          SCHEDULER_NM.concat(InetAddress.getLocalHost().getHostName()).toUpperCase());
      }
      if (!CollectionUtils.isEmpty(listOfTrigger)) {
        factory.setTriggers(listOfTrigger.toArray(new Trigger[0]));
      }

      System.out.println("Quartz Scheduler Name: " + jobFactory(applicationContext).getClass().getName());
    } catch (Exception e) {
      logger.error("Quartz initialization failed due to: {}", e.getMessage());
      throw new RuntimeException(e);
    }
    return factory;
  }

  @Bean
  public Scheduler scheduler(@Qualifier("quartzDataSource") DataSource quartzDataSource) throws SchedulerException {
    Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
    scheduler.getContext().put("quartzDataSource", quartzDataSource);
    scheduler.start();
    return scheduler;
  }

  @Bean(name = "quartzTransactionManager")
  public JpaTransactionManager quartzTransactionManager() {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(quartzEntityManagerFactory().getObject());
    return transactionManager;
  }

  @Bean(name = "quartzEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean quartzEntityManagerFactory() {
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(quartzDataSource());
    em.setPackagesToScan(
      "com.aexp.gcs.supplierprofile.profiledatamodel.domain.database.entity");
    em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    return em;
  }

  @Bean
  public Properties quartzProperties() throws IOException {
    PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
    propertiesFactoryBean.setLocation(new ClassPathResource("/application.properties"));
    Properties props = new Properties();
    props.putAll(quartzProperties.getProperties());
    propertiesFactoryBean.setProperties(props);
    propertiesFactoryBean.afterPropertiesSet();
    return propertiesFactoryBean.getObject();
  }

}
