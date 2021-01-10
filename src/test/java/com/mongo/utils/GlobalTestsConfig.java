package com.mongo.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@DataMongoTest
@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
//@AutoConfigureTestDatabase(replace = Replace.NONE)
//@TestPropertySource("classpath:config-test.properties")
//@TestPropertySource("classpath:application-test.properties")
//@WebFluxTest
@Slf4j
@Ignore
public class GlobalTestsConfig {



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

    @Before
    public void setUp() {
        System.out.println("----- SUPER SETUP -----");
    }

    @After
    public void tearDown() {
        System.out.println("----- SUPER TEARDOWN -----");
    }
}
