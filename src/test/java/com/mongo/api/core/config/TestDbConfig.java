package com.mongo.api.core.config;

import config.utils.DbUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestDbConfig {

  @Bean
  public DbUtils userDbUtils() {
    return new DbUtils();
  }
}