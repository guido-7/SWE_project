package src.domainmodel;

public class CommentWarnings extends PostWarnings {
    private final int commentId;
    private final int level;

    public CommentWarnings(int senderId, String sender_nickname, String content, int postId,int reportedId,String reported_nickname,String title,  int commentId, int level) {
        super(senderId, sender_nickname, content, postId,reportedId, reported_nickname,title);
        this.commentId = commentId;
        this.level = level;
    }

    public int getCommentId() {
        return commentId;
    }
    public int getLevel() {
        return level;
    }

}
