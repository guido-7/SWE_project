package src.factory;

import src.services.*;
import src.services.componentservices.PostService;
import src.services.pageservices.CommunityCreationPageService;
import src.services.pageservices.CommunityService;
import src.services.pageservices.PostCreationPageService;
import src.services.pageservices.UserProfilePageService;
import src.controllers.pagecontrollers.*;
import src.controllers.pagecontrollers.HomePageController;
import src.domainmodel.*;

public class PageControllerFactory {

    public static HomePageController createHomePageController(Guest guest) {
        return new HomePageController(new FeedService(guest));
    }

    public static PostCreationPageController createPostCreationPageController() {
        return new PostCreationPageController(new PostCreationPageService());
    }

    public static PostPageController createPostPageController(Post post) {
        return new PostPageController(new PostService(post));
    }

    public static UserProfilePageController createUserProfilePageController(User user) {
        return new UserProfilePageController(new UserProfilePageService(user));
    }

    public static CommunityPageController createCommunityPageController(int communityId) {
        return new CommunityPageController(new CommunityService(communityId));
    }

    public static CommunityCreationPageController createCommunityCreationPageController() {
        return new CommunityCreationPageController(new CommunityCreationPageService());
    }

    public static CommunitySettingsPageController createCommunitySettingsController(int communityId) {
        return new CommunitySettingsPageController(new CommunityService(communityId));
    }

    public static AdminPageController createAdminPageController(int communityId) {
        return new AdminPageController(new CommunityService(communityId));
    }

    public static AddRulePageController createAddRuleController(int communityId) {
        return new AddRulePageController(new CommunityService(communityId));
    }

}
