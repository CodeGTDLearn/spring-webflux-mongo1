package com.mongo.api.core.data.mass;

import com.mongo.api.core.data.builder.UserDatabuilder;
import com.mongo.api.modules.user.User;
import com.mongo.api.modules.user.IUserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Slf4j
//@Component
//@Profile("!test")
public class NoRelationship implements CommandLineRunner {

    @Autowired
    IUserRepo userRepo;

    @Override
    public void run(String... args) throws Exception {

        initialDataSetUp();
    }

    public List<User> data() {
        return Arrays.asList(UserDatabuilder.userNullID().create(),
                             UserDatabuilder.userNullID().create(),
                             UserDatabuilder.userNullID().create(),
                             UserDatabuilder.userNullID().create()
                            );
    }

    private void initialDataSetUp() {
        //todo: 02 - BLOCKING CODE PORTION
        userRepo
                .deleteAll()
                .thenMany(Flux.fromIterable(data()))
                .flatMap(userRepo::save)
                .thenMany(userRepo.findAll())
                .subscribe((item -> {
                    System.out.println(
                            "Item inserted from CommandLineRunner : " + item);
                }));
    }
}
