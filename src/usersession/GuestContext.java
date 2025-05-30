package src.usersession;

import src.controllers.Controller;
import src.controllers.PageController;
import src.domainmodel.Guest;

import java.util.ArrayList;
import java.util.List;

public class GuestContext {
    private static Guest previousContextGuest;
    private static Guest currentGuest;
    private static Controller currentController;
    private static final List<Controller> previousContextController = new ArrayList<Controller>();

    public static Controller getPreviousContextController() {
        Controller ctrl = previousContextController.getLast();
        previousContextController.removeLast();
        return ctrl;
    }

    private static void setPreviousContextController(Controller previousContextController) {
        GuestContext.previousContextController.addLast(previousContextController);
    }

    public static Controller getCurrentController() {
        return currentController;
    }

    public static void setCurrentController(Controller currentController) {
        setPreviousContextController(GuestContext.currentController);
        GuestContext.currentController = currentController;
    }

    public static Guest getPreviousContextGuest() {
        return previousContextGuest;
    }

    private static void setPreviousContextGuest(Guest previousContextUser) {
       GuestContext.previousContextGuest = previousContextUser;
    }

    public static Guest getCurrentGuest() {
        return currentGuest;
    }

    public static void setCurrentGuest(Guest currentUser) {
        setPreviousContextGuest(GuestContext.getCurrentGuest());
        GuestContext.currentGuest = currentUser;
    }

    public static void backToPreviousContext(){
        currentGuest = previousContextGuest;
        previousContextGuest = null;
    }

    public static void clearController() {
        previousContextController.clear();
        currentController = null;
    }

}
