package src.servicemanager;

import src.businesslogic.FeedService;
import src.domainmodel.Guest;
import src.domainmodel.User;

public class Service {
    private static FeedService feedService;

    public static void initializeServices(Guest guest) {
        if (guest != null) {
            feedService = new FeedService(guest);
        }
    }

    public static FeedService getFeedService() {
        return feedService;
    }
}
