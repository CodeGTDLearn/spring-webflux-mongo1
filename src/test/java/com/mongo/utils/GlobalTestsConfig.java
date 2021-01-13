package com.mongo.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.DockerComposeContainer;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

//@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
//@AutoConfigureTestDatabase(replace = Replace.NONE)
//@TestPropertySource("classpath:config-test.properties")
//@TestPropertySource("classpath:application-test.properties")
//@WebFluxTest
@DataMongoTest
@RunWith(SpringRunner.class)
@Slf4j
@Ignore
public class GlobalTestsConfig extends TestContainers {
//public class GlobalTestsConfig  {

    static {
        BlockHound.install();
    }

    @Test
    public void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });

            Schedulers.parallel()
                      .schedule(task);

            task.get(10,TimeUnit.SECONDS);
            Assert.fail("should fail");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            Assert.assertTrue("detected",e.getCause() instanceof BlockingOperationError);
        }
    }

    @BeforeClass
    public static void beforeClass() {
        System.out.println("----- SUPER BEFORE-CLASS -----");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("----- SUPER AFTER-CLASS -----");
    }
}
