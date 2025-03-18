package src.servicemanager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.controllers.Controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SceneManager {
    private static final Map<String, Scene> primarySceneCache = new HashMap<>();
    private static final Map<String, Scene> secondarySceneCache = new HashMap<>();
    private static final Map<Scene, Controller> primaryControllers = new HashMap<>();
    private static final Map<Scene, Controller> secondaryControllers  = new HashMap<>();
    private static Stage primaryStage;
    private static Stage secondaryStage;

    private static final List<Scene> previousScene = new ArrayList<>();

    public static void setPrimaryStage(Stage stage) {
            primaryStage = stage;
    }

    public static void setSecondaryStage(Stage stage) {
        secondaryStage = stage;
    }

    public static void loadPrimaryScene(String name, String fileFxml, Controller controller) {
        try {
            System.out.println("Loading scene: " + name);
            GuestContext.setCurrentController(controller);
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fileFxml));
            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // load data into caches
            primarySceneCache.put(name, scene);
            primaryControllers.put(scene, controller);
            primaryStage.setScene(scene);
            primaryStage.setTitle(name);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Errore nel caricamento del file FXML: " + fileFxml);
            e.printStackTrace();
        }
    }

    public static Controller changeScene(String name, String fileFxml, Controller controller) {
        return changeScene(name, fileFxml, controller, primarySceneCache, primaryStage, primaryControllers);
    }

    public static Controller changeSecondaryScene(String name, String fileFxml, Controller controller) {
      return changeScene(name, fileFxml, controller, secondarySceneCache, secondaryStage, secondaryControllers);
    }

    public static Stage loadScene(String filefxml, Controller controller){
        LoadScene(filefxml, controller, primaryStage);
        return primaryStage;
    }

    private static Controller changeScene(String name, String fileFxml, Controller controller, Map<String, Scene> cache, Stage stage ,Map<Scene, Controller> Controllers) {
        try {
            System.out.println("Loading scene: " + name);

            if (cache.containsKey(name)) {
                Scene scene = cache.get(name);
                System.out.println("Scene from cache");
                Controller ctrl = Controllers.get(scene);
                GuestContext.setCurrentController(ctrl);
                stage.setScene(scene);
                ctrl.init_data();
                stage.show();
                return ctrl;
            } else {
                GuestContext.setCurrentController(controller);
                // Carichiamo dinamicamente la scena
                FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fileFxml));
                loader.setController(controller);
                Parent root = loader.load();
                Scene scene = new Scene(root);
                primarySceneCache.put(name, scene);
                Controllers.put(scene, controller);
                stage.setScene(scene);
                stage.setTitle(name);
            }
            stage.show();
            return controller;
        } catch (IOException e) {
            System.err.println("Errore nel caricamento dinamico del file FXML: " + fileFxml);
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static void showFirstStage() {
        primaryStage.show();
    }

    // Apri una finestra modale (come il login)
    public static void openModal(String name, String fileFxml, Controller controller, Stage owner) {
        try {
            GuestContext.setCurrentController(controller);
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fileFxml));
            loader.setController(controller);
            Parent root = loader.load();
            Stage modalStage = new Stage();
            secondaryStage = modalStage;
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(owner);
            Scene loginScene = new Scene(root);
            secondarySceneCache.put(name, loginScene);
            secondaryControllers.put(loginScene, controller);
            modalStage.setScene(loginScene);
            modalStage.setTitle(name);
            modalStage.showAndWait();  // Blocco finché non si chiude la finestra
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della finestra modale: " + fileFxml);
            e.printStackTrace();
        }
    }

    private static void closeStage(Stage stage){
        stage.close();
    }

    public static void closePrimaryStage(){
        closeStage(primaryStage);
    }

    public static void closeSecondaryStage(){
        closeStage(secondaryStage);
    }

    private static void LoadScene(String fileFxml, Controller controller, Stage stage) {
        try {
            GuestContext.setPreviousContextController(GuestContext.getCurrentController());
            GuestContext.setCurrentController(controller);
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fileFxml));
            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Cambiamo la scena sulla finestra principale senza creare una nuova Stage
            stage.setScene(scene);
            //primaryStage.show();
            //stage.show();

        } catch (IOException e) {
            System.err.println("Errore nel caricamento della scena: " + fileFxml);
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage(){
        return primaryStage;
    }

    public static void loadPreviousScene() throws SQLException {
        loadPreviousScene(false);
    }

    public static void loadPreviousScene(Boolean reload) throws SQLException {
        Controller ctrl = GuestContext.getPreviousContextController();
        GuestContext.setCurrentController(ctrl);
        primaryStage.setScene(getPreviousScene());
        if(reload) {
            ctrl.init_data();
        }
        primaryStage.show();
    }

    public static Scene getPreviousScene() {
        Scene scene = previousScene.getLast();
        previousScene.removeLast();
        return scene;
    }

    public static void setPreviousScene(Scene currentScene) {
        previousScene.addLast(currentScene);
    }

    // For test
    public static Scene getCurrentScene() {
        if (primaryStage != null && primaryStage.isShowing()) {
            return primaryStage.getScene();
        } else if (secondaryStage != null && secondaryStage.isShowing()) {
            return secondaryStage.getScene();
        }
        return null;
    }

    public static String getCurrentStageName() {
        if (secondaryStage != null && secondaryStage.isShowing()) {
            return secondaryStage.getTitle();
        } else if (primaryStage != null && primaryStage.isShowing()) {
            return primaryStage.getTitle();
        }
        return null;
    }

}
