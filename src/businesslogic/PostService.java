
package src.businesslogic;

import src.domainmodel.*;
import src.orm.CommentDAO;
import src.orm.CommunityDAO;
import src.orm.PostDAO;
import src.orm.UserDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PostService {
    private Post post;
    private Integer vote;
    private PostDAO postDAO = new PostDAO();


    public PostService(Post post) throws SQLException {
        this.post = post;
    }

    public Post getPost(){
        return post;
    }

    private boolean canLike(Guest guest) {
        return guest.getRole() != Role.GUEST;
    }

    public void toggleLike(Guest guest) throws SQLException {
        toggleLikeDislike(guest,"like");
    }

    public void toggleDislike(Guest guest) throws SQLException {
        toggleLikeDislike(guest,"dislike");
    }

    private void toggleLikeDislike(Guest guest, String likelihood) throws SQLException {
        UserDAO userDAO = new UserDAO();
        if (!canLike(guest)) return;
        User currentUser = (User)guest;
        Integer userVote = getVote(currentUser);
        switch (likelihood) {
            case "like":
                // userVote == 1
                if (userVote == null || userVote == 0){
                    //voteInfo.put("post_type", userVote);
                    Map<String, Object> voteInfo = Map.of("user_id", currentUser.getId(), "post_id", post.getId(), "community_id", post.getCommunityId(), "vote_type", 1);
                    userDAO.insertPostVotes(voteInfo); // PostVotes
                } else {
                    Map<String, Object> voteInfo = Map.of("user_id", currentUser.getId(), "post_id", post.getId());
                    userDAO.removePostVotes(voteInfo);
                }
                break;
            case "dislike":
                //if (userVote == 0)
                if (userVote == null || userVote == 1) {
                    //voteInfo.put("post_type", userVote);
                    Map<String, Object> voteInfo = Map.of("user_id", currentUser.getId(), "post_id", post.getId(), "community_id", post.getCommunityId(), "vote_type", 0);
                    userDAO.insertPostVotes(voteInfo);
                } else {
                    Map<String, Object> voteInfo = Map.of("user_id", currentUser.getId(), "post_id", post.getId());
                    userDAO.removePostVotes(voteInfo);
                }
                break;
        }
    }

    private Integer getVote(User user) throws SQLException {
        UserDAO userDAO = new UserDAO();
        return userDAO.getVote(user.getId(),post.getId());
    }

    public String getCommunityTitle() throws SQLException {
        CommunityDAO communityDAO = new CommunityDAO();
        return (String) communityDAO.retrieveSingleAttribute("Community","title","id = ?" , post.getCommunityId());
    }

    public String getnickname() throws SQLException {
        UserDAO userDAO = new UserDAO();
        return (String) userDAO.retrieveSingleAttribute("User","nickname","id = ?", post.getUserId());
    }

    public List<Comment> getRootComments() {
        CommentDAO commentDAO = new CommentDAO();
        return commentDAO.getRootComments(post,10,0);
    }




    public void refreshPost() {
        try {
            post = postDAO.retrievePost(post.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
