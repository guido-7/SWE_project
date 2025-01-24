package src.domainmodel;

import java.time.LocalDateTime;

public class Post {
    private final int id;
    private final LocalDateTime time;
    private int likes = 0;
    private int dislikes =0;
    private String content;
    private final User user;
    private final Community community;
    private boolean is_modified = false;

    // Constructor for creation of object from database
    public Post(int id, LocalDateTime time, String content, User user, Community community, int likes, int dislikes) {
        this.id = id;
        this.time = time;
        this.content = content;
        this.user = user;
        this.community = community;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public Community getCommunity() {
        return community;
    }

    // Method to call only when modifying the post
    public void setModifyFlag(){
        if (!is_modified)
            is_modified = true;
    }
}
