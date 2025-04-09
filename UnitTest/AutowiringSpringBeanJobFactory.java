package com.aexp.gcs.supplierprofile.scriptsupport.scheduler.config;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

public final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {
  private AutowireCapableBeanFactory beanFactory;

  @Override
  public void setApplicationContext(final ApplicationContext context) {
    this.beanFactory = context.getAutowireCapableBeanFactory();
  }

  @Override
  protected Object createJobInstance(final TriggerFiredBundle triggerFiredBundle) throws Exception {
    final Object OBJECT = super.createJobInstance(triggerFiredBundle);
    System.out.println("autowiring job.." + OBJECT.getClass().getName());
    beanFactory.autowireBean(OBJECT);
    return OBJECT;
  }

}
