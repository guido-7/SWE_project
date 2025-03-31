package src;

import src.controllers.pagecontrollers.UserProfilePageController;
import src.domainmodel.*;
import src.persistence.DAOs.CommunityDAO;
import src.persistence.DAOs.SubscriptionDAO;
import src.persistence.DAOs.UserDAO;
import src.services.FeedService;
import test.functionaltest.FunctionalTest;
import test.testutils.ContinuousPDFChart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;

public class testfeed {

    public static void main(String[] args) throws SQLException, IOException {
        //create directory for saving data
        if (new File("charts").mkdirs()) {
            System.out.println("Directory created");
        }
        else {
            throw new IOException("Directory already exists");
        }
        //preset
        FunctionalTest.seDB();
        User user = createTestUser();

        //data
        int maxCommunityId = 12;
        int limitSubscriptions = maxCommunityId/2;
        ArrayList<Double> means = new ArrayList<>();
        ArrayList<Double> variances = new ArrayList<>();

        for ( int iteration = 1 ; iteration <= limitSubscriptions ; iteration++){
            List<Double> percentages = new ArrayList<>();

        for(int i = 1; i <= 100 ; i++) {
            Set<Integer> uniqueCommunityIndexes = getUniqueCommunityIndexes(iteration, maxCommunityId);
            subscribeToUniqueCommunities(user, uniqueCommunityIndexes);
            double percentage = getPercentageOfPostsWhenUserSubscribedTo_i_communities(user, uniqueCommunityIndexes);
            percentages.add(percentage);
            unsubscribeFromUniqueCommunities(user, uniqueCommunityIndexes);

        }
        means.add(calculateMean(percentages));
        variances.add(calculateVariance(percentages, means.getFirst()));
        double[][] samples = ContinuousPDFChart.computeKDE(percentages, 0.05, 50);
        ContinuousPDFChart.createPDFChart("testfeed_"+ iteration, samples);
        }
        saveStatsToCSVfile(means,variances);


    }

    private static void saveStatsToCSVfile(ArrayList<Double> means, ArrayList<Double> variances) {
            File csvOutputFile = new File("charts/stats.csv");
            try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                for (int i = 0; i < means.size(); i++) {
                    pw.println(means.get(i) + "," + variances.get(i));
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
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
        while (uniqueCommunityIndexes.size() != limitSubscriptions) {
            int randomIndex = random.nextInt(maxCommunityId) + 1;
            uniqueCommunityIndexes.add(randomIndex);
        }
        return uniqueCommunityIndexes;
    }
    public static double getPercentageOfPostsWhenUserSubscribedTo_i_communities(User user,Set<Integer> uniqueCommunityIndexes) throws SQLException {
        // user gets feed
        List<Post> posts = new FeedService(user).getFeed();
        System.out.println("Total posts: " + posts.size());
        int totalPosts = posts.size();

        // count posts from subscribed communities
        int postCountFromCommunity = 0;
        for(Post post : posts) {
            if(uniqueCommunityIndexes.contains(post.getCommunityId())) {
                postCountFromCommunity++;
            }
        }
        double percentage = (double) postCountFromCommunity / totalPosts;
        System.out.println("Percentage: " + percentage);
        return percentage;
    }

    public static double calculateMean(List<Double> data) {
        return data.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public static double calculateVariance(List<Double> data, double mean) {
        double sumSquaredDiffs = data.stream()
                .mapToDouble(x -> Math.pow(x - mean, 2))
                .sum();
        return sumSquaredDiffs / (data.size()-1);
    }

}
