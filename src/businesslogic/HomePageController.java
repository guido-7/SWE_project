package src.businesslogic;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import src.domainmodel.Post;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HomePageController implements Initializable {

    @FXML
    private VBox postsContainer;
    List<Post> posts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try{
            posts = new ArrayList<>(getPosts());
            for (Post post : posts) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/fxml/Post.fxml"));
                VBox vBox = fxmlLoader.load();
                PostController postController = fxmlLoader.getController();
                postController.setData(post);
                postsContainer.getChildren().add(vBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Post> getPosts() throws SQLException {
        PostController postController = new PostController();
        List<Post> ls = new ArrayList<>();

        for (int i = 1; i < 8; i++) {
            ls.add(postController.getPost(i));
        }

        return ls;
    }
}
