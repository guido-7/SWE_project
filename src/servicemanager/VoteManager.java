package src.servicemanager;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import src.businesslogic.PostService;
import src.businesslogic.CommentService;
import src.domainmodel.Guest;
import src.domainmodel.User;
import src.servicemanager.GuestContext;

import java.sql.SQLException;

public class VoteManager {
    private boolean isLiked = false;
    private boolean isDisliked = false;
    private final Label scoreLabel;
    private final Button likeButton;
    private final Button dislikeButton;
    private final Object service; // Può essere PostService o CommentService

    public VoteManager(Label scoreLabel, Button likeButton, Button dislikeButton, Object service) {
        this.scoreLabel = scoreLabel;
        this.likeButton = likeButton;
        this.dislikeButton = dislikeButton;
        this.service = service;
        checkUserVote();
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        likeButton.setOnAction(event -> handleLikeButton());
        dislikeButton.setOnAction(event -> handleDislikeButton());
    }

    public void checkUserVote() {
        Guest guest = GuestContext.getCurrentGuest();
        if (guest instanceof User user) {
            if (service instanceof PostService postService) {
                isLiked = postService.isLiked(user.getId());
                isDisliked = postService.isDisliked(user.getId());
            } else if (service instanceof CommentService commentService) {
                isLiked = commentService.isLiked(user.getId());
                isDisliked = commentService.isDisliked(user.getId());
            }
        }
        updateButtonStyles();
    }

    private void handleLikeButton() {
        try {
            Guest guest = GuestContext.getCurrentGuest();
            if (!(guest instanceof User user)) return;

            if (service instanceof PostService postService) {
                postService.toggleLike(user);
            } else if (service instanceof CommentService commentService) {
                commentService.toggleLike(user);
            }
            updateScore();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDislikeButton() {
        try {
            Guest guest = GuestContext.getCurrentGuest();
            if (!(guest instanceof User user)) return;

            if (service instanceof PostService postService) {
                postService.toggleDislike(user);
            } else if (service instanceof CommentService commentService) {
                commentService.toggleDislike(user);
            }
            updateScore();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateScore() throws SQLException {
        if (service instanceof PostService postService) {
            postService.refreshPost();
            scoreLabel.setText(String.valueOf(postService.getPost().getLikes() - postService.getPost().getDislikes()));
        } else if (service instanceof CommentService commentService) {
            commentService.refreshComment();
            scoreLabel.setText(String.valueOf(commentService.getComment().getLikes() - commentService.getComment().getDislikes()));
        }
        checkUserVote();
    }

    private void updateButtonStyles() {
        likeButton.getStyleClass().removeAll("selected", "unselected");
        dislikeButton.getStyleClass().removeAll("selected", "unselected");

        if (isLiked) {
            likeButton.getStyleClass().add("selected");
            dislikeButton.getStyleClass().add("unselected");
        } else if (isDisliked) {
            dislikeButton.getStyleClass().add("selected");
            likeButton.getStyleClass().add("unselected");
        } else {
            likeButton.getStyleClass().add("unselected");
            dislikeButton.getStyleClass().add("unselected");
        }
    }
}
