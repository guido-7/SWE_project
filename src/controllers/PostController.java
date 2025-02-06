package src.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import src.domainmodel.Community;
import src.domainmodel.Post;
import src.domainmodel.User;
import src.orm.CommunityDAO;
import src.orm.PostDao;
import src.orm.UserDAO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class PostController {
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

    private Post post;
    private PostDao postDao = new PostDao();

    public void setData(Post post) throws SQLException {
        this.post = post;

        CommunityDAO communityDAO = new CommunityDAO();
        Community comm = communityDAO.findById(post.getCommunityId()).orElse(null);
        community.setText("r/" + comm.getTitle());

        UserDAO userDAO = new UserDAO();
        User user = userDAO.findById(post.getUserId()).orElse(null);
        username.setText(user.getNickname());

        date.setText(getFormattedTime(post.getTime()));
        title.setText(post.getTitle());
        content.setText(post.getContent());
        scoreLabel.setText(post.getLikes() - post.getDislikes() + "");

    }

    public Post getPost(int id) throws SQLException {
        return postDao.findById(id).orElse(null);
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
}
