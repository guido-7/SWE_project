package src.businesslogic;

import src.domainmodel.*;
import src.orm.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommunityService {

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
}
