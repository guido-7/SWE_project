package src.businesslogic;

import src.domainmodel.*;
import src.orm.CommentDAO;
import src.orm.PostDAO;
import src.orm.UserDAO;
import src.servicemanager.GuestContext;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CommentService {
    private Comment comment;
    private final  UserDAO userDAO = new UserDAO();
    private final  PostDAO postDAO = new PostDAO();
    private final  CommentDAO commentDAO = new CommentDAO();

    int noOfCommentsTaken = 0;
    int numberOfComments = 10;

    public CommentService(Comment comment) {
        this.comment = comment;
    }

    public Comment getComment() {
        return comment;
    }

    public String getCommentText() {
        return comment.getContent();
    }

    public String getCommentAuthor() {
        return userDAO.getNicknameById(comment.getUser_id());
    }

    public List<Comment> getRootComments(Post post) {
        List<Comment> rootComments = commentDAO.getRootComments(post, numberOfComments, 0);
        System.out.println("Post ID: " + post.getId());
        noOfCommentsTaken = rootComments.size();
        return rootComments;
    }

    public List<Comment> getCommentsByLevel() {
        List<Comment> commentsByLevel = commentDAO.getCommentsByLevel(comment);
        System.out.println("New " + commentsByLevel.size() + " comments loaded");
        return commentsByLevel;
    }

    public boolean hasSubComments() {
        if (!commentDAO.getCommentsIdsByLevel(comment).isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isLiked(int id) {
        return commentDAO.isLiked(id, comment.getId(), comment.getPost_id());
    }

    public boolean isDisliked(int id) {
        return commentDAO.isDisliked(id, comment.getId(), comment.getPost_id());
    }

    public void toggleLike(User user) throws SQLException {
        toggleVote(user, "like");
    }

    public void toggleDislike(User user) throws SQLException {
        toggleVote(user, "dislike");
    }

    private void toggleVote(User guest, String vote) throws SQLException {
        Integer userVote = getVote( (User) guest);
        Post post = postDAO.findById(comment.getPost_id()).orElse(null);
        switch (vote) {
            case "like":
                if (userVote == null || userVote == 0) {
                    Map<String, Object> voteInfo = Map.of("user_id", guest.getId(), "post_id", comment.getPost_id(), "comment_id", comment.getId(), "community_id", post.getCommunityId(), "vote_type", 1);
                    userDAO.insertCommentVotes(voteInfo);
                } else {
                    Map<String, Object> voteInfo = Map.of("user_id", guest.getId(), "comment_id", comment.getId(), "post_id", comment.getPost_id());
                    userDAO.removeCommentVotes(voteInfo);
                }
                break;
            case "dislike":
                if (userVote == null || userVote == 1) {
                    Map<String, Object> voteInfo = Map.of("user_id", guest.getId(), "post_id", comment.getPost_id(), "comment_id", comment.getId(), "community_id", post.getCommunityId(), "vote_type", 0);
                    userDAO.insertCommentVotes(voteInfo);
                } else {
                    Map<String, Object> voteInfo = Map.of("user_id", guest.getId(), "comment_id", comment.getId(), "post_id", comment.getPost_id());
                    userDAO.removeCommentVotes(voteInfo);
                }
                break;
        }
    }

    private Integer getVote(User user) throws SQLException {
        UserDAO userDAO = new UserDAO();
        return userDAO.getCommentVote(user.getId(), comment.getId(), comment.getPost_id());
    }

    public void refreshComment() {
        try {
            comment = commentDAO.findById(List.of(comment.getPost_id(), comment.getId())).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addReply(String reply) throws SQLException {
        Guest guest = GuestContext.getCurrentGuest();
        if (guest.getRole() == Role.GUEST)
            return false;
        User user = (User) guest;
        Comment newComment = new Comment(comment.getPost_id(), comment.getLevel() + 1, user.getId(), reply, comment.getCommunity_id());
        boolean isOk = commentDAO.save(newComment, comment.getId(), comment.getLevel());
        return isOk;
    }

    public void reportComment() {
        Guest guest = GuestContext.getCurrentGuest();
        User user = (User) guest;
        commentDAO.reportComment(user.getId(), comment.getId(), comment.getPost_id(), comment.getCommunity_id());
    }

    public boolean isReported() {
        Guest guest = GuestContext.getCurrentGuest();
        User user = (User) guest;
        return commentDAO.isReported(user.getId(), comment.getId(), comment.getPost_id(), comment.getCommunity_id());
    }
}
