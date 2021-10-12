package com.mongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class ApiDriver {

    static {
        BlockHound.install(
                builder -> builder
                        .allowBlockingCallsInside(
                                "java.util.UUID",
                                "randomUUID"
                                                 )
                          );
    }

    public static void main(String[] args) {
//        ReactorDebugAgent.init();
        SpringApplication.run(ApiDriver.class,args);
    }

}
