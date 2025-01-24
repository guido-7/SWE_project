package src.domainmodel;

import java.time.LocalDateTime;

public class Moderator extends User{
    private int userId;
    private int communityId;
    private LocalDateTime assignedDate;
    private String permissions;

    public Moderator(User user) {
        super(user.getId(), user.getNickname(), user.getName(), user.getSurname());
    }
}
