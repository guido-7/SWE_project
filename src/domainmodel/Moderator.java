package src.domainmodel;

import java.time.LocalDateTime;

public class Moderator extends User{
    private final Community community;
    private final LocalDateTime assignedDate;

    public Moderator(User user, Community community, LocalDateTime assignedDate) {

        super(user.getId(), user.getNickname(), user.getName(), user.getSurname());
        this.community = community;
        this.assignedDate = assignedDate;

    }

    public Community getCommunityId() {
        return community;
    }

    public LocalDateTime getAssignedDate() {
        return assignedDate;
    }
}
