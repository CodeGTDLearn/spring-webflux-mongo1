package com.mongo.api.core.data.mass;

import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.IPostRepo;
import com.mongo.api.modules.user.User;
import com.mongo.api.modules.user.IUserRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.mongo.api.core.data.builder.PostDatabuilder.postNullIdAuthorParam;
import static com.mongo.api.core.data.builder.UserDatabuilder.userNameParam;

@Slf4j
@AllArgsConstructor
@Configuration
public class EmbedRelationship implements CommandLineRunner {

    private final IUserRepo userRepo;
    private final IPostRepo postRepo;

    @Override
    public void run(String... args) {

        User melissa = userNameParam("melissa").create();
        User tony = userNameParam("tony").create();
        User lucas = userNameParam("lucas").create();

        Flux<User> userFlux = Flux.fromIterable(List.of(melissa,tony,lucas));

        //todo: 01 - BLOCKING CODE PORTION
        userRepo
                .deleteAll()
                .thenMany(userFlux)
                .flatMap(userRepo::save)
                .flatMap(user -> {
                    Post post = postNullIdAuthorParam(user).create();
                    return postRepo.save(post);
                });
//                .subscribe();

    }

}