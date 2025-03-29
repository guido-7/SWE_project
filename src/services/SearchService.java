package src.services;

import src.domainmodel.Community;
import src.domainmodel.Post;
import src.domainmodel.User;
import src.persistence.DAOs.CommunityDAO;
import src.persistence.DAOs.PostDAO;
import src.persistence.DAOs.SubscriptionDAO;
import src.usersession.GuestContext;

import java.util.List;

public class SearchService {
    private final CommunityDAO communityDAO = new CommunityDAO();;
    private final PostDAO postDAO = new PostDAO();

    public SearchService() {}

    public List<Community> searchCommunities(String query) {
        return communityDAO.searchByTitle(query);
    }

    public List<Post> searchPosts(String query, int communityId) {
        return postDAO.searchByTitle(query, communityId);
    }

    // TODO: review
    public List<Community> searchSubscribedCommunities(String query) {
        User user = (User) GuestContext.getCurrentGuest();
        return SubscriptionDAO.getSubscribedCommunities(query,user.getId());
    }

}
