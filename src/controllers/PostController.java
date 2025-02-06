package src.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import src.domainmodel.Post;
import src.domainmodel.User;
import src.orm.PostDao;
import src.orm.UserDAO;

import java.sql.SQLException;

public class PostController {
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

        UserDAO userDAO = new UserDAO();
        //User user = userDAO.findById(post.getUserId()).orElse(null);
        username.setText("viiii");

        date.setText(post.getTime().toString());
        title.setText(post.getTitle());
        content.setText(post.getContent());
        scoreLabel.setText(post.getLikes() - post.getDislikes() + "");

    }

    public Post getPost(int id) throws SQLException {
        return postDao.findById(id).orElse(null);
    }
}
