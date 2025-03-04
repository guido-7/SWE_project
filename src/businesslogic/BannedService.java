package src.businesslogic;

import javafx.scene.control.Label;
import src.domainmodel.User;
import src.orm.UserDAO;
import src.servicemanager.GuestContext;
import src.servicemanager.FormattedTime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class BannedService {
    private int communityId;

    public BannedService(int communityId) {
        this.communityId = communityId;
    }

    public void setLabels(Label banDurationLabel, Label banReasonLabel) {
        UserDAO userDAO = new UserDAO();
        User user = (User) GuestContext.getCurrentGuest();

        HashMap<String, String> bannedInfo = userDAO.getBannedInfo(user.getId(), communityId);
        if (bannedInfo != null) {
            LocalDateTime banDate = LocalDateTime.parse(bannedInfo.get("ban_date"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            FormattedTime formattedTime = new FormattedTime();
            String formattedBanDuration = formattedTime.getBanTime(banDate);

            banDurationLabel.setText(formattedBanDuration);
            banReasonLabel.setText(bannedInfo.get("reason"));
        } else {
            banDurationLabel.setText("N/A");
            banReasonLabel.setText("N/A");
        }
    }

}
