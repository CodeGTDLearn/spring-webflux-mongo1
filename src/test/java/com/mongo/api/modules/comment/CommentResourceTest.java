package com.mongo.api.modules.comment;//package com.mongo.api.modules.comment;

import com.github.javafaker.Faker;
import com.mongo.api.core.config.TestDbConfig;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptionsProperties;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.user.User;
import config.annotations.MergedResource;
import config.testcontainer.TcComposeConfig;
import config.utils.TestDbUtils;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;

import static com.mongo.api.core.routes.RoutesComment.*;
import static config.databuilders.CommentBuilder.commentNoId;
import static config.databuilders.PostBuilder.postFull_withId_CommentsEmpty;
import static config.databuilders.UserBuilder.userWithID_IdPostsEmpty;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.RestAssureSpecs.*;
import static config.utils.TestUtils.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.*;

@Import({
     TestDbConfig.class,
     //     PostService.class,
     //     UserService.class,
     //     CommentService.class,
     //     CustomExceptions.class,
     //     CustomExceptionsProperties.class,
     //     ModelMapper.class,
})
@DisplayName("CommentResourceTest")
@MergedResource
class CommentResourceTest {

  // STATIC-@Container: one service for ALL tests -> SUPER FASTER
  // NON-STATIC-@Container: one service for EACH test
  @Container
  private static final DockerComposeContainer<?>
       compose = new TcComposeConfig().getTcCompose();

  final String enabledTest = "true";

  // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
  // SHOULD BE USED WITH 'TEST-CONTAINERS'
  // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
  @Autowired
  WebTestClient mockedWebClient;

  private Comment comment1;
  private Post commentedPost;
  private User userCommentAuthor, userPostAuthor;

  private final String invalidId = Faker.instance()
                                        .idNumber()
                                        .valid();

  @Autowired
  CustomExceptionsProperties customExceptions;

  @Autowired
  TestDbUtils dbUtils;

  @Autowired
  ICommentService commentService;


  @BeforeAll
  static void beforeAll(TestInfo testInfo) {
    globalBeforeAll();
    globalTestMessage(testInfo.getDisplayName(),"class-start");
    globalComposeServiceContainerMessage(compose,
                                         TC_COMPOSE_SERVICE,
                                         TC_COMPOSE_SERVICE_PORT
                                        );

    RestAssuredWebTestClient.reset();
    RestAssuredWebTestClient.requestSpecification =
         requestSpecsSetPath("http://localhost:8080/" + REQ_COMMENT);

    RestAssuredWebTestClient.responseSpecification = responseSpecs();
  }


  @AfterAll
  static void afterAll(TestInfo testInfo) {
    globalTestMessage(testInfo.getDisplayName(),"class-end");
  }


  @BeforeEach
  void beforeEach(TestInfo testInfo) {
    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-start");

    userCommentAuthor = userWithID_IdPostsEmpty().create();
    userPostAuthor = userWithID_IdPostsEmpty().create();
    Flux<User> userFlux =
         dbUtils.saveUserList(asList(userCommentAuthor,userPostAuthor));
    dbUtils.countAndExecuteUserFlux(userFlux,2);


    commentedPost = postFull_withId_CommentsEmpty(userPostAuthor).create();
    Flux<Post> postFlux = dbUtils.savePostList(singletonList(commentedPost));
    dbUtils.countAndExecutePostFlux(postFlux,1);

    comment1 = commentNoId(userCommentAuthor,commentedPost).create();
    Flux<Comment> commentFlux = dbUtils.saveLinked(singletonList(comment1));
    dbUtils.countAndExecuteCommentFlux(commentFlux,1);

  }


  @AfterEach
  void tearDown(TestInfo testInfo) {
    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-end");
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindAll")
  void FindAll() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(FIND_ALL_COMMENTS)

         .then()
         .log()
         .everything()

         .statusCode(OK.value())
         .body("size()",is(1))
         .body("commentId",hasItem(comment1.getCommentId()))
         .body("postId",hasItem(comment1.getPostId()))
         .body("author.id",hasItem(userCommentAuthor.getId()))
         .body(matchesJsonSchemaInClasspath("contracts/comment/findAll.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindAllCommentsDto")
  void FindAllCommentsDto() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(FIND_ALL_COMMENTS_DTO)

         .then()
         .log()
         .everything()

         .statusCode(OK.value())
         .body("size()",is(1))
         .body("commentId",hasItem(comment1.getCommentId()))
         .body("postId",hasItem(comment1.getPostId()))
         .body("text",hasItem(comment1.getText()))
         .body("post.id",hasItem(commentedPost.getPostId()))
         .body(matchesJsonSchemaInClasspath("contracts/comment/findAllCommentsDto.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindById")
  void FindById() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(FIND_COMMENT_BY_ID,comment1.getCommentId())

         .then()
         .log()
         .everything()

         .statusCode(OK.value())
         .body("commentId",containsString(comment1.getCommentId()))
         .body("postId",containsString(commentedPost.getPostId()))
         .body("author.id",containsString(userCommentAuthor.getId()))
         .body(matchesJsonSchemaInClasspath("contracts/comment/findById.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindUserByCommentId")
  void FindUserByCommentId() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(FIND_USER_BY_COMMENTID,comment1.getCommentId())

         .then()
         .log()
         .everything()

         .statusCode(OK.value())
         .body("id",containsString(userCommentAuthor.getId()))
         .body("name",containsString(userCommentAuthor.getName()))
         .body(matchesJsonSchemaInClasspath("contracts/comment/findUserByCommentId.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("findCommentsByAuthorIdV1")
  void findCommentsByAuthorIdV1() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(FIND_COMMENTS_BY_AUTHORIDV1, userCommentAuthor.getId())

         .then()
         .log()
         .everything()

         .statusCode(OK.value())
         .body("commentId",hasItem(comment1.getCommentId()))
         .body("postId",hasItem(commentedPost.getPostId()))
         .body(matchesJsonSchemaInClasspath("contracts/comment/findCommentsByAuthorId.json"))
    ;
  }

  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("findCommentsByAuthor_IdV2")
  void findCommentsByAuthor_IdV2() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(FIND_COMMENTS_BY_AUTHORIDV2, userCommentAuthor.getId())

         .then()
         .log()
         .everything()

         .statusCode(OK.value())
         .body("commentId",hasItem(comment1.getCommentId()))
         .body("postId",hasItem(commentedPost.getPostId()))
         .body(matchesJsonSchemaInClasspath("contracts/comment/findCommentsByAuthorId.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindCommentsByPostId")
  void FindCommentsByPostId() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(FIND_COMMENTS_BY_POSTID,commentedPost.getPostId())

         .then()
         .log()
         .everything()

         .statusCode(OK.value())
         .body("commentId",hasItem(comment1.getCommentId()))
         .body("postId",hasItem(commentedPost.getPostId()))
         .body(matchesJsonSchemaInClasspath("contracts/comment/findCommentsByPostId.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("SaveLinked")
  void SaveLinked() {
    Comment comment2 = commentNoId(userCommentAuthor,commentedPost).create();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(comment2)

         .when()
         .post(SAVE_COMMENT_LINKED_OBJECT)

         .then()
         .log()
         .everything()

         .statusCode(CREATED.value())
         .body("text",containsStringIgnoringCase(comment2.getText()))
         .body("postId",containsStringIgnoringCase(commentedPost.getPostId()))
         .body("author.id",containsString(userCommentAuthor.getId()))
         .body(matchesJsonSchemaInClasspath("contracts/comment/saveLinked.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("saveEmbedSubst")
  void saveEmbedSubst() {
    Comment comment2 = commentNoId(userCommentAuthor,commentedPost).create();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(comment2)

         .when()
         .post(SAVE_COMMENT_EMBED_OBJECT_SUBST)

         .then()
         .log()
         .everything()

         .statusCode(CREATED.value())
         .body("postId",containsStringIgnoringCase(commentedPost.getPostId()))
         .body("author.id",containsStringIgnoringCase(userPostAuthor.getId()))
         .body("comment.text",containsStringIgnoringCase(comment2.getText()))
         .body("comment.author.id",containsStringIgnoringCase(comment2.getAuthor()
                                                                      .getId()))
         .body(matchesJsonSchemaInClasspath("contracts/comment/saveEmbedSubst.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("SaveEmbedList")
  void SaveEmbedList() {
    Comment comment2 = commentNoId(userCommentAuthor,commentedPost).create();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(comment2)

         .when()
         .post(SAVE_COMMENT_EMBED_OBJECT_LIST)

         .then()
         .log()
         .everything()

         .statusCode(CREATED.value())
         .body("postId",containsStringIgnoringCase(commentedPost.getPostId()))
         .body("author.id",containsStringIgnoringCase(userPostAuthor.getId()))
         .body("listComments.text",hasItem(comment2.getText()))
         .body("listComments.author.id",hasItem(comment2.getAuthor()
                                                        .getId()))
         .body(matchesJsonSchemaInClasspath("contracts/comment/SaveEmbedList.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("delete")
  void delete() {
    RestAssuredWebTestClient.responseSpecification = responseSpecNoContentType();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(comment1)

         .when()
         .delete()

         .then()
         .log()
         .everything()

         .statusCode(NO_CONTENT.value())
    ;

    dbUtils.countAndExecuteCommentFlux(commentService.findAll(),0);
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Update")
  void Update() {
    var previousText = comment1.getText();

    var newText = Faker.instance()
                       .lorem()
                       .paragraph(2);

    comment1.setText(newText);

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(comment1)

         .when()
         .put()

         .then()
         .log()
         .everything()

         .statusCode(OK.value())
         .body("text",equalTo(newText))
         .body("text",not(equalTo(previousText)))
         .body(matchesJsonSchemaInClasspath("contracts/comment/update.json"))
    ;
  }


}