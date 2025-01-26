package src.domainmodel;

import java.time.LocalDateTime;

public class Subscription {
    private final int user_id;
    private final int community_id;
    private User user;
    private Community community;
    private final LocalDateTime subscription_date;

    public Subscription(int user_id, int community_id, LocalDateTime subscription_date) {
        this.user_id = user_id;
        this.community_id = community_id;
        this.subscription_date = subscription_date;
    }

    public int getUser_id() {
        return user_id;
    }

    public int getCommunity_id() {
        return community_id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    public LocalDateTime getSubscription_date() {
        return subscription_date;
    }
}
