package testsconfig.utils;

import lombok.NoArgsConstructor;
import org.junit.Ignore;
import org.springframework.test.annotation.DirtiesContext;
import reactor.blockhound.BlockHound;
import reactor.blockhound.integration.BlockHoundIntegration;

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

}
