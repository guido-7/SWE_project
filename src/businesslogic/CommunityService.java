package src.businesslogic;

import src.domainmodel.*;
import src.orm.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommunityService {

    PostDao postDao = new PostDao();
    CommunityDAO communityDAO = new CommunityDAO();
    int  noOfPostsTaken;
    PostDAO postDao = new PostDAO();
    int noOfPostsTaken;
    int communityId;
    int numberofPosts = 30;

    public CommunityService(int communityId) {
        this.communityId = communityId;
    }

    public List<Post> getPosts() {
        List<Post> communityPosts = postDao.getPosts(communityId, numberofPosts, 0);
        System.out.println("Community ID: " + communityId + ", Number of Posts: " + communityPosts.size());
        noOfPostsTaken = communityPosts.size();
        return communityPosts;
    }

    public List<Post> getNextPosts() {
        List<Post> communityPosts = postDao.getPosts(communityId, numberofPosts, noOfPostsTaken);
        System.out.println("Community ID: " + communityId + ", Number of Posts: " + communityPosts.size());
        noOfPostsTaken = communityPosts.size();
        return communityPosts;
    }

    public List<Post> getFilteredPosts(String searchTerm) {
        // Resetta il contatore quando si fa una nuova ricerca
        noOfPostsTaken = 0;
        return postDao.getFilteredPosts(communityId, searchTerm, numberofPosts, 0);
    }

    public List<Post> getNextFilteredPosts(String searchTerm) {
        List<Post> filteredPosts = postDao.getFilteredPosts(communityId, searchTerm, numberofPosts, noOfPostsTaken);
        noOfPostsTaken += filteredPosts.size();
        return filteredPosts;
    }

    public ArrayList<PostWarnings> getPostWarnings() {
        CommunityDAO communityDAO = new CommunityDAO();
        return communityDAO.getPostWarnings(communityId);
    }

    public ArrayList<CommentWarnings> getCommentWarnings() {
        CommunityDAO communityDAO = new CommunityDAO();
        return communityDAO.getCommentWarnings(communityId);
    }

    public ArrayList<PostWarnings> getWarnings(){
        ArrayList<PostWarnings> postWarnings = getPostWarnings();
        ArrayList<CommentWarnings> commentWarnings = getCommentWarnings();
        ArrayList<PostWarnings> warnings = new ArrayList<>();
        warnings.addAll(postWarnings);
        warnings.addAll(commentWarnings);
        return warnings;
    }

    public int getCommunityId() {
        return communityId;
    }

    public boolean isModerator(int moderatorId) {
        ModeratorDAO moderatorDAO = new ModeratorDAO();
        return moderatorDAO.isModerator(moderatorId, communityId);
    }
    public int getCommunityId() {
        return communityId;
    }
    public Community getCommunity() throws SQLException {
        return communityDAO.findById(communityId).orElse(null);
    }
    public List<Rule> getCommunityRules(int communityId) {
        return communityDAO.getCommunityRules(communityId);
    }



}
