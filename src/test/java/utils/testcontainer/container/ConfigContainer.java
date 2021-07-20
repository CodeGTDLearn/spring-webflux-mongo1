package utils.testcontainer.container;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class ConfigContainer {

    /* SPEED UP 01: TEST-CONTAINERS
        Containers declared as STATIC fields will be shared between test methods,
        starting only once before any test method is executed and
        stopped after the last test method has executed.
        Containers declared as INSTANCE fields will be started and stopped for every test method.
     */
    @Container
    public static final MongoDBContainer sharedContainer = new MongoDBContainer("mongo:4.4.2");


    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri",sharedContainer::getReplicaSetUrl);
    }

}




