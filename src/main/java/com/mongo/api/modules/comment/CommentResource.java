package com.mongo.api.modules.comment;

import com.mongo.api.core.dto.CommentAllDtoFull;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.user.User;
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

    private final CommentServiceInt service;


    @GetMapping(FIND_ALL_COMMENTS)
    @ResponseStatus(OK)
    public Flux<Comment> findAll() {
        return service.findAll();
    }


    @GetMapping(FIND_ALL_COMMENTS_DTO)
    @ResponseStatus(OK)
    public Flux<CommentAllDtoFull> findAllCommentsDto() {
        return service.findAllCommentsDto();
    }


    @GetMapping(FIND_COMMENT_BY_ID)
    @ResponseStatus(OK)
    public Mono<Comment> findCommentById(@PathVariable String id) {
        return service.findById(id);
    }


    @GetMapping(FIND_COMMENTS_BY_POSTID)
    @ResponseStatus(OK)
    public Flux<Comment> findCommentsByPostId(@PathVariable String id) {
        return service.findCommentsByPostId(id);
    }


    @GetMapping(FIND_USER_BY_COMMENTID)
    @ResponseStatus(OK)
    public Mono<User> findUserByCommentId(@PathVariable String id) {
        return service.findUserByCommentId(id);
    }


    @PostMapping(SAVE_COMMENT_LINKED_OBJECT)
    @ResponseStatus(CREATED)
    public Mono<Comment> saveCommentLinkedObject(@RequestBody Comment comment) {
        return service.saveLinkedObject(comment);
    }


    @PostMapping(SAVE_COMMENT_EMBED_OBJECT_SUBST)
    @ResponseStatus(CREATED)
    public Mono<Post> saveCommentEmbedObjectSubst(@RequestBody Comment comment) {
        return service.saveEmbedObjectSubst(comment);
    }


    @PostMapping(SAVE_COMMENT_EMBED_OBJECT_LIST)
    @ResponseStatus(CREATED)
    public Mono<Post> saveCommentEmbedObjectList(@RequestBody Comment comment) {
        return service.saveEmbedObjectList(comment);
    }


    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> delete(@RequestBody Comment comment) {
        return service.delete(comment);
    }


    @PutMapping
    @ResponseStatus(OK)
    public Mono<Comment> update(@RequestBody Comment comment) {
        return service.update(comment);
    }

}
