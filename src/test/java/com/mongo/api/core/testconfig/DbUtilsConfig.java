package com.mongo.api.core.testconfig;

import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.user.User;
import config.utils.DbUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class DbUtilsConfig {

  @Bean
  public DbUtils<User> userDbUtils() {
    return new DbUtils<>();
  }


  @Bean
  public DbUtils<Post> postDbUtils() {
    return new DbUtils<>();
  }
}