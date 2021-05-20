package com.mongo.api.modules.post;

import com.mongo.api.core.dto.PostDto;
import com.mongo.api.core.dto.PostDtoComments;
import com.mongo.api.modules.user.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mongo.api.core.Routes.*;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(REQ_POST)
public class PostResource {

    private final PostServiceInt postServiceInt;

    @Autowired
    private final ModelMapper converter;


    @GetMapping(FIND_ALL_POSTS)
    @ResponseStatus(OK)
    public Flux<Post> findAll() {
        return postServiceInt.findAll();
    }


    @GetMapping(FIND_POST_BY_ID)
    @ResponseStatus(OK)
    public Mono<PostDto> findPostById(@PathVariable String id) {
        return postServiceInt
                .findById(id)
                .map(post -> converter.map(post,PostDto.class));
    }


    @GetMapping(FIND_POST_BY_ID_SHOW_COMMENTS)
    @ResponseStatus(OK)
    public Mono<PostDtoComments> findPostByIdShowComments(@PathVariable String id) {
        return postServiceInt.findPostByIdShowComments(id)
                             .map(item -> converter.map(item,PostDtoComments.class));
    }


    @PostMapping(SAVE_EMBED_USER_IN_THE_POST)
    @ResponseStatus(CREATED)
    public Mono<Post> saveEmbedObject(@RequestBody Post post) {
        return postServiceInt.save(post);
    }


    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> delete(@RequestBody Post post) {
        return postServiceInt.delete(post);
    }


    @PutMapping
    @ResponseStatus(OK)
    public Mono<Post> update(@RequestBody Post post) {
        return postServiceInt.update(post);
    }


    @GetMapping(FIND_USER_BY_POSTID)
    @ResponseStatus(OK)
    public Mono<User> findUserByPostId(@PathVariable String id) {
        return postServiceInt.findUserByPostId(id);
    }
}
