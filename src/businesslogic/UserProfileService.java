package src.businesslogic;

import src.domainmodel.Post;
import src.domainmodel.User;
import src.orm.PostDAO;
import src.orm.UserDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class UserProfileService {
    private final User user ;
    private final UserDAO userDAO = new UserDAO();
    private final PostDAO postDAO = new PostDAO();

    public UserProfileService(User user) {
        this.user = user;
    }

    public String getDescription() throws SQLException {
        return (String) userDAO.retrieveSingleAttribute("UserDescription", "description", "user_id = ?", user.getId());
    }

    public void SaveDescription(String description) throws SQLException {
        String [] columns = {"user_id","description"};
        userDAO.insertMultipleValues("UserDescription", columns, user.getId(), description);
    }

    public void updateDescription(String description) throws SQLException {
        userDAO.updatesingleAttribute("UserDescription", "description", description, "user_id = ?", user.getId());
    }

    public List<Post> getUserPosts() {
        return postDAO.getPostsByUser(user.getId());
    }

    public List<Post> getSavedPosts() {
        return postDAO.getSavedPosts(user.getId(),10,0);
    }

    public Map<String,String> getUserInfo() throws SQLException {
        return userDAO.getUserInfo(user.getId());
    }

}
