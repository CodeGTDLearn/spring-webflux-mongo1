package testsconfig.testcontainer.compose;

import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;


@Testcontainers
public class ConfigCompose {

  final static public int SERVICE_PORT = 27017;
  final static public String SERVICE_COMPOSE_FILE = "db";
  final static private String COMPOSE_PATH = "src/test/resources/tc-compose.yml";
  //    @Container //Nao anotar aqui. Annotacao deve ficar na classe receptora
  public DockerComposeContainer<?> compose =
       new DockerComposeContainer<>(new File(COMPOSE_PATH))
            .withExposedService(
                 SERVICE_COMPOSE_FILE,
                 SERVICE_PORT,
                 Wait.forListeningPort()
                               );
}




