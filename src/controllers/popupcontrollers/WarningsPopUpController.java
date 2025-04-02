package src.controllers.popupcontrollers;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class WarningsPopUpController{
    @FXML
    private Text Content;
    @FXML
    private Text Title;

    public void setWarningData(String title, String content) {
        Title.setText(title);
        Title.setFont(javafx.scene.text.Font.font("bold", 20));
        Content.setText(content);
    }
}
