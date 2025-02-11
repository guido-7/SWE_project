package src.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import src.businesslogic.CommunityService;
import src.domainmodel.PostWarnings;

import java.util.ArrayList;

public class CommunitySettingsController {

    @FXML
    private TableView<PostWarnings> reportsTable;

    @FXML
    private TableColumn<PostWarnings, String> reporter;

    @FXML
    private TableColumn<PostWarnings, String> reported;

    private final CommunityService communityService;

    public CommunitySettingsController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @FXML
    public void initialize() {
        reporter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSender_nickname()));
        reported.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReported_nickname()));
        setData(communityService.getPostWarnings());

    }

    public void setData(ArrayList<PostWarnings> reports) {
        ObservableList<PostWarnings> observableReports = FXCollections.observableArrayList(reports);
        reportsTable.setItems(observableReports);
    }
}

