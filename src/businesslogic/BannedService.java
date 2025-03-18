package src.businesslogic;

import src.domainmodel.User;
import src.orm.UserDAO;
import src.servicemanager.GuestContext;

import java.util.Map;

public class BannedService {
    private final int communityId;
    private final UserDAO userDAO = new UserDAO();

    public BannedService(int communityId) {
        this.communityId = communityId;
    }

    public Map<String,String> getBannedInfo(){
        User user = (User) GuestContext.getCurrentGuest();
        return userDAO.getBannedInfo(user.getId(), communityId);
    }

}
