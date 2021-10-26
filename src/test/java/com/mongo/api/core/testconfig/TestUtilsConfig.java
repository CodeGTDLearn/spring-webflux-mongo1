package com.mongo.api.core.testconfig;

import config.utils.TestUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestUtilsConfig {

  @Bean
  public TestUtils testUtils() {
    return new TestUtils();
  }
}