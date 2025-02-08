package src.businesslogic;

import src.domainmodel.Community;
import src.orm.CommunityDAO;

import java.util.List;

public class SearchCommunityService {
    private final CommunityDAO communityDAO;

    public SearchCommunityService() {
        this.communityDAO = new CommunityDAO();
    }

    public List<Community> searchCommunities(String query) {
        // Puoi aggiungere ulteriori controlli o logica (es. normalizzazione della stringa)
        return communityDAO.searchByTitle(query);
    }
}
