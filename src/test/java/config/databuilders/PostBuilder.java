package config.databuilders;

import com.github.javafaker.Faker;
import com.mongo.api.core.dto.UserAuthorDto;
import com.mongo.api.modules.comment.Comment;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.user.User;
import lombok.Builder;
import lombok.Getter;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Builder
@Getter
public class PostBuilder {

  private final Post post;

  private static final ModelMapper conv = new ModelMapper();

  private static final Faker faker = new Faker(new Locale("en-CA.yml"));
  //    private static String FAKER_REGEX_CPF = "[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2}";
  //    private static String FAKER_REGEX_DDD = "[0-9]{2}";
  //    private static String FAKER_REGEX_TEL = "[0-9]{9}";


  public static PostBuilder postFull
       (User postUserAuthor,
        Comment comment,
        List<Comment> commentList) {

    List<String> idCommentsList = new ArrayList<>();

    Post postFull = new Post();
    postFull.setPostId(faker.regexify("/^[a-f\\d]{24}$/i"));
    postFull.setTitle(faker.rockBand()
                           .name());
    postFull.setBody(faker.lorem()
                          .sentence(25));
    postFull.setDate(faker.date()
                          .birthday());
    postFull.setAuthor(conv.map(postUserAuthor,UserAuthorDto.class));
    postFull.setComment(comment);
    postFull.setListComments(commentList);

    for (Comment comment1 : commentList) {
      idCommentsList.add(comment1.getCommentId());
    }
    postFull.setIdComments(idCommentsList);
    return PostBuilder.builder()
                      .post(postFull)
                      .build();
  }


  public static PostBuilder post_IdNull_CommentsEmpty(User user) {

    Post postFull = new Post();

    postFull.setTitle(faker.rockBand()
                           .name());

    postFull.setBody(faker.lorem()
                          .sentence(25));

    postFull.setDate(faker.date()
                          .birthday());

    postFull.setAuthor(conv.map(user,UserAuthorDto.class));

    return PostBuilder.builder()
                      .post(postFull)
                      .build();
  }

  public static PostBuilder postFull_CommentsEmpty(User user) {

    Post postFull = new Post();

    postFull.setPostId(faker.regexify("[a-f]{24}"));

    postFull.setTitle(faker.rockBand()
                           .name());

    postFull.setBody(faker.lorem()
                          .sentence(25));

    postFull.setDate(faker.date()
                          .birthday());

    postFull.setAuthor(conv.map(user,UserAuthorDto.class));

    return PostBuilder.builder()
                      .post(postFull)
                      .build();
  }


  public Post create() {
    return this.post;
  }
}
