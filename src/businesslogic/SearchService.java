package src.businesslogic;

import src.domainmodel.Community;
import src.domainmodel.Post;
import src.orm.CommunityDAO;
import src.orm.PostDAO;

import java.util.List;

public class SearchService {
    private final CommunityDAO communityDAO;
    private final PostDAO postDAO;

    public SearchService() {
        this.communityDAO = new CommunityDAO();
        this.postDAO = new PostDAO();
    }

    public List<Community> searchCommunities(String query) {
        return communityDAO.searchByTitle(query);
    }
}
