package com.mongo.api.core.routes;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoutesPost {

    public static final String REQ_POST = "/postsss";
    public static final String FIND_ALL_POSTS = "/findAllPosts";
    public static final String FIND_POST_BY_ID = "/{id}";
    public static final String FIND_POST_BY_ID_SHOW_COMMENTS = "/showcomments/{id}";
    public static final String FIND_POSTS_BY_AUTHORID = "/{id}/posts";
    public static final String SAVE_EMBED_USER_IN_THE_POST = "/saveEmbedObject";
    public static final String FIND_USER_BY_POSTID = "/{id}/user";

}