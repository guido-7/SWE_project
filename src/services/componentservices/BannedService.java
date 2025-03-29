package src.services.componentservices;

import src.domainmodel.User;
import src.persistence.DAOs.UserDAO;
import src.usersession.GuestContext;

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
