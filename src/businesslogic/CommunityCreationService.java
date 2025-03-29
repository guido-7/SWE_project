package src.businesslogic;

import javafx.scene.control.Label;
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
    Label errorLabel;

    public int createCommunity(String title, String description) throws SQLException {
        String validationMessage = validateInput(title, description);
        if(validationMessage != null){
            showError(errorLabel, validationMessage);
            return -1;
        }
        int communityId = communityDAO.save(Map.of("title",title,"description",description));
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

    private String validateInput(String title, String description) {
        if (title.isEmpty() && description.isEmpty()) return "Please enter both: title and description.";
        if (title.isEmpty()) return "Please enter title.";
        if (description.isEmpty()) return "Please enter description.";
        if (communityDAO.findByTitle(title)) return "A community with this name already exists.";
        return null;
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setOpacity(1);
        errorLabel.setText(message);
    }

    public void setErrorLabel(Label errorLabel) {
        this.errorLabel = errorLabel;
    }

}
