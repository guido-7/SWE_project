package src.servicemanager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.businesslogic.FeedService;
import src.controllers.HomePageController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private static final Map<String, Scene> primarySceneCache = new HashMap<>();
    private static final Map<String, Scene> secondarySceneCache = new HashMap<>();
    private static Stage primaryStage;
    private static Stage secondaryStage;


    public static void setPrimaryStage(Stage stage) {
            primaryStage = stage;
    }

    public static void setSecondaryStage(Stage stage) {
        secondaryStage = stage;

    }



    public static void loadPrimaryScene(String name, String fileFxml, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fileFxml));
            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primarySceneCache.put(name, scene);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Errore nel caricamento del file FXML: " + fileFxml);
            e.printStackTrace();
        }
    }

    public static void changeScene(String name, String fileFxml, Object controller) {
        changeScene(name, fileFxml, controller, primarySceneCache, primaryStage);
    }

    public static void changeSecondaryScene(String name, String fileFxml, Object controller) {
      changeScene(name, fileFxml, controller, secondarySceneCache, secondaryStage);
    }

    private static void changeScene(String name, String fileFxml, Object controller, Map<String, Scene> cache, Stage stage) {
        try {

            if (cache.containsKey(name)) {
                stage.setScene(cache.get(name));
                System.out.println("Scene from cache");
            } else {
                // Carichiamo dinamicamente la scena
                FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fileFxml));
                loader.setController(controller);
                Parent root = loader.load();
                Scene scene = new Scene(root);

                stage.setScene(scene);

            }
            stage.show();
        } catch (IOException e) {
            System.err.println("Errore nel caricamento dinamico del file FXML: " + fileFxml);
            e.printStackTrace();
        }
    }


    private static void showFirstStage(){
        primaryStage.show();
    }

    // Apri una finestra modale (come il login)
    public static void openModal(String name, String fileFxml, Object controller, Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fileFxml));
            loader.setController(controller);
            Parent root = loader.load();

            Stage modalStage = new Stage();
            secondaryStage=modalStage;
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(owner);
            Scene loginScene = new Scene(root);
            secondarySceneCache.put(name, loginScene);
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

//    public static void reloadScene(String name, String fileFxml, Object controller) {
//        try {
//            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fileFxml));
//            loader.setController(controller);
//            Parent root = loader.load();
//            Scene scene = new Scene(root);
//
//            // Aggiorniamo la cache con la nuova scena
//            sceneCache.put(name, scene);
//
//            // Cambiamo la scena sulla finestra principale senza creare una nuova Stage
//            primaryStage.setScene(scene);
//            primaryStage.show();
//        } catch (IOException e) {
//            System.err.println("Errore nel ricaricamento della scena: " + fileFxml);
//            e.printStackTrace();
//        }
//    }

}

