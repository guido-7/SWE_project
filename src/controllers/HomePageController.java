package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import src.domainmodel.Post;

import java.io.IOException;
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

    private List<Post> getPosts() throws SQLException {
        PostController postController = new PostController();
        List<Post> ls = new ArrayList<>();

        for (int i = 1; i < 8; i++) {
            ls.add(postController.getPost(i));
        }

        return ls;
    }

    @FXML
    public static void openHomePage(Stage stage) {
        try {
            System.out.println("Opening Home Page...");
            FXMLLoader homePage = new FXMLLoader(HomePageController.class.getResource("/src/view/fxml/HomePage.fxml"));
            stage.setScene(new Scene(homePage.load()));
            stage.setTitle("Home Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Home Page.");
        }
    }
}
