package com.mongo.api.modules.user;

import com.mongo.utils.GlobalTestsConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static com.mongo.api.core.data.builder.UserDatabuilder.userNullID;

public class UserRepoTest extends GlobalTestsConfig {

    @Autowired
    private UserRepo userRepo;

    User user1 = new User();
    User user2 = new User();

    @Container
    private static final MongoDBContainer db =
            new MongoDBContainer("mongo:4.2.8");

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        System.out.println(
                "uri: " + db.getReplicaSetUrl("pauleta") + "\n" +
                        "port: " + db.getExposedPorts()
                                     .toString() + "\n" +
                        "host: " + db.getHost() + "\n" +
                        "created: " + db.isCreated() + "\n" +
                        "getBinds: " + db.getBinds()
                                         .toString() + "\n" +
                        "container-name: " + db.getContainerName() + "\n" +
                        "folder: " + db.getWorkingDirectory() + "\n" +
                        "image: " + db.getDockerImageName());

        registry.add("spring.data.mongodb.uri",db::getReplicaSetUrl);
        //        registry.add("spring.data.mongodb.database",db::getReplicaSetUrl);
        registry.add("spring.data.mongodb.host",db::getHost);
        registry.add("spring.data.mongodb.port",db::getExposedPorts);
        //        registry.add("spring.data.mongodb.username",container::getDockerImageName);
        registry.add("spring.data.mongodb.authentication-database",() -> "admin");
    }

    @BeforeEach
    void beforeEach() {
        System.out.println("------------- USER-REPO-TEST ----BEFORE-EACH");
        super.blockHoundWorks();
        user1 = userNullID().create();
        user2 = userNullID().create();

        List<User> list = Arrays.asList(user1,user2);

        userRepo.deleteAll()
                .thenMany(Flux.fromIterable(list))
                .flatMap(userRepo::save)
                .doOnNext((item -> System.out.println(" User item is: " + item)))
                .subscribe();
        //            .blockLast(); // THATS THE WHY, BLOCKHOUND IS NOT BEING USED.
    }

    @AfterEach
    void afterEach() {
        System.out.println("------------- USER-REPO-TEST ----AFTER-EACH");
    }

    @Test
    public void findAll() {
        StepVerifier
                .create(userRepo.findAll())
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
    }

}
