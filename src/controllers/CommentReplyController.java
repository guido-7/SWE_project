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

    private CommentController commentController;
    private CommentService commentService;
    private String reply;

    public CommentReplyController(CommentController commentController) {
        this.commentController = commentController;
        this.commentService = commentController.getCommentService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sendButton.setOnAction(event -> {
            reply = replyField.getText();
            if (reply.isEmpty()) {
                return;
            } else {
                try {
                    if(addReply()) {
                        commentController.loadSubComments();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public boolean addReply() throws SQLException {
        if(commentService.addReply(reply)) {
            commentController.removeReplyField();
            commentController.loadSubComments();
            return true;
        }
        return false;
    }

    public void init_data() {
    }
}
