package src.businesslogic;

import src.domainmodel.*;
import src.orm.*;
import src.servicemanager.GuestContext;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommunityService {
    int communityId;
    int noOfPostsTaken;
    int numberOfPosts = 30;
    PostDAO postDao = new PostDAO();
    CommunityDAO communityDAO = new CommunityDAO();

    public CommunityService(int communityId) {
        this.communityId = communityId;
    }

    public List<Post> getPosts() {
        List<Post> communityPosts = postDao.getPosts(communityId, numberOfPosts, 0);
        System.out.println("Community ID: " + communityId + ", Number of Posts: " + communityPosts.size());
        noOfPostsTaken = communityPosts.size();
        return communityPosts;
    }

    public List<Post> getNextPosts() {
        List<Post> communityPosts = postDao.getPosts(communityId, numberOfPosts, noOfPostsTaken);
        System.out.println("Community ID: " + communityId + ", Number of Posts: " + communityPosts.size());
        noOfPostsTaken = communityPosts.size();
        return communityPosts;
    }

    public List<Post> getFilteredPosts(String searchTerm) {
        // Resetta il contatore quando si fa una nuova ricerca
        noOfPostsTaken = 0;
        return postDao.getFilteredPosts(communityId, searchTerm, numberOfPosts, 0);
    }

    public List<Post> getNextFilteredPosts(String searchTerm) {
        List<Post> filteredPosts = postDao.getFilteredPosts(communityId, searchTerm, numberOfPosts, noOfPostsTaken);
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

    public Moderator getModerator(int moderatorId) throws SQLException {
        ModeratorDAO moderatorDAO = new ModeratorDAO();
        return moderatorDAO.getCommunityModerator(moderatorId, communityId);
    }

    public boolean isSubscribed() {
        Guest guest =  GuestContext.getCurrentGuest();
        if(guest.getRole() == Role.GUEST) {
            return false;
        }
        User user = (User) guest;
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
        return subscriptionDAO.isSubscribed(user.getId(),communityId);
    }

    public boolean subscribe() throws SQLException {
        Guest guest =  GuestContext.getCurrentGuest();
        if(guest.getRole() == Role.GUEST)
            return false;
        User user = (User) guest;
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
        subscriptionDAO.subscribe(user.getId(), communityId);
        return true;
    }

    public boolean unsubscribe() throws SQLException {
        Guest guest =  GuestContext.getCurrentGuest();
        if(guest.getRole() == Role.GUEST)
            return false;
        User user = (User) guest;
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
        subscriptionDAO.unsubscribe(user.getId(), communityId);
        return true;
    }

    public User getUser(int userId) throws SQLException {
        UserDAO userDAO = new UserDAO();
        return userDAO.findById(userId).orElse(null);
    }

    public void timeOutUser(int reportedId, LocalDateTime time) {
        communityDAO.timeOutUser(reportedId,communityId ,time);
    }

    public void banUser(int reportedId,String banReason) {
        communityDAO.banUser(reportedId,communityId,banReason);
    }

    public boolean checkBannedUser() {
        Guest guest =  GuestContext.getCurrentGuest();
        if(guest.getRole() == Role.GUEST)
            return false;
        User user = (User) guest;
        return communityDAO.isBanned(user.getId(),communityId);
    }

    public void removeWarnings(ArrayList<PostWarnings> reports) {
        communityDAO.removeWarnings(reports,communityId);
    }
}
