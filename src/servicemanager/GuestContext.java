package src.servicemanager;

import src.domainmodel.Guest;
import src.domainmodel.User;

public class GuestContext {
    private static Guest previousContextGuest;
    private static  Guest currentGuest;



    public static Guest getPreviousContextGuest() {
        return previousContextGuest;
    }

    public static void setPreviousContextGuest(Guest previousContextUser) {
       GuestContext.previousContextGuest = previousContextUser;
    }

    public static Guest getCurrentGuest() {
        return currentGuest;
    }

    public static void setCurrentGuest(Guest currentUser) {
        GuestContext.currentGuest = currentUser;
    }

    public static void backToPreviousContext(){
        currentGuest = previousContextGuest;
        previousContextGuest = null;
    }
}
