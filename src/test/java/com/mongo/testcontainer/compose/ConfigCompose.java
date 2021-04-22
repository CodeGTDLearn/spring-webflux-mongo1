package com.mongo.testcontainer.compose;

import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;


@Testcontainers
public class ConfigCompose {

    final static private String COMPOSE_PATH = "src/test/resources/compose-testcontainers.yml";
    final static public int SERVICE_PORT = 27017;
    final static public String SERVICE = "db";


    //    @Container //Nao anotar aqui. Annotacao deve ficar na classe receptora
    public DockerComposeContainer<?> compose =
            new DockerComposeContainer<>(new File(COMPOSE_PATH))
                    .withExposedService(
                            SERVICE,
                            SERVICE_PORT,
                            Wait.forListeningPort()
                                       );
}




