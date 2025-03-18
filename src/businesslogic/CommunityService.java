package src.businesslogic;

import src.domainmodel.*;
import src.orm.*;
import src.servicemanager.GuestContext;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommunityService {
    int communityId;
    int noOfPostsTaken;
    int numberOfPosts = 30;
    private final PostDAO postDao = new PostDAO();
    private final RulesDAO rulesDAO = new RulesDAO();
    private final CommunityDAO communityDAO = new CommunityDAO();
    private final SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AdminDAO adminDAO = new AdminDAO();
    private final ModeratorDAO moderatorDAO = new ModeratorDAO();
    int offset = 0;

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
        noOfPostsTaken += communityPosts.size();
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
        return communityDAO.getPostWarnings(communityId);
    }

    public ArrayList<CommentWarnings> getCommentWarnings() {
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

    public Community getCommunity() throws SQLException {
        return communityDAO.findById(communityId).orElse(null);
    }

    public List<Rule> getCommunityRules() {
        return communityDAO.getCommunityRules(communityId);
    }

    public boolean isSubscribed() {
        Guest guest =  GuestContext.getCurrentGuest();
        if(guest.getRole() == Role.GUEST) {
            return false;
        }
        User user = (User) guest;
        return subscriptionDAO.isSubscribed(user.getId(),communityId);
    }

    public boolean subscribe() throws SQLException {
        Guest guest =  GuestContext.getCurrentGuest();
        if(guest.getRole() == Role.GUEST)
            return false;
        User user = (User) guest;
        subscriptionDAO.subscribe(user.getId(), communityId);
        return true;
    }

    public boolean unsubscribe() throws SQLException {
        Guest guest =  GuestContext.getCurrentGuest();
        if(guest.getRole() == Role.GUEST)
            return false;
        User user = (User) guest;
        subscriptionDAO.unsubscribe(user.getId(), communityId);
        return true;
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

    public void addRule(String title, String content, int priority) {
        //change priority to other Rule i.e. if I add a rule of priority 1 ; the actual rule with
        // priority 1 will be changed to 2
        //maybe add a trigger for that in the database
        communityDAO.addRule(communityId, title, content, priority);
    }

    public int getLastPriority() {
        return communityDAO.getLastPriority(communityId);
    }

    public void deleteRule(int ruleId) throws SQLException {
        ArrayList<Integer> primaryKeysIds = new ArrayList<>(List.of(ruleId,communityId));
        rulesDAO.deleteById(primaryKeysIds);
    }

    public void deleteCommunity() throws SQLException {
        communityDAO.deleteById(communityId);
    }

    public List<Integer> getPinnedPosts() {
        return postDao.getPinnedPosts(communityId);
    }

    public String getPostTitle(Integer pinnedPostId) throws SQLException {
        return (String) postDao.retrieveSingleAttribute("Post", "title", "id = ?", pinnedPostId);
    }

    public Post getPost(Integer postId) throws SQLException {
        return postDao.findById(postId).orElse(null);
    }

    public Object[][] getSubscribedData(ArrayList<Integer> subIds) throws SQLException {
        Object[][] subsInfos = new Object[subIds.size()][2];
        int i = 0 ;
        for( Integer subId : subIds){
            Object[] subInfo = new Object[2];
            subInfo[0] = userDAO.retrieveSingleAttribute("User","nickname"," id = ? ",subId);
            subInfo[1] = userDAO.retrieveSingleAttribute("Subscription","subscription_date","user_id = ?",subId);
            subsInfos[i++] = subInfo;
        }
        return subsInfos;
    }

    public ArrayList<Integer> getSubs(int maxSubscribedNo) {
        ArrayList<Integer> subsIds = subscriptionDAO.getSubs(maxSubscribedNo, communityId,offset);
        offset += subsIds.size();
        return subsIds;
    }

//    public Pair<Integer, Object[]> getMoreSubs() {
//        SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
//
//        return subscriptionDAO.getSubs(, communityId);
//    }

    public void promote(int subscriberId) throws SQLException {
        moderatorDAO.save(Map.of("user_id",subscriberId,"community_id",communityId));
    }

    public void dismiss(int subscriberId) throws SQLException {
        moderatorDAO.deleteById(subscriberId);
    }

    public Map<Integer,String> getFilteredSubs(String searchTerm) {
        noOfPostsTaken = 0;
        int maxnumberOfSubsShown = 10;
        return subscriptionDAO.getFilteredSubs(communityId, searchTerm, maxnumberOfSubsShown, 0);

    }
    public boolean isModerator(int moderatorId) {
        return moderatorDAO.isModerator(moderatorId, communityId);
    }

    public User getUser(int userId) throws SQLException {
        return userDAO.findById(userId).orElse(null);
    }

    public Moderator getModerator(int moderatorId) {
        return moderatorDAO.getCommunityModerator(moderatorId, communityId);
    }

    public Admin getAdmin(int userId) throws SQLException {
        boolean isAdmin = adminDAO.isAdmin(userId,communityId);
        if (!isAdmin)
            return null;
        return adminDAO.findById(userId).orElse(null);
    }

}
