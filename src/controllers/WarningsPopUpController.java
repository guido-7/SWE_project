package src.controllers;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class WarningsPopUpController implements Controller {
    @FXML
    private Text Content;
    @FXML
    private Text Title;

    public void setData(String title, String content) {
        Title.setText(title);
        Title.setFont(javafx.scene.text.Font.font("bold", 20));
        Content.setText(content);
    }

    @Override
    public void init_data() {

    }
}
