package com.mongo.utils;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

//@Ignore
public class TestContainers {

    static final int DB_PORT = 80;
    static final String COMPOSE_PATH = "src/test/resources/test-compose.yml";
    static final String COMPOSE_DB_SERVICE = "api-db";

    @ClassRule
    public static DockerComposeContainer<?> compose =
            new DockerComposeContainer<>(
                    new File(COMPOSE_PATH))
                    .withExposedService(COMPOSE_DB_SERVICE,DB_PORT);

    public String testContainerDbUrl() {
        return "http://" +
                compose.getServiceHost(COMPOSE_DB_SERVICE,DB_PORT) + ":" +
                compose.getServicePort(COMPOSE_DB_SERVICE,DB_PORT);
    }
}
