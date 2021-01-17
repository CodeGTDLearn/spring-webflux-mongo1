package com.mongo.api.modules.user;

import com.mongo.api.core.data.builder.UserDatabuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


//@SpringBootTest
@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class UserRepoTest {

    @Container
    static MongoDBContainer container =
            new MongoDBContainer("mongo:4.4.2");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri",container::getReplicaSetUrl);
    }

    @Autowired
    private UserRepo userRepo;

    @BeforeEach
    void setUp() {
        container.start();
    }

    @AfterEach
    void cleanUp() {
        userRepo.deleteAll();
        container.stop();
    }

    @Test
    void findAllUsers() {
        this.userRepo.save(UserDatabuilder.userNullID().create());
        this.userRepo.save(UserDatabuilder.userNullID().create());

        StepVerifier
                .create(userRepo.findAll())
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
    }
}
