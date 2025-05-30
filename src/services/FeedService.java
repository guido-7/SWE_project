package src.services;

import src.domainmodel.*;
import src.persistence.DAOs.*;

import java.util.*;

public class FeedService {
    private final Map<Integer, Integer> community_partition = new LinkedHashMap<>();// at the end of the loading of feed this should be in
                                                                        // the form of community_id,number of posts from that community
    private final PostDAO postDAO = new PostDAO();
    private final CommunityDAO communityDAO = new CommunityDAO();
    private final SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CommentDAO commentDAO = new CommentDAO();
    private final Map<Integer,Integer> noOfPostsTaken = new LinkedHashMap<>();
    private final Set<Integer> seenPosts = new HashSet<>();

    Guest guest;

    int numberofPosts = 30;
    int numberofCommunities = 10;

    public FeedService(Guest guest){
        this.guest = guest;
    }

    public List<Post> getFeed() {
        community_partition.clear();
        if (guest.getRole() == Role.GUEST){
            return GuestFeed();
        } else {
            //10: 4 3 2 1
            ArrayList<ArrayList<Integer>> community_ids = new ArrayList<>();

            //join tables and get the community ids
            //takes the ids of the communities to which the user is subscribed
            ArrayList<Integer> subscription_C_ids = subscriptionDAO.getCommunityIds(((User)guest).getId(),(int)(numberofCommunities * 0.4));
            community_ids.add(subscription_C_ids);
            //System.out.println("Length of sub : " + subscription_C_ids.size());

            // uses PostVotes table and use aggregate operator to get the community ids
            ArrayList<Integer> votes_C_ids = userDAO.getMostLikedCommunityIds(((User)guest).getId(),(int)(numberofCommunities * 0.3));
            community_ids.add(votes_C_ids);
            //System.out.println("Length of votes: " + votes_C_ids.size());

            //takes the ids of the communities to which the user has commented
            ArrayList<Integer> comment_C_ids = commentDAO.getCommunityIds(((User)guest).getId(),(int)(numberofCommunities * 0.2 ));
            community_ids.add(comment_C_ids);
            //System.out.println("Length of commentc: " + comment_C_ids.size());

            // utilize scores and visits of all communities
            ArrayList<Integer> community_C_ids = communityDAO.getCommunityIds((int)(numberofCommunities * 0.1 ));
            community_ids.add(community_C_ids);
            //System.out.println("Length of community : " + community_C_ids.size());


            //merge
            ArrayList<Integer> mergedList = new ArrayList<>();

            for ( ArrayList<Integer> list : community_ids){
                mergedList.addAll(list);
            }
            //System.out.println("Length of merged list : " + mergedList.size());

            // 1, 2, 3, 2, 3, 5, 7, 1
            // 1 : 1 , 2 : 1 ,3:1,

            Map<Integer, Integer> idCountMap = new LinkedHashMap<>();
            for (Integer id : mergedList) {
                idCountMap.put(id, idCountMap.getOrDefault(id, 0) + 1);
            }
            //System.out.println("Length of idCount: " + idCountMap.size());
            // we take all necessary information from the database about the weights of the communities
            Map<Integer, Double> scores = communityDAO.getScore(idCountMap);

            //normalization
            double sum = scores.values().stream().mapToDouble(Double::doubleValue).sum();
            scores.replaceAll((k, v) -> v / sum);

            //System.out.println("Length of scores: " + scores.size());
            // we obtain the partition of the posts in the following form (community_id,number of posts from that community)
            getPartition(scores);

            List<Post>posts = getPostsFromCommunity();
            System.out.println("Total Posts from original feed: " + posts.size());
            return posts;
        }
    }
    // 345 : 122,56 , 333 : 123 , 566 : 124 --->scores  se io facessi get(0) mi darebbe 0
    // 345 , 333, 566 --->community_ids --->get(0)=345 get(get(0))-->get(345)
    //
    private void getPartition(Map<Integer,Double> scores){
        int maxKey = findmaxKey(scores);
        //1 : 1      2 : 2   4:7

        List<Integer> community_ids = new ArrayList<>(scores.keySet());

        for (int i = 0; i < scores.size(); i++) {
            int value;
            if (community_ids.get(i) == maxKey) {
                value = (int) Math.floor(scores.get(community_ids.get(i)) * numberofPosts) + 1;
                // nel rigo sotto
            } else {
                value = (int) Math.floor(scores.get(community_ids.get(i)) * numberofPosts);
            }
            community_partition.put(community_ids.get(i), value);
        }
    }

    private int findmaxKey(Map<Integer, Double> list) {
        int maxKey = -1;
        double maxValue = Double.NEGATIVE_INFINITY;

        for (Map.Entry<Integer, Double> entry : list.entrySet()) {
            if (entry.getValue() > maxValue) {
                maxValue = entry.getValue();
                maxKey = entry.getKey();
            }
        }
        return maxKey;
    }

    private List<Post> getPostsFromCommunity() {
        List<Post> posts = new ArrayList<>();
        List<Integer> keys = new ArrayList<>(community_partition.keySet());
        Collections.shuffle(keys);

        for (Integer communityId : keys) {
            Integer postCount = community_partition.get(communityId);
            List<Post> communityPosts = postDAO.getPosts(communityId, postCount, 0);
            System.out.println("Community ID: " + communityId + ", Number of Posts: " + communityPosts.size());
            noOfPostsTaken.put(communityId,communityPosts.size());
            // 2 : 4
            posts.addAll(communityPosts);
        }
        return posts;
    }
    public List<Post> getNextFeed(){
        List<Post> posts = new ArrayList<>();
        List<Integer> keys = new ArrayList<>(community_partition.keySet());

        for (Integer communityId : keys) {
            Integer postCount = community_partition.get(communityId);
            List<Post> communityPosts = postDAO.getPosts(communityId, postCount,noOfPostsTaken.get(communityId));
            System.out.println("Community ID: " + communityId + ", Number of Posts: " + communityPosts.size());
            noOfPostsTaken.put(communityId, noOfPostsTaken.getOrDefault(communityId, 0) + communityPosts.size());
            if(communityPosts.isEmpty()){
                community_partition.remove(communityId);
            }
            posts.addAll(communityPosts);
        }
        for(Post post : posts){
            if(seenPosts.contains(post.getId())){
                posts.remove(post);
            }
            else{
                seenPosts.add(post.getId());
            }
        }
        return posts;
    }
    // 120 : 23  , 121 : 22 , 122 : 10 , 123 : 23 , 124 : 23

    public Guest getGuest() {
        return guest;
    }

    private List<Post> GuestFeed(){
        int maxPostRetrieved = 3 ;
        List<Integer> bestCommunityIds = communityDAO.getCommunityIds(numberofCommunities);
        Collections.shuffle(bestCommunityIds);

        for(Integer id : bestCommunityIds){
            community_partition.put(id,(int)(Math.random()* maxPostRetrieved)+1 );
        }
        return getPostsFromCommunity();
    }
}
