package com.mongo.utils;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
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

import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

//@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
//@AutoConfigureTestDatabase(replace = Replace.NONE)
//@TestPropertySource("classpath:config-test.properties")
//@TestPropertySource("classpath:application-test.properties")
//@WebFluxTest
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ExtendWith(SpringExtension.class)
@Slf4j
@Disabled
public class GlobalTestsConfig {

    @BeforeAll
    public static void beforeClass() {
        System.out.println("----------------BEFORECLASS-----------");
        BlockHound.install(
                //builder -> builder.allowBlockingCallsInside("java.util.UUID" ,"randomUUID")
                          );
    }

    @AfterAll
    public static void afterClass()  {
        System.out.println("----------------AFTERCLASS-------------");
    }

    @Test
    public void blockHoundWorks() {
        System.out.println("------GLOBAL----BLOCKHOUND: START--------");
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

