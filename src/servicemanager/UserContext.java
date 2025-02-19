package src.servicemanager;

import src.domainmodel.User;

public class UserContext {
    private  User previousContextUser;
    private  User currentUser;

    public UserContext(User currentUser) {
        this.currentUser = currentUser;
    }
    public UserContext() {}

    public User getPreviousContextUser() {
        return previousContextUser;
    }

    public void setPreviousContextUser(User previousContextUser) {
        this.previousContextUser = previousContextUser;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void backToPreviousContext(){
        currentUser = previousContextUser;
        previousContextUser = null;
    }
}
