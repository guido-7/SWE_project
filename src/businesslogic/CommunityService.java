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
