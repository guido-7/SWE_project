package src;

import src.controllers.pagecontrollers.UserProfilePageController;
import src.domainmodel.*;
import src.persistence.DAOs.CommunityDAO;
import src.persistence.DAOs.SubscriptionDAO;
import src.persistence.DAOs.UserDAO;
import src.services.FeedService;
import test.functionaltest.FunctionalTest;

import java.sql.SQLException;
import java.util.*;

public class testfeed {
    public static void main(String[] args) throws SQLException {
        //preset
        FunctionalTest.seDB();
        User user = createTestUser();

        //data
        int maxCommunityId = 12;
        int limitSubscriptions = maxCommunityId/2;

        for(int i = 1; i <= limitSubscriptions; i++) {

            // user subscribes to i communities
            Set<Integer> uniqueCommunityIndexes = getUniqueCommunityIndexes(i, maxCommunityId);
            subscribeToUniqueCommunities(user, uniqueCommunityIndexes);

            // user gets feed
            List<Post> posts = new FeedService(user).getFeed();
            int totalPosts = posts.size();

            // count posts from subscribed communities
            int postCountFromCommunity = 0;
            for(Post post : posts) {
                if(uniqueCommunityIndexes.contains(post.getCommunityId())) {
                    postCountFromCommunity++;
                }
            }
            double percentage = (double) postCountFromCommunity / totalPosts * 100;
            System.out.println("Percentage: " + percentage);




    }
    }
    private static User createTestUser() throws SQLException {
        String nickname = "nicknameTest";
        String name = "userTest";
        String surname = "surnameTest";
        String password = "passwordTest";
        UserDAO userDAO = new UserDAO();
        userDAO.save(Map.of("nickname", nickname, "name", name, "surname", surname));
        int id = userDAO.getUserId(nickname);
        userDAO.registerUserAccessInfo(id, nickname, password);
        return new User(id, nickname, name, surname, PermitsManager.createUserPermits(), Role.USER);
    }
    public static void subscribeToUniqueCommunities(User user, Set<Integer> uniqueCommunityIndexes) throws SQLException {
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
        for (int index : uniqueCommunityIndexes) {
            System.out.println("Subscibed to Community "+ index);
            subscriptionDAO.subscribe(user.getId(), index);
        }
    }
    public static void unsubscribeFromUniqueCommunities(User user, Set<Integer> uniqueCommunityIndexes) throws SQLException {
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
        for (int index : uniqueCommunityIndexes) {
            System.out.println("Unsubscibed from Community "+ index);
            subscriptionDAO.unsubscribe(user.getId(), index);
        }
    }
    public static Set<Integer> getUniqueCommunityIndexes(int limitSubscriptions, int maxCommunityId) {
        Set<Integer> uniqueCommunityIndexes = new HashSet<>();
        Random random = new Random();
        while (uniqueCommunityIndexes.size() < limitSubscriptions) {
            int randomIndex = random.nextInt(maxCommunityId) + 1;
            uniqueCommunityIndexes.add(randomIndex);
        }
        return uniqueCommunityIndexes;
    }
}
