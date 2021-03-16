package com.mongo.utils;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.springframework.boot.test.context.SpringBootTest.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

//@AutoConfigureTestDatabase(replace = Replace.NONE)
//@TestPropertySource("classpath:config-test.properties")
//@TestPropertySource("classpath:application-test.properties")
//@WebFluxTest
//@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
//@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
//@ExtendWith(SpringExtension.class)
//@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
//@ActiveProfiles("test")
//@Disabled
//@TestPropertySource(locations = "classpath:application-test.properties")
//@Testcontainers
@SpringBootTest
@Slf4j
public class GlobalTestsConfig {

    @BeforeAll
    public static void beforeClass() {
        System.out.println("------GLOBAL----BEFORECLASS-----------");
        BlockHound.install(
                //builder -> builder.allowBlockingCallsInside("java.util.UUID" ,"randomUUID")
                          );
    }

    @AfterAll
    public static void afterClass() {
        System.out.println("------GLOBAL----AFTERCLASS-------------");
    }

    @Test
    public void bHWorks() {
        System.out.println("------GLOBAL----BHOUND: START--------");
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });

            Schedulers.parallel()
                      .schedule(task);

            task.get(10,TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
            //            Assert.assertTrue("detected",e.getCause() instanceof
            //            BlockingOperationError);
        }
        System.out.println("------GLOBAL----BLOCKHOUND: ENDING-------");
    }
}

