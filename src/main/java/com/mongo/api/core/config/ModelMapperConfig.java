package com.mongo.api.core.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }
}

//    @Bean
//    public ModelMapper modelMapper() {
//
//        ModelMapper mapper = new ModelMapper();
//
//        Condition<User, UserAllDto>
//                useralldtoPostsGetZeroIsString =
//                ctx -> ctx.getSource()
//                           .getIdPosts().get(0).getClass().equals(String.class);
//
//        mapper
//                .createTypeMap(User.class,UserAllDto.class)
//                .addMappings(map -> map
//                        .when(useralldtoPostsGetZeroIsString)
//                        .skip(UserAllDto::setPosts));
//
//        return mapper;
//    }
