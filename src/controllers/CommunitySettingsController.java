package src.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import src.businesslogic.CommunityService;
import src.domainmodel.PostWarnings;
import javafx.scene.text.Text;
import src.servicemanager.SceneManager;

import java.util.ArrayList;

public class CommunitySettingsController implements Controller {

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
    private ImageView exit;

    private final CommunityService communityService;

    public CommunitySettingsController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @FXML
    public void initialize() {
        exit.setOnMouseClicked(event-> backToCommunity());

        reportsTable.setSelectionModel(null);

        reporter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSender_nickname()));

        reporter.setCellFactory(tc -> new TableCell<PostWarnings, String>() {
            private final Text text = new Text();  // Usando Text per la scritta

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);  // Nessun grafico quando la cella è vuota
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
                    });

                    setGraphic(text);  // Imposta il testo come contenuto della cella
                }
            }
        });

        reported.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReported_nickname()));
        content.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContent()));
        title.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        setData(communityService.getWarnings());
    }

    private void backToCommunity() {
        SceneManager.changeScene("community " + communityService.getCommunityId(), "/src/view/fxml/CommunityPage.fxml",null);
    }

    public void setData(ArrayList<PostWarnings> reports) {
        ObservableList<PostWarnings> observableReports = FXCollections.observableArrayList(reports);
        reportsTable.setItems(observableReports);
    }

    @Override
    public void init_data() {
    }
}

