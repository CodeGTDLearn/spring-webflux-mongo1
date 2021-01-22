package com.mongo.api.core.data.builder;

import com.github.javafaker.Faker;
import com.mongo.api.modules.user.User;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

import java.util.Locale;

@Builder
@Getter
public class UserDatabuilder {

    private final User userDataBuilder;

    private final static Faker faker = new Faker(new Locale("en-BR"));

    @Default
    private static String id;

    @Default
    private static String name;

    @Default
    private static String email;

//    public static UserDatabuilder userFull() {
//        autoGenerateFullUsers();
//
//        User user = new User();
//        user.setId(id);
//        user.setName(name);
//        user.setEmail(email);
//
//        return UserDatabuilder.builder()
//                              .userDataBuilder(user)
//                              .build();
//    }

    public static UserDatabuilder userNullID() {
        autoGenerateUsersWithoutId();

        User user = new User();
        user.setId(null);
        user.setName(name);
        user.setEmail(email);

        return UserDatabuilder.builder()
                              .userDataBuilder(user)
                              .build();
    }

    public static UserDatabuilder userNameParam(String nameUser) {
        autoGenerateUsersWithoutId();

        User user = new User();
        user.setId(null);
        user.setName(nameUser);
        user.setEmail(email);

        return UserDatabuilder.builder()
                              .userDataBuilder(user)
                              .build();
    }


    private static void autoGenerateUsersWithoutId() {
        name = faker.name()
                    .fullName();

        email = faker.internet()
                     .emailAddress();
    }

    private static void autoGenerateFullUsers() {
        id = faker.idNumber()
                  .valid();

        name = faker.name()
                    .fullName();

        email = faker.internet()
                     .emailAddress("gmail");
    }

    public User create() {
        return this.userDataBuilder;
    }
}