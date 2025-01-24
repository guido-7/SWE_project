package src.domainmodel;

import java.time.LocalDateTime;
import java.util.UUID;

public class Comment {
    private int id;
    private UUID post_id;
    private int user_id;
    private LocalDateTime time;
    private String content;
    private int upvotes;
    private int downvotes;

}