import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import src.businesslogic.CommunityService;
import src.businesslogic.FeedService;
import src.controllers.CommunityController;
import src.controllers.CommunitySettingsController;
import src.controllers.HomePageController;
import src.domainmodel.Guest;
import src.domainmodel.PermitsManager;
import src.domainmodel.User;
import src.servicemanager.SceneManager;
import src.servicemanager.Service;

public class main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        User user = new User(1, "gio63", "Giovanni", "Lello", PermitsManager.createUserPermits());
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/CommunityPage.fxml"));
//        loader.setController(new CommunityController(new CommunityService(1)));
//        Parent root = loader.load();
//        primaryStage.setTitle("Community");
//        primaryStage.setScene(new Scene(root));
//        primaryStage.show();
        SceneManager.setPrimaryStage(primaryStage);
        SceneManager.loadPrimaryScene("home", "/src/view/fxml/HomePage.fxml", new HomePageController(new FeedService(user)));
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/HomePage.fxml"));
//        loader.setController(new HomePageController(new FeedService(user)));
//        Parent root = loader.load();
//        primaryStage.setTitle("Home");
//        primaryStage.setScene(new Scene(root));
//        primaryStage.show();

//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/CommunityPage.fxml"));
//        loader.setController(new CommunityController(new CommunityService(1)));
//        Parent root = loader.load();
//        primaryStage.setTitle("Community Settings");
//        primaryStage.setTitle("Community");
//        primaryStage.setScene(new Scene(root));
//        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

//    public static void launchCommunityService(Stage primaryStage) throws Exception {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/CommunitySettings.fxml"));
//        loader.setController(new CommunitySettingsController(new CommunityService(1)));
//        Parent root = loader.load();
//        primaryStage.setTitle("Community Settings");
//        primaryStage.setScene(new Scene(root));
//        primaryStage.show();
//}
}