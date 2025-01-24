package src.domainmodel;

import java.time.LocalDateTime;

public class Comment {
    private final int id;
    private final Post post;
    private final User user;
    private final LocalDateTime time;
    private String content;
    private int likes = 0;
    private int dislikes = 0;
    private boolean is_modified = false;

    public Comment(int id, Post post, User user, LocalDateTime time, String content, int likes, int dislikes, boolean is_modified) {
        this.id = id;
        this.post = post;
        this.user = user;
        this.time = time;
        this.content = content;
        this.likes = likes;
        this.dislikes = dislikes;
        this.is_modified = is_modified;
    }

    public int getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public User getUser() {
        return user;
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

    public int getLikes() {
        return likes;
    }


    public int getDislikes() {
        return dislikes;
    }

    public void setModifyFlag(){
        if (!is_modified)
            is_modified = true;
    }
}