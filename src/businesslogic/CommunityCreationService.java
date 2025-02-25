package src.businesslogic;

import src.orm.CommunityDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class CommunityCreationService {

    CommunityDAO communityDAO = new CommunityDAO();

    public int createCommunity(String title, String description) throws SQLException {
        int id =communityDAO.save(Map.of("title",title,"description",description));

        System.out.println(id);
        return id;
    }

    public void saveRules(int CommunityId,Map<Integer,ArrayList<String>> rulesMapping) {
        communityDAO.saveRules(CommunityId,rulesMapping);
    }
}
