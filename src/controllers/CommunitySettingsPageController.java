package src.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import src.businesslogic.CommunityService;
import src.controllers.factory.ComponentFactory;
import src.controllers.factory.PageControllerFactory;
import src.domainmodel.PostWarnings;
import javafx.scene.text.Text;
import src.domainmodel.User;
import src.servicemanager.SceneManager;
import src.utils.LoadingPost;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class CommunitySettingsPageController implements Initializable, Controller {
    @FXML
    private VBox ModeratorChoiceContainer;
    @FXML
    private TableView<PostWarnings> reportsTable;
    @FXML
    private TableColumn<PostWarnings, String> reporter;
    @FXML
    private TableColumn<PostWarnings, String> content;
    @FXML
    private TableColumn<PostWarnings, String> reported;
    @FXML
    private TableColumn<PostWarnings, String> title;
    @FXML
    private TableColumn<Integer,String> reportNo;
    @FXML
    private ImageView exit;
    @FXML
    private ScrollPane scrollPane;

    private final CommunityService communityService;
    ArrayList<PostWarnings> reports;

    public CommunitySettingsPageController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init_data();
        exit.setOnMouseClicked(event-> backToCommunity());
    }

    @Override
    public void init_data() {
        ModeratorChoiceContainer.getChildren().clear();

        reportsTable.setSelectionModel(null);

        reportNo.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(reportsTable.getItems().indexOf(cellData.getValue()) + 1)));
        reporter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSender_nickname()));
        reporter.setCellFactory(tc -> new TableCell<PostWarnings, String>() {
            private final Text text = new Text();  // Usando Text per la scritta

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);  // Imposta il nome dell'utente come testo

                    // Gestisci il passaggio del mouse sopra il testo
                    text.setOnMouseEntered(event -> {
                        text.setFill(Color.GREEN);
                    });

                    // Gestisci quando il mouse esce dalla cella
                    text.setOnMouseExited(event -> {
                        text.setFill(Color.BLACK);  // Torna al colore originale (nero)
                    });

                    // Rendi il testo cliccabile
                    text.setOnMouseClicked(event -> {
                        System.out.println("Vai alla pagina di: " + item);
                        try {
                            User user = communityService.getUser(getTableRow().getItem().getSenderId());
                            UserProfilePageController userProfilePageController = PageControllerFactory.createUserProfilePageController(user);
                            SceneManager.setPreviousScene(SceneManager.getPrimaryStage().getScene());
                            Stage primaryStage = SceneManager.loadScene( "/src/view/fxml/UserProfilePage.fxml", userProfilePageController);
                            manageLookUpUser(userProfilePageController);
                            primaryStage.show();

                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    setGraphic(text);  // Imposta il testo come contenuto della cella
                }
            }
        });

        reported.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReported_nickname()));
        reported.setCellFactory(tc -> new TableCell<PostWarnings, String>() {
            private final Text text = new Text();  // Usando Text per la scritta

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);  // Imposta il nome dell'utente come testo

                    // Gestisci il passaggio del mouse sopra il testo
                    text.setOnMouseEntered(event -> {
                        text.setFill(Color.RED);
                    });

                    // Gestisci quando il mouse esce dalla cella
                    text.setOnMouseExited(event -> {
                        text.setFill(Color.BLACK);  // Torna al colore originale (nero)
                    });

                    // Rendi il testo cliccabile
                    text.setOnMouseClicked(event -> {
                        System.out.println("Vai alla pagina di: " + item);
                        try {
                            User user = communityService.getUser(getTableRow().getItem().getReportedId());
                            // TODO: review
                            //UserProfilePageController userProfilePageController = new UserProfilePageController(new UserProfileService(user));
                            UserProfilePageController userProfilePageController = PageControllerFactory.createUserProfilePageController(user);
                            SceneManager.setPreviousScene(SceneManager.getPrimaryStage().getScene());
                            Stage primaryStage = SceneManager.loadScene("/src/view/fxml/UserProfilePage.fxml", userProfilePageController);
                            manageLookUpUser(userProfilePageController);
                            primaryStage.show();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    setGraphic(text);  // Imposta il testo come contenuto della cella
                }
            }
        });
        content.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReducedContent(3)));
        content.setCellFactory(tc -> new TableCell<PostWarnings, String>() {
            private final Text text = new Text();  // Usando Text per la scritta

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);  // Imposta il contenuto del post

                    // Cambia colore al passaggio del mouse
                    text.setOnMouseEntered(event -> text.setFill(Color.BLUE));
                    text.setOnMouseExited(event -> text.setFill(Color.BLACK));

                    // Rendi il testo cliccabile
                    text.setOnMouseClicked(event -> {
                        System.out.println("Vai alla pagina del post: " + item);
                        try {
                            // Ottenere l'oggetto PostWarnings della riga
                            PostWarnings postWarning = getTableRow().getItem();

                            if (postWarning != null) {
                                String titoloPost = postWarning.getTitle();
                                String fullcontent = postWarning.getFullContent();

                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/WarningsPopUp.fxml"));
                                WarningsPopUpController popUpController = new WarningsPopUpController();
                                loader.setController(popUpController);
                                Parent root = loader.load();
                                popUpController.setWarningData(titoloPost, fullcontent);
                                Stage stage = new Stage();
                                stage.setScene(new Scene(root));
                                stage.show();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    setGraphic(text);  // Imposta il testo come contenuto della cella
                }
            }
        });

        title.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        reports = communityService.getWarnings();
        buildModeratorDecisionMenu(reports);
        setData(reports);
    }

    private void buildModeratorDecisionMenu(ArrayList<PostWarnings> reports) {
        Map<Integer , ArrayList<PostWarnings>> reportedUserCount = new HashMap<>(); // (reported_id , warningsOfUser)
        for (PostWarnings report : reports) {
            if (reportedUserCount.containsKey(report.getReportedId())) {
                reportedUserCount.get(report.getReportedId()).add(report);
            } else {
                ArrayList<PostWarnings> warnings = new ArrayList<>();
                warnings.add(report);
                reportedUserCount.put(report.getReportedId(),warnings);
            }
        }

        Set<Integer> reportedIds = reportedUserCount.keySet();
        for (Integer reportedId : reportedIds) {
            try {
                ArrayList<PostWarnings> reportedReports = reportedUserCount.get(reportedId);
                ModeratorDecisionController moderatorDecisionController = ComponentFactory.createModeratorDecisionController(reportedReports, communityService.getCommunityId());
                FXMLLoader fxmlLoader = new FXMLLoader(LoadingPost.class.getResource("/src/view/fxml/ModeratorDecisionSnapShot.fxml"));
                fxmlLoader.setController(moderatorDecisionController);
                Pane pane = fxmlLoader.load();
                moderatorDecisionController.setNumber(reportedReports.size());
                ModeratorChoiceContainer.getChildren().add(pane);

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    private void manageLookUpUser(UserProfilePageController userProfilePageController) {
        userProfilePageController.setText("Posts");
        userProfilePageController.deleteSavedPostPane();
        userProfilePageController.moveUserPostPaneToCenter();
        userProfilePageController.setNotEditable();
        userProfilePageController.getExitButton().setOnMouseClicked(event -> {
            try {
                SceneManager.loadPreviousScene();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void backToCommunity() {
        SceneManager.changeScene("community " + communityService.getCommunityId(), "/src/view/fxml/CommunityPage.fxml", null);
    }

    public void setData(ArrayList<PostWarnings> reports) {
        ObservableList<PostWarnings> observableReports = FXCollections.observableArrayList(reports);
        reportsTable.setItems(observableReports);
    }

    public void removeReport(Pane pane) {
        ModeratorChoiceContainer.getChildren().remove(pane);
    }

    public void removeReportsFromTable(ArrayList<PostWarnings> reports) {
        communityService.removeWarnings(reports);
        ObservableList<PostWarnings> observableReports = reportsTable.getItems();
        observableReports.removeAll(reports);
        reportsTable.setItems(observableReports);
    }

}
