package src.domainmodel;
import src.utils.StringManager;

public class PostWarnings {
    private final int senderId;
    private final String sender_nickname;
    private final String content;
    private final int postId;
    private final String reported_nickname;
    private final String title;

    public PostWarnings(int senderId, String sender_nickname, String content, int postId, String reported_nickname,String title) {
        this.senderId = senderId;
        this.sender_nickname= sender_nickname;
        this.content = content;
        this.postId = postId;
        this.reported_nickname = reported_nickname;
        this.title = title;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getPostId() {
        return postId;
    }
    public String getContent(){
        return StringManager.reduceStringToFiveWords(content);
    }
    public String getSender_nickname(){
        return sender_nickname;
    }
    public String getReported_nickname(){
        return reported_nickname;
    }
    public String getTitle() {
        return title;
    }

}
