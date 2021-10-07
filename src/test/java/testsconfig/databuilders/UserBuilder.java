package testsconfig.databuilders;

import com.github.javafaker.Faker;
import com.mongo.api.core.dto.CommentAllDto;
import com.mongo.api.core.dto.PostAllDto;
import com.mongo.api.core.dto.UserAllDto;
import com.mongo.api.modules.comment.Comment;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Builder
@Getter
public class UserBuilder {

  private final User user;
  private final UserAllDto userShowAll;

  private static final Faker faker = new Faker(new Locale("en-CA.yml"));
  //    private static String FAKER_REGEX_CPF = "[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2}";
  //    private static String FAKER_REGEX_DDD = "[0-9]{2}";
  //    private static String FAKER_REGEX_TEL = "[0-9]{9}";


  public static String createFakeUniqueRandomId() {
    return faker.regexify("PP[a-z0-9]{24}");
  }


  public static UserBuilder userFull_withoutID(List<Post> userListPosts) {
    List<String> idPosts = new ArrayList<>();

    User userFull = new User();

    userFull.setName(faker.name()
                          .fullName());
    userFull.setEmail(faker.internet()
                           .emailAddress());

    for (Post post : userListPosts) {
      idPosts.add(post.getPostId());
    }

    userFull.setIdPosts(idPosts);

    return UserBuilder.builder()
                      .user(userFull)
                      .build();
  }


  public static UserBuilder userWithID_IdPostsEmpty() {

    User userWithID_ListIdPostsEmpty = new User();

    userWithID_ListIdPostsEmpty.setId(createFakeUniqueRandomId());

    userWithID_ListIdPostsEmpty.setName(faker.name()
                                             .fullName());

    userWithID_ListIdPostsEmpty.setEmail(faker.internet()
                                              .emailAddress());

    return UserBuilder.builder()
                      .user(userWithID_ListIdPostsEmpty)
                      .build();
  }


  public static UserBuilder userShowAll_Test(User user,Post post,Comment comment) {

    UserAllDto userAllDto = new UserAllDto();
    userAllDto.setId(user.getId());
    userAllDto.setName(user.getName());

    PostAllDto postAllDto = new PostAllDto();
    postAllDto.setPostId(post.getPostId());
    postAllDto.setTitle(post.getTitle());
    postAllDto.setIdAuthor(user.getId());

    CommentAllDto commentAllDto = new CommentAllDto();
    commentAllDto.setCommentId(comment.getCommentId());
    commentAllDto.setAuthorId(user.getId());
    commentAllDto.setPostId(post.getPostId());
    commentAllDto.setText(comment.getText());

    postAllDto.getListComments()
              .add(commentAllDto);
    userAllDto.getPosts()
              .add(postAllDto);


    return UserBuilder.builder()
                      .userShowAll(userAllDto)
                      .build();
  }


  public static UserBuilder userFull_IdNull_ListIdPostsEmpty() {

    User userFull_IdNull_ListIdPostsEmpty = new User();

    userFull_IdNull_ListIdPostsEmpty
         .setName(faker.name()
                       .fullName());

    userFull_IdNull_ListIdPostsEmpty
         .setEmail(faker.internet()
                        .emailAddress());

    return UserBuilder.builder()
                      .user(userFull_IdNull_ListIdPostsEmpty)
                      .build();
  }


  public User createTestUser() {
    return this.user;
  }


  public UserAllDto createTestUserShowAll() {
    return this.userShowAll;
  }
}
