package src.domainmodel;

import java.sql.Timestamp;
import java.util.UUID;

public class Post {
    private UUID id;
    private Timestamp time;
    private int likes;
    private int dislikes;
    private String content;
    private int userId;
    private int communityId;

    public Post(UUID id, Timestamp time, String content, int userId, int communityId) {
        this.id = id;
        this.time = time;
        this.likes = 0;
        this.dislikes = 0;
        this.content = content;
        this.userId = userId;
        this.communityId = communityId;
    }


}
