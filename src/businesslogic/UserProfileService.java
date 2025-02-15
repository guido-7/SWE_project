package src.businesslogic;

import src.domainmodel.Post;
import src.domainmodel.User;
import src.orm.PostDAO;
import src.orm.UserDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserProfileService {
    User user ;
    UserDAO userDAO = new UserDAO();

    public UserProfileService(User User) {
        this.user = User;
    }

    public String getDescription() throws SQLException {
        return (String) userDAO.retrieveSingleAttribute("UserDescription", "description", "user_id = ?", user.getId());
    }
    public void SetDescription(String description) throws SQLException {
        userDAO.insertSingleAttribute("UserDescription", "description",description);
    }
    public void updateDescription(String description) throws SQLException {
        userDAO.updatesingleAttribute("UserDescription", "description", description, "user_id = ?", user.getId());
    }
    public List<Post> getUserPosts() throws SQLException {
        PostDAO postDAO = new PostDAO();
        return postDAO.getPostsByUser(user.getId());
    }
    public List<Post> getSavedPosts() throws SQLException {
        PostDAO postDAO = new PostDAO();
        return postDAO.getSavedPosts(user.getId(),10,0);
    }
    public Map<String,String> getUserInfo() throws SQLException {
        return userDAO.getUserInfo(user.getId());
    }
}
