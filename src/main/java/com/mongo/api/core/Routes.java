package com.mongo.api.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Routes {

    public static final String REQ_USER = "/user";
    public static final String FIND_ALL_USERS = "/findAllUsers";
    public static final String FIND_ALL_USERS_DTO = "/findAllUsersDto";
    public static final String FIND_USER_BY_ID = "/{id}";
    public static final String FIND_USER_BY_POSTID = "/{id}/user";
    public static final String SAVE_LINKED_POST_IN_THE_USER = "/saveLinkedObject";

    public static final String REQ_POST = "/post";
    public static final String FIND_ALL_POSTS = "/findAllPosts";
    public static final String FIND_POST_BY_ID = "/{id}";
    public static final String FIND_POST_BY_ID_SHOW_COMMENTS = "showcomments/{id}";
    public static final String FIND_POSTS_BY_USERID = "/{userId}/posts";
    public static final String SAVE_EMBED_USER_IN_THE_POST = "/saveEmbedObject";

    public static final String REQ_COMMENT = "comment";
    public static final String FIND_ALL_COMMENTS = "/findAllComments";
    public static final String FIND_COMMENT_BY_ID = "/{id}";
    public static final String FIND_COMMENTS_BY_POSTID = "/{id}/comments";
    public static final String FIND_USER_BY_COMMENTID = "/{id}/user";
    public static final String SAVE_COMMENT_LINKED_OBJECT = "/saveCommentLinkedObject";
    public static final String SAVE_COMMENT_EMBED_OBJECT = "/saveCommentEmbedObject";
    public static final String SAVE_COMMENT_EMBED_OBJECT_LIST = "/saveCommentEmbedObjectList";

    public static final String ERROR_PATH = "/error";
}
