import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import src.domainmodel.PermitsManager;
import src.domainmodel.User;
import src.servicemanager.Service;

public class main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        User user = new User(1, "gio63", "Giovanni", "Lello", PermitsManager.createUserPermits());
        Service.initializeServices(user);
        Parent root = FXMLLoader.load(getClass().getResource("/src/view/fxml/HomePage.fxml"));
        primaryStage.setTitle("Home Page");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}