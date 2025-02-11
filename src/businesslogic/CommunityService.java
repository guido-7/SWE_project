package src.businesslogic;

import src.domainmodel.*;
import src.orm.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommunityService {

    PostDao postDao = new PostDao();
    int  noOfPostsTaken;
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
    public ArrayList<PostWarnings> getPostWarnings() {
        CommunityDAO communityDAO = new CommunityDAO();
        return communityDAO.getPostWarnings(communityId);
    }
    public ArrayList<CommentWarnings> getCommentWarnings() {
        CommunityDAO communityDAO = new CommunityDAO();
        return communityDAO.getCommentWarnings(communityId);
    }


}
