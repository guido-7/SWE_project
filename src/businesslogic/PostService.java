package src.businesslogic;

import src.domainmodel.Comment;
import src.domainmodel.Post;
import src.orm.CommentDAO;
import src.orm.CommunityDAO;
import src.orm.PostDAO;
import src.orm.UserDAO;

import java.sql.SQLException;
import java.util.List;

public class PostService {
    Post post;
    PostDAO postDAO = new PostDAO();

    public PostService(Post post) throws SQLException {
        this.post = post;
    }

    public Post getPost(){
        return post;
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
}
