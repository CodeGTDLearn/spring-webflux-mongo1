package com.mongo.api.modules.user;

import com.mongo.api.core.dto.UserAllDto;
import com.mongo.api.core.dto.UserDto;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
import com.mongo.api.core.exceptions.globalException.GlobalException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

import static com.mongo.api.core.routes.RoutesError.ERROR_PATH;
import static com.mongo.api.core.routes.RoutesUser.*;
import static org.springframework.http.HttpStatus.*;

// EXCEPTIONS IN CONTROLLER:
// - "Como stream pode ser manipulado por diferentes grupos de thread, caso um erro aconteça em
// uma thread que não é a que operou a controller, o ControllerAdvice não vai ser notificado "
// - https://medium.com/nstech/programa%C3%A7%C3%A3o-reativa-com-spring-boot-webflux-e-mongodb-chega-de-sofrer-f92fb64517c3
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(REQ_USER)
public class UserResource {

  private final IUserService service;

  private final ModelMapper modelMapper;

  private final CustomExceptions customExceptions;

  private final GlobalException globalException;


  @GetMapping(FIND_ALL_USERS)
  @ResponseStatus(OK)
  public Flux<User> findAll() {
    return service
         .findAll()
         .switchIfEmpty(customExceptions.usersNotFoundException());
  }


  @GetMapping(FIND_USER_BY_ID)
  @ResponseStatus(OK)
  public Mono<UserDto> findById(@PathVariable String id) {

    return service
         .findById(id)
         .switchIfEmpty(customExceptions.userNotFoundException())
         .map(userFound -> modelMapper.map(userFound,UserDto.class));
  }


  @GetMapping(FIND_ALL_SHOW_ALL_DTO)
  @ResponseStatus(OK)
  public Flux<UserAllDto> findAllShowAllDto() {
    return service
         .findAllShowAllDto()
         .switchIfEmpty(customExceptions.usersNotFoundException());
  }


  @GetMapping(FIND_ALL_USERS_DTO)
  @ResponseStatus(OK)
  public Flux<UserDto> findAllDto() {
    return service
         .findAll()
         .switchIfEmpty(customExceptions.usersNotFoundException())
         .map(user -> {
           return modelMapper.map(user,UserDto.class);
         });
  }


  @PostMapping
  @ResponseStatus(CREATED)
  public Mono<UserDto> save(@Valid @RequestBody UserDto userDto) {
    // Criar dtoSanitizado (procedimento OWASP de sanitizacao/filter/clean) com excecao
    User user = modelMapper.map(userDto,User.class);

    return service
         .save(user)
         .map(item -> modelMapper.map(item,UserDto.class));
  }


  @DeleteMapping
  @ResponseStatus(NO_CONTENT)
  public Mono<Void> delete(@Valid @RequestBody UserDto userDto) {
    return service
         .findById(userDto.getId())
         .switchIfEmpty(customExceptions.userNotFoundException())
         .flatMap(item -> service.delete(item.getId()));
  }


  @PutMapping
  @ResponseStatus(OK)
  public Mono<User> update(@Valid @RequestBody UserDto userDto) {
    User user = modelMapper.map(userDto,User.class);
    return service
         .findById(user.getId())
         .switchIfEmpty(customExceptions.userNotFoundException())
         .flatMap(userDB -> service.update(user));
  }


  @GetMapping(ERROR_PATH)
  public Flux<User> globalExceptionError() {
    return service
         .findAll()
         .concatWith(globalException.globalErrorException());
  }
}





