package src.domainmodel;

import java.time.LocalDateTime;

public class Moderator extends User{
    private final int community_id;
    private Community community;
    private final LocalDateTime assignedDate;

    public Moderator(User user, int community_id, LocalDateTime assignedDate) {
        super(user.getId(), user.getNickname(), user.getName(), user.getSurname(), user.getPermits());
        this.community_id = community_id;
        this.assignedDate = assignedDate;

    }

    public int getCommunity_id() {
        return community_id;
    }

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    public Community getCommunityId() {
        return community;
    }

    public LocalDateTime getAssignedDate() {
        return assignedDate;
    }
}
