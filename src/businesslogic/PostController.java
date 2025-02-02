package src.businesslogic;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import src.domainmodel.Post;
import src.orm.PostDao;

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

    private Post post;
    private PostDao postDao = new PostDao();

    public void setData(Post post){
//        this.post = post;
//        username.setText(post.getUser().getName());
//        date.setText(post.getTime().toString());
//        title.setText(post.getTitle());
//        content.setText(post.getContent());

        this.post = post;
        username.setText("Ute");
        date.setText("2025-01-01");
        title.setText("Titolo");
        content.setText("Contenuto");
    }

    public Post getPost(int id) throws SQLException {;
        return postDao.findById(id).orElse(null);
    }
}
