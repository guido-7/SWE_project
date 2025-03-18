package src.businesslogic;

import javafx.scene.control.Label;
import src.domainmodel.User;
import src.orm.UserDAO;
import src.servicemanager.GuestContext;
import src.servicemanager.FormattedTime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class BannedService {
    private final int communityId;
    UserDAO userDAO = new UserDAO();

    public BannedService(int communityId) {
        this.communityId = communityId;
    }

    public Map<String,String> getBannedInfo(){
        User user = (User) GuestContext.getCurrentGuest();
        return userDAO.getBannedInfo(user.getId(), communityId);
    }
}
