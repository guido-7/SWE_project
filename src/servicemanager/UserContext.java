package src.servicemanager;

import src.domainmodel.User;

public class UserContext {
    private static   User previousContextUser;
    private static  User currentUser;


    public static User getPreviousContextUser() {
        return previousContextUser;
    }

    public static void setPreviousContextUser(User previousContextUser) {
       UserContext.previousContextUser = previousContextUser;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        UserContext.currentUser = currentUser;
    }

    public static void backToPreviousContext(){
        currentUser = previousContextUser;
        previousContextUser = null;
    }
}
