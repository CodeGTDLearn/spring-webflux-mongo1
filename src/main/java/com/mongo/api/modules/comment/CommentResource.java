package com.mongo.api.modules.comment;

import com.mongo.api.modules.post.PostServiceInt;
import com.mongo.api.modules.post.entity.Post;
import com.mongo.api.modules.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mongo.api.core.Routes.*;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(REQ_COMMENT)
public class CommentResource {

    private final PostServiceInt postServiceInt;

    private final CommentService commentService;

    @GetMapping(FIND_ALL_COMMENTS)
    @ResponseStatus(OK)
    public Flux<Comment> findAll() {
        return commentService.findAll();
    }

    @GetMapping(FIND_COMMENT_BY_ID)
    @ResponseStatus(OK)
    public Mono<Comment> findCommentById(@PathVariable String id) {
        return commentService.findCommentById(id);
    }

    @GetMapping(FIND_COMMENTS_BY_POSTID)
    @ResponseStatus(OK)
    public Flux<Comment> findCommentsByPostId(@PathVariable String id) {
        return commentService.findCommentsByPostId(id);
    }

    @GetMapping(FIND_USER_BY_COMMENTID)
    @ResponseStatus(OK)
    public Mono<User> findUserByCommentId(@PathVariable String id) {
        return commentService.findUserByCommentId(id);
    }

    @PostMapping(SAVE_COMMENT_LINKED_OBJECT)
    @ResponseStatus(CREATED)
    public Mono<Post> saveCommentLinkedObject(@RequestBody Comment comment) {
        return commentService.saveCommentLinkedObject(comment);
    }

    @PostMapping(SAVE_COMMENT_EMBED_OBJECT)
    @ResponseStatus(CREATED)
    public Mono<Post> saveCommentEmbedObject(@RequestBody Comment comment) {
        return commentService.saveCommentEmbedObject(comment);
    }

    @PostMapping(SAVE_COMMENT_EMBED_OBJECT_LIST)
    @ResponseStatus(CREATED)
    public Mono<Post> saveCommentEmbedObjectList(@RequestBody Comment comment) {
        return commentService.saveCommentEmbedObjectList(comment);
    }

    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> delete(@RequestBody Comment comment) {
        return commentService.delete(comment);
    }

    @PutMapping
    @ResponseStatus(OK)
    public Mono<Comment> update(@RequestBody Comment comment) {
        return commentService.update(comment);
    }

}
