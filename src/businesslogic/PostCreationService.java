package src.businesslogic;

import src.orm.PostDAO;

import java.sql.SQLException;
import java.util.Map;

public class PostCreationService {
    PostDAO postDAO = new PostDAO();

    public void createPost(String title, String content, int communityId, int userId) throws SQLException {
        postDAO.save(Map.of("title", title, "content", content, "community_id", communityId, "user_id", userId));
    }
}
