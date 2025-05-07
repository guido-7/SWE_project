import javafx.application.Application;
import javafx.stage.Stage;
import src.domainmodel.Guest;
import src.domainmodel.PermitsManager;
import src.domainmodel.Role;
import src.usersession.GuestContext;
import src.usersession.SceneManager;
import src.factory.PageControllerFactory;
public class main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Guest guest = new Guest(PermitsManager.createGuestPermits(), Role.GUEST);
        GuestContext.setCurrentGuest(guest);
        SceneManager.setPrimaryStage(primaryStage);
        SceneManager.loadPrimaryScene("home", "/src/view/fxml/HomePage.fxml", PageControllerFactory.createHomePageController(guest));
       }

    public static void main(String[] args) {
        launch(args);
    }
}
