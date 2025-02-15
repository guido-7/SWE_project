package src.controllers;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private static final Map<String, Scene> sceneCache = new HashMap<>();
    private static Stage primaryStage = new Stage();

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        showFirstStage();
    }

    public static void preloadScene(String name, String fileFxml, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fileFxml));
            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            sceneCache.put(name, scene);
        } catch (IOException e) {
            System.err.println("Errore nel caricamento del file FXML: " + fileFxml);
            e.printStackTrace();
        }
    }

    public static void changeScene(String name, String fileFxml, Object controller) {
        try {
            // Se la scena è precaricata, usiamo la cache
            if (sceneCache.containsKey(name)) {
                primaryStage.setScene(sceneCache.get(name));
            } else {
                // Carichiamo dinamicamente la scena
                FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fileFxml));
                loader.setController(controller);
                Parent root = loader.load();
                Scene scene = new Scene(root);

                primaryStage.setScene(scene);

            }
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Errore nel caricamento dinamico del file FXML: " + fileFxml);
            e.printStackTrace();
        }
    }

    public static void changeScene(String name, Scene scene) {
        if (sceneCache.containsKey(name))
            primaryStage.setScene(sceneCache.get(name));
        else
            primaryStage.setScene(scene);

        primaryStage.show();
    }

    private static void showFirstStage(){
        primaryStage.show();
    }
}

