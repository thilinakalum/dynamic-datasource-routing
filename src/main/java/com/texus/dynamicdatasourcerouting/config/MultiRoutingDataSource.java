package com.texus.dynamicdatasourcerouting.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author Thilina Kalum
 * @since 6/5/2021
 */
public class MultiRoutingDataSource extends AbstractRoutingDataSource {

  @Override
  protected Object determineCurrentLookupKey() {
    return com.texus.dynamicdatasourcerouting.config.DataBaseContextHolder.getCurrentDb();
  }
}
