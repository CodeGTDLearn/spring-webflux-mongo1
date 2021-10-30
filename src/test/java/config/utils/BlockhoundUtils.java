package config.utils;

import lombok.NoArgsConstructor;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.springframework.test.annotation.DirtiesContext;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.blockhound.integration.BlockHoundIntegration;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
@Ignore
@NoArgsConstructor
public class BlockhoundUtils {

  //EXCECOES DE METODOS BLOQUEANTES DETECTADOS PELO BLOCKHOUND:
  static BlockHoundIntegration AllowedCalls =
       builder -> builder
            .allowBlockingCallsInside("java.io.PrintStream",
                                      "write"
                                     )
            .allowBlockingCallsInside("java.io.FileOutputStream",
                                      "writeBytes"
                                     )
            .allowBlockingCallsInside("java.io.PrintStream",
                                      "write"
                                     )
            .allowBlockingCallsInside("java.io.FileOutputStream",
                                      "writeBytes"
                                     )
            .allowBlockingCallsInside("java.io.BufferedOutputStream",
                                      "flushBuffer"
                                     )
            .allowBlockingCallsInside("java.io.BufferedOutputStream",
                                      "flush"
                                     )
            .allowBlockingCallsInside("java.io.OutputStreamWriter",
                                      "flushBuffer"
                                     )
            .allowBlockingCallsInside("java.io.PrintStream",
                                      "print"
                                     )
            .allowBlockingCallsInside("java.io.PrintStream",
                                      "println"
                                     );


   public static void blockhoundInstallAllowAllCalls() {
    BlockHound.install(AllowedCalls);
  }


  public static void blockhoundInstallSimple() {
    BlockHoundIntegration allowedCalls =
         builder -> builder
              .allowBlockingCallsInside("java.io.PrintStream",
                                        "write"
                                       );
    BlockHound.install(allowedCalls);
  }

  public static void bhWorks() {
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
      Assertions.assertTrue(e.getCause() instanceof BlockingOperationError,"detected");
    }
  }

}
