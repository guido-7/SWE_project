package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import src.businesslogic.CommentService;
import src.businesslogic.PostService;
import src.servicemanager.GuestContext;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PostReplyController implements Controller, Initializable {
    @FXML
    private TextArea replyField;
    @FXML
    private Button sendButton;

    private final PostController postController;
    private final PostService postService;
    private final PostPageController postPageController;
    private String reply;

    public PostReplyController(PostController postController) {
        this.postController = postController;
        this.postService = postController.getPostService();
        this.postPageController = (PostPageController) GuestContext.getCurrentController();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sendButton.setOnAction( event ->handleSendReplyButtonClick());

    }

    private boolean addReply() throws SQLException {
        if(postService.addReply(reply)) {
            postController.removeReplyField();
            postPageController.init_data();
            return true;
        }
        return false;
    }

    @Override
    public void init_data() {

    }

    private void handleSendReplyButtonClick(){
        reply = replyField.getText();
        if (reply.isEmpty()) {
            return;
        } else {
            try {
                if(addReply()) {
                    postController.init_data();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
