package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import src.businesslogic.FeedService;
import src.domainmodel.Guest;
import src.domainmodel.PermitsManager;
import src.domainmodel.Post;
import src.domainmodel.User;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HomePageController implements Initializable {

    @FXML
    private VBox postsContainer;
    List<Post> posts;
    FeedService feedService = new FeedService(new User(1, "gio63", "Giovanni", "Lello", PermitsManager.createUserPermits()));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try{
            posts = new ArrayList<>(feedService.getFeed());
            for (Post post : posts) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/src/view/fxml/Post.fxml"));
                VBox vBox = fxmlLoader.load();
                PostController postController = fxmlLoader.getController();
                postController.setData(post);
                postsContainer.getChildren().add(vBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
