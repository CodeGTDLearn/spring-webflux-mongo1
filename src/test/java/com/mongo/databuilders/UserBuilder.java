package com.mongo.databuilders;

import com.github.javafaker.Faker;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Builder
@Getter
public class UserBuilder {

    private final User user;

    private static final Faker faker = new Faker(new Locale("en-CA.yml"));
    //    private static String FAKER_REGEX_CPF = "[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2}";
    //    private static String FAKER_REGEX_DDD = "[0-9]{2}";
    //    private static String FAKER_REGEX_TEL = "[0-9]{9}";


    public static UserBuilder userFull_withoutID(List<Post> userListPosts) {
        List<String> idPosts = new ArrayList<>();

        User userFull = new User();
//        userFull.setId(faker.regexify("/^[a-f\\d]{24}$/i"));
        userFull.setName(faker.name()
                              .fullName());
        userFull.setEmail(faker.internet()
                               .emailAddress());

        for (Post post : userListPosts) {
            idPosts.add(post.getId());
        }

        userFull.setIdPosts(idPosts);

        return UserBuilder.builder()
                          .user(userFull)
                          .build();
    }

    public static UserBuilder userWithID_ListIdPostsEmpty() {
        User userWithID_ListIdPostsEmpty = new User();
        userWithID_ListIdPostsEmpty.setId(faker.regexify("/^[a-f\\d]{24}$/i"));
        userWithID_ListIdPostsEmpty.setName(faker.name()
                              .fullName());
        userWithID_ListIdPostsEmpty.setEmail(faker.internet()
                               .emailAddress());

        return UserBuilder.builder()
                          .user(userWithID_ListIdPostsEmpty)
                          .build();
    }

    public static UserBuilder userFull_IdNull_ListIdPostsEmpty() {
//        List<String> idPosts = new ArrayList<>();

        User userFull_IdNull_ListIdPostsEmpty = new User();
//        userFull_IdNull_ListIdPostsEmpty.setId(null);
        userFull_IdNull_ListIdPostsEmpty.setName(faker.name()
                                .fullName());
        userFull_IdNull_ListIdPostsEmpty.setEmail(faker.internet()
                                 .emailAddress());
//        userFull_IdNull_ListIdPostsEmpty.setIdPosts(idPosts);
        return UserBuilder.builder()
                          .user(userFull_IdNull_ListIdPostsEmpty)
                          .build();
    }


    public static UserBuilder userFull_Id_ListIdPostsEmpty() {
        //        List<String> idPosts = new ArrayList<>();

        User userFull_IdNull_ListIdPostsEmpty = new User();
        //        userFull_IdNull_ListIdPostsEmpty.setId(null);
        userFull_IdNull_ListIdPostsEmpty.setName(faker.name()
                                                      .fullName());
        userFull_IdNull_ListIdPostsEmpty.setEmail(faker.internet()
                                                       .emailAddress());
        //        userFull_IdNull_ListIdPostsEmpty.setIdPosts(idPosts);
        return UserBuilder.builder()
                          .user(userFull_IdNull_ListIdPostsEmpty)
                          .build();
    }

    public User create() {
        return this.user;
    }
}
