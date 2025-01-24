package src.domainmodel;

import java.time.LocalDateTime;

public class Post {
    private final int id;
    private final LocalDateTime time;
    private int likes;
    private int dislikes;
    private String content;
    private final int userId;
    private final int communityId;
//    private final User user;
//    private final Community community;
    private boolean is_modified = false;

    // Constructor for creation of object from database
    public Post(int id, LocalDateTime time, String content, int userId, int communityId, int likes, int dislikes) {
        this.id = id;
        this.time = time;
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
}
