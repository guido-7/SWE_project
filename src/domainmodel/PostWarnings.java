package src.domainmodel;

public class PostWarnings {
    private final int senderId;
    private final String sender_nickname;
    private final String content;
    private final int postId;
    private final String reported_nickname;

    public PostWarnings(int senderId, String sender_nickname, String content, int postId, String reported_nickname) {
        this.senderId = senderId;
        this.sender_nickname= sender_nickname;
        this.content = content;
        this.postId = postId;
        this.reported_nickname = reported_nickname;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getPostId() {
        return postId;
    }
    public String getContent(){
        return content;
    }
    public String getSender_nickname(){
        return sender_nickname;
    }
    public String getReported_nickname(){
        return reported_nickname;
    }

}
