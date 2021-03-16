package com.mongo.api.modules.user;

import com.mongo.utils.GlobalTestsConfig;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.mongo.api.core.data.builder.UserDatabuilder.userNullID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserRepoSimpleIntegrationTest extends GlobalTestsConfig {

    @Autowired
    private UserRepo repo;

    User userToBeSaved = new User();
    User user2 = new User();

    @ClassRule
    public static DockerComposeContainer environment =
            new DockerComposeContainer(new File("src/test/resources/test-compose.yml"))
                    .withExposedService("db",27017,
                                        Wait.forListeningPort()
                                            .withStartupTimeout(
                                                    Duration.ofSeconds(30))
                                       );

    //    @BeforeAll
    //    static void initAll() {
    //        environment.start();
    //    }

    @BeforeEach
    void beforeEach() {
        System.out.println("------------- USER-REPO-TEST ----BEFORE-EACH");
        environment.start();

        super.bHWorks();
        userToBeSaved = userNullID().create();
        user2 = userNullID().create();

        List<User> list = Arrays.asList(userToBeSaved,user2);

        repo.deleteAll()
                .thenMany(Flux.fromIterable(list))
                .flatMap(repo::save)
                .doOnNext((item -> System.out.println(" User item is: " + item)))
                .subscribe();
        //            .blockLast(); // THATS THE WHY, BLOCKHOUND IS NOT BEING USED.
    }

    @AfterEach
    void afterEach() {
        System.out.println("------------- USER-REPO-TEST ----AFTER-EACH");
    }

    @Test
    void saveUser() {
        userToBeSaved = userNullID().create();
        User userReturned = repo.save(userToBeSaved)
                                .block();
        assert userReturned != null;
        assertEquals(userToBeSaved.getName(),userReturned.getName());
    }
}

//    @Test
//    public void findAll() {
//        StepVerifier
//                .create(userRepo.findAll())
//                .expectSubscription()
//                .expectNextCount(2)
//                .verifyComplete();
//    }
