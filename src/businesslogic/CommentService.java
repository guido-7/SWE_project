package src.businesslogic;

import src.domainmodel.Comment;
import src.domainmodel.Post;
import src.orm.CommentDAO;
import src.orm.UserDAO;

import java.util.List;

public class CommentService {
    Comment comment;
    UserDAO userDAO = new UserDAO();
    CommentDAO commentDAO = new CommentDAO();

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

    public List<Comment> getCommentsByLevel(){
        List<Comment> commentsByLevel = commentDAO.getCommentsByLevel(comment);
        System.out.println("New " + commentsByLevel.size() + " comments loaded");
        return commentsByLevel;
    }

    public boolean hasSubComments() {
        if(!commentDAO.getCommentsIdsByLevel(comment).isEmpty()) {
            return true;
        }
        return false;
    }
}
