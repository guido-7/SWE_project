package src.domainmodel;

import java.time.LocalDateTime;

public class Post {
    private int id;
    private String title;
    private final LocalDateTime time;
    private int likes;
    private int dislikes;
    private String content;
    private final int userId;
    private final int communityId;
    private User user;
    private Community community;
    private boolean is_modified = false;

    // Constructor for creation of object from database

    public Post(String title, String content, int userId) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.communityId = 0;
        this.time = LocalDateTime.now();
    }

    public Post(int id, LocalDateTime time, String title, String content, int userId, int communityId, int likes, int dislikes) {
        this.id = id;
        this.time = time;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.communityId = communityId;
        this.likes = likes;
        this.dislikes = dislikes;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUserId() {
        return userId;
    }

    public int getCommunityId() {
        return communityId;
    }

    // Method to call only when modifying the post
    public void setModifyFlag(){
        if (!is_modified)
            is_modified = true;
    }

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public void setLikes(int i) {
        this.likes = i;
    }

    public void setDislikes(int i) {
        this.dislikes = i;
    }

}
