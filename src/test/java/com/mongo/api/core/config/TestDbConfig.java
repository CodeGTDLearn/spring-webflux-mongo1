package com.mongo.api.core.config;

import config.utils.TestDbUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestDbConfig {

  @Bean
  public TestDbUtils testDbUtils() {
    return new TestDbUtils();
  }
}