import src.managerdatabase.DBConnection;
import src.managerdatabase.SetDB;
import src.orm.CommunityDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/*
public class main {
    public static void main(String[] args) throws SQLException {
        DBConnection.connect();
        SetDB.createDB();
        DBConnection.disconnect();


    }
}*/
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
        primaryStage.setTitle("Login Page");
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
