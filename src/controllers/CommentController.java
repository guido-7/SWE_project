package src.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import src.businesslogic.CommentService;
import src.domainmodel.Comment;
import src.servicemanager.FormattedTime;

public class CommentController {
    @FXML
    private Label content;
    @FXML
    private Label date;
    @FXML
    private Button dislikeButton;
    @FXML
    private Button likeButton;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label username;

    private CommentService commentService;
    private final FormattedTime formatter = new FormattedTime();

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    public void setData(Comment comment) {
        CommentService commentService = new CommentService(comment);
        System.out.println("Setting data");
        username.setText(commentService.getCommentAuthor());
        content.setText(commentService.getCommentText());
        date.setText(formatter.getFormattedTime(comment.getTime()));
        scoreLabel.setText(comment.getLikes() - comment.getDislikes() + "");
    }
}
