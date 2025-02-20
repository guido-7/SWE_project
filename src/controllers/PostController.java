package src.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import src.businesslogic.PostService;
import src.domainmodel.Community;
import src.domainmodel.Post;
import src.domainmodel.User;
import src.orm.CommunityDAO;
import src.orm.PostDAO;
import src.orm.UserDAO;
import src.utils.StringManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class PostController implements Controller {
    @FXML
    private Label community;
    @FXML
    private Label username;
    @FXML
    private Label content;
    @FXML
    private Label date;
    @FXML
    private Label title;
    @FXML
    private Label scoreLabel;
    @FXML
    private VBox myVBox;

    private PostService postService;

    public PostController(PostService postService){
        this.postService = postService;

    }
    private void setDataOnCard(Post post) throws SQLException {

        String communityTitle = postService.getCommunityTitle();
        community.setText("r/" + communityTitle);

        String nickname = postService.getnickname();
        username.setText(nickname);

        date.setText(getFormattedTime(post.getTime()));
        title.setText(post.getTitle());
        content.setText(post.getContent());
        scoreLabel.setText(post.getLikes() - post.getDislikes() + "");
    }

    public void setData(Post post) throws SQLException {
        myVBox.setMaxHeight(180);
        setDataOnCard(post);
    }

    public void setDataPostPage(Post post) throws SQLException {
        myVBox.setMaxHeight(Region.USE_COMPUTED_SIZE);
        setDataOnCard(post);
    }

    public Post getPost() throws SQLException {
        return postService.getPost();
    }

    public static String getFormattedTime(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();

        if (time.isAfter(now.minusHours(24))) { // Oggi
            long hoursAgo = ChronoUnit.HOURS.between(time, now);
            return hoursAgo + "h ago";
        }
        else if (time.isAfter(now.minusDays(7))) { // Ultima settimana
            long daysAgo = ChronoUnit.DAYS.between(time, now);
            return daysAgo + "d ago";
        }
        else if (time.getYear() == now.getYear()) { // Stesso anno
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            return time.format(formatter);
        }
        else { // Anni precedenti
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
            return time.format(formatter);
        }
    }

    @Override
    public void init_data() {

    }
}
