package src.domainmodel;

import java.util.ArrayList;

public class Community {
    private final int id;
    private String title;
    private String description;
    private int subscribers;
    private int monthlyVisits;
    private int score;
    private ArrayList<Post> posts = new ArrayList<>();

    public Community(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void addPostToCommunity(Post post) {
        posts.add(post);
    }

    public void removePostFromCommunity(Post post) {
        posts.remove(post);
    }

    public ArrayList<Post> getPosts() {
        return posts;
    }

    public void setPosts(ArrayList<Post> posts) {
        this.posts = posts;
    }

    public int getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(int subscribers) {
        this.subscribers = subscribers;
    }

    public int getMonthlyVisits() {
        return monthlyVisits;
    }

    public void setMonthlyVisits(int monthlyVisits) {
        this.monthlyVisits = monthlyVisits;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
