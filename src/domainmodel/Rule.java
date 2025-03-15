package src.domainmodel;

public class Rule {
    private final int id;
    private final int community_id;
    private final String Title;
    private String content;
    private int priority;

    public Rule(int id, int community_id, String title, String content, int priority) {
        this.id = id;
        this.community_id = community_id;
        this.Title = title;
        this.content = content;
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public int getCommunity_id() {
        return community_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTitle() {
        return Title;
    }
}
