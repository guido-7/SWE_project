package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import src.businesslogic.CommentService;
import src.domainmodel.Comment;
import src.servicemanager.FormattedTime;
import src.servicemanager.VoteManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CommentController implements Controller, Initializable {
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
    @FXML
    private ImageView moreComments;
    @FXML
    private ImageView minusComments;
    @FXML
    private VBox repliesContainer;

    private final CommentService commentService;
    private final FormattedTime formatter = new FormattedTime();
    private VoteManager voteManager;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        moreComments.setVisible(false);
        minusComments.setVisible(false);
        voteManager = new VoteManager(scoreLabel, likeButton, dislikeButton, commentService);

        init_data();

        moreComments.setOnMouseClicked(event -> {
            moreComments.setVisible(false);
            minusComments.setVisible(true);
            List<Comment> subComment = commentService.getCommentsByLevel();
            loadSubComments();
        } );

        minusComments.setOnMouseClicked(event -> {
            minusComments.setVisible(false);
            moreComments.setVisible(true);
            repliesContainer.getChildren().clear();
        });

    }

    public void setData(Comment comment) {
        CommentService commentService = new CommentService(comment);
        System.out.println("Setting data");
        username.setText(commentService.getCommentAuthor());
        content.setText(commentService.getCommentText());
        date.setText(formatter.getFormattedTime(comment.getTime()));
        scoreLabel.setText(comment.getLikes() - comment.getDislikes() + "");
    }

    private void loadSubComments() {
        List<Comment> subComments = commentService.getCommentsByLevel();
        for (Comment comment : subComments) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/src/view/fxml/Comment.fxml"));
                CommentController commentController = new CommentController(new CommentService(comment));
                fxmlLoader.setController(commentController);
                VBox commentBox = fxmlLoader.load();
                commentController.setData(comment);
                repliesContainer.getChildren().add(commentBox);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void init_data() {
        if(commentService.hasSubComments()) {
            moreComments.setVisible(true);
        }
        commentService.refreshComment();
        setData(commentService.getComment());
        voteManager.checkUserVote();
    }

}
