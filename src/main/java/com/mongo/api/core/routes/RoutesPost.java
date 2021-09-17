package com.mongo.api.core.routes;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoutesPost {

    public static final String REQ_POST = "/post";
    public static final String FIND_ALL_POSTS = "/findAllPosts";
    public static final String FIND_POST_BY_ID = "/{id}";
    public static final String FIND_POST_BY_ID_SHOW_COMMENTS = "showcomments/{id}";
    public static final String FIND_POSTS_BY_USERID = "/{id}/posts";
    public static final String SAVE_EMBED_USER_IN_THE_POST = "/saveEmbedObject";

}
