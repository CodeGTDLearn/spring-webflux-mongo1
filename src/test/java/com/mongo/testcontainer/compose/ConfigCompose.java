package com.mongo.testcontainer.compose;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

/*------------------------------------------------------------
                         DataMongoTest
  ------------------------------------------------------------
a) AMBOS FUNCIONAM:
 - @DataMongoTest
 - @DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
b) USO ALTERNATIVO (DataMongoTest/SpringBootTest) - CONFLITAM ENTRE-SI:
 - @SpringBootTest(webEnvironment = RANDOM_PORT)
  ------------------------------------------------------------*/
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@Slf4j
@Testcontainers
public class ConfigCompose {

    final private String COMPOSE_PATH = "src/test/resources/compose-testcontainers.yml";
    final static public int SERVICE_PORT = 27017;
    final static public String SERVICE = "db";


    @Container //Nao anotar aqui. Annotacao deve ficar na classe receptora
    public DockerComposeContainer<?> compose =
            new DockerComposeContainer<>(new File(COMPOSE_PATH))
                    .withExposedService(
                            SERVICE,
                            SERVICE_PORT
                                       );
}




