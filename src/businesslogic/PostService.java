package src.businesslogic;

import src.domainmodel.*;
import src.orm.CommentDAO;
import src.orm.CommunityDAO;
import src.orm.PostDAO;
import src.orm.UserDAO;
import src.servicemanager.GuestContext;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PostService {
    private  Post post;
    private final PostDAO postDAO = new PostDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CommunityDAO communityDAO = new CommunityDAO();

    private int offset = 0;

    public PostService(Post post) {
        this.post = post;
    }

    public Post getPost(){
        return post;
    }

    public void toggleLike(User user) throws SQLException {
        toggleLikeDislike(user,"like");
    }

    public void toggleDislike(User user) throws SQLException {
        toggleLikeDislike(user,"dislike");
    }

    private void toggleLikeDislike(User guest, String likelihood) throws SQLException {
        Integer userVote = getVote((User)guest);
        switch (likelihood) {
            case "like":
                // userVote == 1
                if (userVote == null || userVote == 0){
                    Map<String, Object> voteInfo = Map.of("user_id", ((User)guest).getId(), "post_id", post.getId(), "community_id", post.getCommunityId(), "vote_type", 1);
                    userDAO.insertPostVotes(voteInfo); // PostVotes
                } else {
                    Map<String, Object> voteInfo = Map.of("user_id", ((User)guest).getId(), "post_id", post.getId());
                    userDAO.removePostVotes(voteInfo);
                }
                break;
            case "dislike":
                if (userVote == null || userVote == 1) {
                    Map<String, Object> voteInfo = Map.of("user_id", ((User)guest).getId(), "post_id", post.getId(), "community_id", post.getCommunityId(), "vote_type", 0);
                    userDAO.insertPostVotes(voteInfo);
                } else {
                    Map<String, Object> voteInfo = Map.of("user_id", ((User)guest).getId(), "post_id", post.getId());
                    userDAO.removePostVotes(voteInfo);
                }
                break;
        }
    }

    private Integer getVote(User user) throws SQLException {
        return userDAO.getPostVote(user.getId(),post.getId());
    }

    public String getCommunityTitle() throws SQLException {
        return (String) communityDAO.retrieveSingleAttribute("Community","title","id = ?" , post.getCommunityId());
    }

    public String getNickname() throws SQLException {
        return (String) userDAO.retrieveSingleAttribute("User","nickname","id = ?", post.getUserId());
    }

    // TODO: review
    public List<Comment> getRootComments() {
        CommentDAO commentDAO = new CommentDAO();
        List<Comment> rC = commentDAO.getRootComments(post,10, 0);
        offset = 10;
        return rC;
    }

    public List<Comment> getNextRootComments() {
        CommentDAO commentDAO = new CommentDAO();
        List<Comment> rC = commentDAO.getRootComments(post,10, offset);
        offset += 10;
        return rC;
    }

    public void refreshPost() {
        try {
            post = postDAO.findById(post.getId()).orElse(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isLiked(int userId){
        return postDAO.isLiked(userId,post.getId());
    }

    public boolean isDisliked(int userId){
        return postDAO.isDisliked(userId,post.getId());
    }

    public boolean isPostOwner(int id) {
        return post.getUserId() == id;
    }

    public void deletePost(int id) {
        try {
            postDAO.deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addSavedPost(int userId) throws SQLException {
        UserDAO userDAO = new UserDAO();
        userDAO.addSavedPost(userId,post.getId());
        System.out.println("Post saved, user: " + userId + " post: " + post.getId());
    }

    public void removeSavedPost(int userId) {
        UserDAO userDAO = new UserDAO();
        userDAO.removeSavedPost(userId, post.getId());
        System.out.println("Post removed, user: " + userId + " post: " + post.getId());
    }

    public boolean isSaved(int userId) throws SQLException {
        UserDAO userDAO = new UserDAO();
        return userDAO.retrieveSingleAttribute("SavedPost", "user_id", "user_id = ? AND post_id = " + post.getId(), userId) != null;
    }

    public void signalPost() {
        User currentUser = (User) GuestContext.getCurrentGuest();
        postDAO.signalPost(currentUser.getId(),post.getId(),post.getCommunityId());
    }

    public boolean isReported() {
        User currentUser = (User) GuestContext.getCurrentGuest();
        return postDAO.isReported(currentUser.getId(),post.getId());
    }

    public boolean addReply(String reply) throws SQLException {
        Guest guest = GuestContext.getCurrentGuest();
        if (guest.getRole() == Role.GUEST)
            return false;
        User user = (User) guest;
        Comment newComment = new Comment(post.getId(), 0, user.getId(), reply, post.getCommunityId());
        CommentDAO commentDAO = new CommentDAO();
        return commentDAO.save(newComment, null, null);
    }

    public void addPinPost() {
        Map<String, Object> postInfo = Map.of("post_id", post.getId(), "community_id", post.getCommunityId());
        postDAO.insertPinnedPost(postInfo);
    }

    public void removePinPost() {
        postDAO.removePinnedPost(post.getId(), post.getCommunityId());
    }

    public boolean isUserAdminOfCommunity(int id, int communityId) {
        CommunityDAO communityDAO = new CommunityDAO();
        return communityDAO.isUserAdminOfCommunity(id, communityId);
    }

    public boolean isPinned() {
        return postDAO.isPinned(post.getId(), post.getCommunityId());
    }
}
