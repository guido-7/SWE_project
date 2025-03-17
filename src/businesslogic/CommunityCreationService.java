package src.businesslogic;

import src.domainmodel.User;
import src.orm.AdminDAO;
import src.orm.CommunityDAO;
import src.orm.SubscriptionDAO;
import src.servicemanager.GuestContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class CommunityCreationService {
    CommunityDAO communityDAO = new CommunityDAO();
    SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
    AdminDAO adminDAO = new AdminDAO();

    public int createCommunity(String title, String description) throws SQLException {
        int communityId =communityDAO.save(Map.of("title",title,"description",description));
        System.out.println(communityId);
        subscribeToCommunity(communityId);
        addAdmin(communityId);
        return communityId;
    }

    public void saveRules(int CommunityId,Map<Integer,ArrayList<String>> rulesMapping) {
        communityDAO.saveRules(CommunityId,rulesMapping);
    }

    private void subscribeToCommunity(int communityId) throws SQLException {
        int userId = ((User) (GuestContext.getCurrentGuest())).getId();
        subscriptionDAO.save(Map.of("user_id",userId,"community_id",communityId));
    }

    private void addAdmin(int communityId) throws SQLException {
        int userId = ((User) (GuestContext.getCurrentGuest())).getId();
        adminDAO.save(Map.of("user_id",userId,"community_id",communityId));
    }
}
