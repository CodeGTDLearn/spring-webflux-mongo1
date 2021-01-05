package com.mongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class AppDriver {

    static {
        BlockHound.install();
    }

    public static void main(String[] args) {
        SpringApplication.run(AppDriver.class,args);
    }

}
