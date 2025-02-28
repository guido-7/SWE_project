package src.servicemanager;

import src.controllers.Controller;
import src.domainmodel.Guest;
import src.domainmodel.User;

public class GuestContext {
    private static Guest previousContextGuest;
    private static  Guest currentGuest;

    public static Controller getCurrentController() {
        return currentController;
    }

    public static void setCurrentController(Controller currentController) {
        GuestContext.currentController = currentController;
    }

    private static Controller currentController;



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
