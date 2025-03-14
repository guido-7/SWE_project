package src.controllers;

import javafx.fxml.Initializable;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import src.businesslogic.CommentService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CommentReplyController implements Controller, Initializable {
    @FXML
    private TextArea replyField;
    @FXML
    private Button sendButton;

    private final CommentController commentController;
    private final CommentService commentService;

    public CommentReplyController(CommentController commentController) {
        this.commentController = commentController;
        this.commentService = commentController.getCommentService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sendButton.setOnAction(event ->handleSendReplayButtonClick());
    }

    @Override
    public void init_data() {

    }

    private void handleSendReplayButtonClick(){
        String reply = replyField.getText();
        if (!reply.isEmpty()) {
            try {
                if(commentService.addReply(reply)) {
                    commentController.removeReplyField();
                    commentController.loadSubComments();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
