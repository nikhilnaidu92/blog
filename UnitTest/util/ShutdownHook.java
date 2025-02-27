package com.aexp.commercial.data.util;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class ShutdownHook {

  private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

  private final DataSource appDataSource;
  private final DataSource quartzDataSource;

  public ShutdownHook(@Qualifier("appDataSource") DataSource appDataSource, @Qualifier("quartzDataSource") DataSource quartzDataSource) {
    this.appDataSource = appDataSource;
    this.quartzDataSource = quartzDataSource;
  }

  public void shutdown() {
    logger.warn("ShutdownHook run method started");
    HikariDataSource ds = (HikariDataSource) appDataSource;

    if (!ds.isClosed()) {
      logger.warn("ShutdownHook:closing app datasource");
      ds.close();
      logger.warn("ShutdownHook:closed  app datasource");
    }

    ds = (HikariDataSource) quartzDataSource;
    if (!ds.isClosed()) {
      logger.warn("ShutdownHook:closing quartz datasource");
      ds.close();
      logger.warn("ShutdownHook:closed  quartz datasource");
    }
    logger.warn("ShutdownHook run method completed");
  }
}
