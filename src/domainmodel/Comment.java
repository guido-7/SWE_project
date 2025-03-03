package src.domainmodel;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private final int post_id;
    private final int level;
    private final int user_id;
    private String content;
    private final int community_id;
    private int likes = 0;
    private int dislikes = 0;
    private final LocalDateTime time;
    private boolean is_modified = false;

    public Comment(int post_id, int level, int user_id, String content, int community_id) {
        this.post_id = post_id;
        this.level = level;
        this.user_id = user_id;
        this.content = content;
        this.time = LocalDateTime.now();
        this.community_id = community_id;
    }

    public Comment(int id, int post_id, int level, int user_id, String content, int community_id, int likes, int dislikes, LocalDateTime time, boolean is_modified) {
        this.id = id;
        this.post_id = post_id;
        this.level = level;
        this.user_id = user_id;
        this.time = time;
        this.content = content;
        this.community_id = community_id;
        this.likes = likes;
        this.dislikes = dislikes;
        this.is_modified = is_modified;
    }

    public int getPost_id() {
        return post_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public int getLevel() {
        return level;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCommunity_id() {
        return community_id;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setModifyFlag() {
        if (!is_modified)
            is_modified = true;
    }
}
