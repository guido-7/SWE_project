package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import src.businesslogic.CommentService;
import src.controllers.factory.ComponentFactory;
import src.domainmodel.Comment;
import src.domainmodel.Role;
import src.servicemanager.FormattedTime;
import src.servicemanager.GuestContext;
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
    private ImageView replyToCommButton;
    @FXML
    private VBox repliesContainer;
    @FXML
    private Button reportButton;

    private final CommentService commentService;
    private final FormattedTime formatter = new FormattedTime();
    private VoteManager voteManager;
    boolean isReplying = false;

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
            loadSubComments();
        } );

        minusComments.setOnMouseClicked(event -> {
            minusComments.setVisible(false);
            moreComments.setVisible(true);
            if (isReplying) {
                repliesContainer.getChildren().remove(1, repliesContainer.getChildren().size());
            } else {
                repliesContainer.getChildren().clear();
            }
        });

        replyToCommButton.setOnMouseClicked(event -> {
            if(isReplying){
                isReplying = false;
                repliesContainer.getChildren().removeFirst();
            } else {
                isReplying = true;
                loadReplyBox();
            }
        });

        reportButton.setOnMouseClicked(event -> {
            handleReport();
            reportButton.setVisible(false);
        });
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

    public void setData(Comment comment) {
        CommentService commentService = new CommentService(comment);
        System.out.println("Setting data");
        username.setText(commentService.getCommentAuthor());
        content.setText(commentService.getCommentText());
        date.setText(formatter.getFormattedTime(comment.getTime()));
        scoreLabel.setText(comment.getLikes() - comment.getDislikes() + "");
        reportButton.setVisible(GuestContext.getCurrentGuest().getRole() != Role.GUEST && !commentService.isReported());
    }

    public void loadSubComments() {
        if(isReplying) {
            repliesContainer.getChildren().remove(1, repliesContainer.getChildren().size());
        } else {
            repliesContainer.getChildren().clear();
        }
        List<Comment> subComments = commentService.getCommentsByLevel();
        if (!subComments.isEmpty()) {
            moreComments.setVisible(false);
            minusComments.setVisible(true);
        }
        for (Comment comment : subComments) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/src/view/fxml/Comment.fxml"));
                CommentController commentController = ComponentFactory.createCommentController(comment);
                fxmlLoader.setController(commentController);
                VBox commentBox = fxmlLoader.load();
                commentController.setData(comment);
                repliesContainer.getChildren().add(commentBox);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadReplyBox() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/src/view/fxml/Reply.fxml"));
            CommentReplyController commentReplyController = new CommentReplyController(this);
            fxmlLoader.setController(commentReplyController);
            VBox replyBox = fxmlLoader.load();
            repliesContainer.getChildren().addFirst(replyBox);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeReplyField() {
        isReplying = false;
        repliesContainer.getChildren().removeFirst();
    }

    private void handleReport() {
        commentService.reportComment();
    }

    public CommentService getCommentService() {
        return commentService;
    }
}
