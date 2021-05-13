package utils.databuilders;

import com.github.javafaker.Faker;
import com.mongo.api.modules.comment.Comment;
import com.mongo.api.modules.post.entity.Post;
import com.mongo.api.modules.user.entity.AuthorDto;
import com.mongo.api.modules.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Builder
@Getter
public class CommentBuilder {

    private final Comment comment;

    private static final Faker faker = new Faker(new Locale("en-CA.yml"));
    //    private static String FAKER_REGEX_CPF = "[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2}";
    //    private static String FAKER_REGEX_DDD = "[0-9]{2}";
    //    private static String FAKER_REGEX_TEL = "[0-9]{9}";


    public static CommentBuilder commentFull(User commentUserAuthorDTO,
                                             Post commentedPost) {

        List<String> idCommentsList = new ArrayList<>();

        Comment commentFull = new Comment();
        commentFull.setId(faker.regexify("/^[a-f\\d]{24}$/i"));
        commentFull.setIdPost(commentedPost.getId());
        commentFull.setDate(faker.date()
                                 .birthday());
        commentFull.setText("Comment-Text: " + faker.lorem()
                                                    .sentence(25));
        commentFull.setAuthor(new AuthorDto(commentUserAuthorDTO));
        return CommentBuilder.builder()
                             .comment(commentFull)
                             .build();
    }

    public static CommentBuilder comment_NoID(Post commentedPostWithUserAuthorDTO) {

        List<String> idCommentsList = new ArrayList<>();

        Comment commentFull = new Comment();
//        commentFull.setId(faker.regexify("/^[a-f\\d]{24}$/i"));
        commentFull.setIdPost(commentedPostWithUserAuthorDTO.getId());
        commentFull.setDate(faker.date()
                                 .birthday());
        commentFull.setText("Comment-Text: " + faker.lorem()
                                                    .sentence(25));
        commentFull.setAuthor(commentedPostWithUserAuthorDTO.getAuthor());
        return CommentBuilder.builder()
                             .comment(commentFull)
                             .build();
    }

    public static CommentBuilder commentFullIdPost(User commentUserAuthorDTO,
                                             String idPost) {

        List<String> idCommentsList = new ArrayList<>();

        Comment commentFull = new Comment();
        commentFull.setId(faker.regexify("/^[a-f\\d]{24}$/i"));
        commentFull.setIdPost(idPost);
        commentFull.setDate(faker.date()
                                 .birthday());
        commentFull.setText("Comment-Text: " + faker.lorem()
                                                    .sentence(25));
        commentFull.setAuthor(new AuthorDto(commentUserAuthorDTO));
        return CommentBuilder.builder()
                             .comment(commentFull)
                             .build();
    }


    public Comment create() {
        return this.comment;
    }
}
