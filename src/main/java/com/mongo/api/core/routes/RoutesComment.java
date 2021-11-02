package com.mongo.api.core.routes;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoutesComment {

    public static final String REQ_COMMENT = "comment";
    public static final String FIND_ALL_COMMENTS = "/findAllComments";
    public static final String FIND_ALL_COMMENTS_DTO = "/findAllCommentsDto";
    public static final String FIND_COMMENT_BY_ID = "/{id}";
    public static final String FIND_COMMENTS_BY_POSTID = "/{id}/comments";
    public static final String FIND_COMMENTS_BY_AUTHORID = "/{id}/author";
    public static final String FIND_USER_BY_COMMENTID = "/{id}/user";
    public static final String SAVE_COMMENT_LINKED_OBJECT = "/saveCommentLinkedObject";
    public static final String SAVE_COMMENT_EMBED_OBJECT_SUBST = "/saveCommentEmbedObjectSubst";
    public static final String SAVE_COMMENT_EMBED_OBJECT_LIST = "/saveCommentEmbedObjectList";
}
