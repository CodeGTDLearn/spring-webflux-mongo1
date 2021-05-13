package com.mongo.api.core.data.builder;

import com.github.javafaker.Faker;
import com.mongo.api.modules.post.entity.Post;
import com.mongo.api.modules.user.entity.AuthorDto;
import com.mongo.api.modules.user.entity.User;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Builder
@Getter
public class PostDatabuilder {

    private final Post postDataBuilder;

    private final static Faker faker = new Faker(new Locale("en-BR"));

    @Default
    private static String id;

    @Default
    private static Date date;

    @Default
    private static String title;

    @Default
    private static String body;

    @Default
    private static User author;


    @SneakyThrows
    public static PostDatabuilder postNullIdAuthorParam(User user) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        Post post = new Post();
        post.setId(null);
        post.setDate(sdf.parse("11/11/2011"));
        post.setTitle(faker.address()
                           .cityName());
        post.setBody(faker.address()
                          .city());
        post.setAuthor(new AuthorDto(user));

        return PostDatabuilder.builder()
                              .postDataBuilder(post)
                              .build();
    }


    public Post create() {
        return this.postDataBuilder;
    }
}
