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

    public int createCommunity(String title, String description) throws SQLException {
        int communityId =communityDAO.save(Map.of("title",title,"description",description));
        System.out.println(communityId);
        SubscribeToCommunity(communityId);
        AddAdmin(communityId);
        return communityId;
    }

    public void saveRules(int CommunityId,Map<Integer,ArrayList<String>> rulesMapping) {
        communityDAO.saveRules(CommunityId,rulesMapping);
    }

    private void SubscribeToCommunity(int communityId) throws SQLException {
        int userId = ((User) (GuestContext.getCurrentGuest())).getId();
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
        subscriptionDAO.save(Map.of("user_id",userId,"community_id",communityId));
    }

    private void AddAdmin(int communityId) throws SQLException {
        int userId = ((User) (GuestContext.getCurrentGuest())).getId();
        AdminDAO adminDAO = new AdminDAO();
        adminDAO.save(Map.of("user_id",userId,"community_id",communityId));
    }
}
