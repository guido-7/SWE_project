package src.domainmodel;

import java.util.ArrayList;

public class Feed {

    private ArrayList<Post> posts = new ArrayList<>();

    public Feed(ArrayList<Post> posts) {
        this.posts = posts;
    }

    public ArrayList<Post> getPosts() {
        return posts;
    }

    public void setPosts(ArrayList<Post> posts) {
        this.posts = posts;
    }
}
