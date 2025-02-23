package src.businesslogic;

import src.domainmodel.Community;
import src.domainmodel.Guest;
import src.domainmodel.Post;
import src.domainmodel.User;
import src.orm.CommunityDAO;
import src.orm.PostDAO;
import src.orm.SubscriptionDAO;
import src.servicemanager.GuestContext;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SearchService {
    private final CommunityDAO communityDAO;
    private final PostDAO postDAO;
    private final SubscriptionDAO subscriptionDAO;

    public SearchService() {
        this.communityDAO = new CommunityDAO();
        this.postDAO = new PostDAO();
        this.subscriptionDAO = new SubscriptionDAO();
    }

    public List<Community> searchCommunities(String query) {
        return communityDAO.searchByTitle(query);
    }

    public List<Post> searchPosts(String query, int communityId) {
        return postDAO.searchByTitle(query, communityId);
    }

    public void subscribeCommunity(Community community) throws SQLException {
        User user = (User) GuestContext.getCurrentGuest();
        subscriptionDAO.subscribe(user.getId(), community.getId());

    }

    public List<Community> searchSubscribedCommunities(String query) {
        User user = (User) GuestContext.getCurrentGuest();
        return SubscriptionDAO.getSubscribedCommunities(query,user.getId());

    }
}
