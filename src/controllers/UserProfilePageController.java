package src.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import src.businesslogic.UserProfileService;
import src.domainmodel.PermitsManager;
import src.domainmodel.User;
import src.servicemanager.SceneManager;
import src.utils.LoadingPost;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class UserProfilePageController implements Initializable, Controller {
    @FXML
    public VBox UserPostsContainer;
    @FXML
    public VBox SavedPostsContainer;
    @FXML
    public Label nicknameLabel;
    @FXML
    public Label nameLabel;
    @FXML
    public Label surnameLabel;
    @FXML
    private TextArea profileDescription;
    @FXML
    private VBox TextAreaContainer;
    @FXML
    private AnchorPane popupContainer;
    @FXML
    private Text savewarning;
    @FXML
    private ImageView exit;

    // vars no FXML
    private String description;
    private String tempDescription;

    private UserProfileService userProfileService ; //new UserProfileService(new User(1, "nickname", "name", "surname", PermitsManager.createUserPermits()));

    public void  setService(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    public UserProfilePageController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        savewarning.setVisible(false);
        popupContainer.setMouseTransparent(true);
        try {
            Map<String, String> userInfo = userProfileService.getUserInfo();
            nicknameLabel.setText(userInfo.get("nickname"));
            nameLabel.setText(userInfo.get("name"));
            surnameLabel.setText(userInfo.get("surname"));
            description = userProfileService.getDescription();
            profileDescription.setText(description);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        tempDescription = profileDescription.getText();

        try {
            init_data();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        exit.setOnMouseClicked(event-> backToHomePage());

        profileDescription.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                event.consume(); // Evita il ritorno a capo nell'area di testo
                showConfirmationPopup();
            }
        });

        profileDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            savewarning.setVisible(!Objects.equals(newValue, oldValue));
        });
    }

    @FXML
    private void showConfirmationPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/ConfirmationDialog.fxml"));
            Parent popUp = loader.load();
            ConfirmationDialogPageController dialogController = loader.getController();

            //  Impostiamo la callback
            dialogController.setCallback(confirmed -> {
                if (confirmed) {
                    tempDescription = profileDescription.getText();
                    if (description == null){
                        userProfileService.SetDescription(tempDescription);
                    }
                    else{
                        userProfileService.updateDescription(tempDescription);
                    }
                    profileDescription.getParent().requestFocus();
                    savewarning.setVisible(false);//  Se "Yes", salviamo nel DB
                } else {
                    System.out.println("Descrizione annullata");
                    profileDescription.setText(tempDescription);
                    profileDescription.getParent().requestFocus();
                    savewarning.setVisible(false);// Se "No", annulliamo la modifica
                }
                closePopup();
            });

            popupContainer.getChildren().clear();
            popupContainer.getChildren().add(popUp);
            popupContainer.setMouseTransparent(false);
            popupContainer.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void closePopup() {
        popupContainer.getChildren().clear();
        popupContainer.setMouseTransparent(true);
        popupContainer.setVisible(false);
    }

    @FXML
    private void backToHomePage(){
        SceneManager.changeScene("home","/src/view/fxml/HomePage.fxml",null);
    }

    private void clearPosts() {
        UserPostsContainer.getChildren().clear();
        SavedPostsContainer.getChildren().clear();
    }

    @Override
    public void init_data() throws SQLException {
        clearPosts();
        LoadingPost.LoadPosts(userProfileService.getUserPosts(), UserPostsContainer);
        LoadingPost.LoadPosts(userProfileService.getSavedPosts(), SavedPostsContainer);
    }
}
