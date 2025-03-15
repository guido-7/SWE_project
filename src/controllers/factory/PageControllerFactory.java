package src.controllers.factory;

import src.businesslogic.*;
import src.controllers.*;
import src.domainmodel.*;

import java.sql.SQLException;

public class PageControllerFactory {

    public static HomePageController createHomePageController(Guest guest) {
        return new HomePageController(new FeedService(guest));
    }

    public static PostCreationPageController createPostCreationPageController() {
        return new PostCreationPageController(new PostCreationService());
    }

    public static PostPageController createPostPageController(Post post) throws SQLException {
        return new PostPageController(new PostService(post));
    }

    public static UserProfilePageController createUserProfilePageController(User user) {
        return new UserProfilePageController(new UserProfileService(user));
    }

    public static CommunityPageController createCommunityPageController(int communityId) {
        return new CommunityPageController(new CommunityService(communityId));
    }

    public static CommunityCreationPageController createCommunityCreationPageController() {
        return new CommunityCreationPageController(new CommunityCreationService());
    }

    public static CommunitySettingsPageController createCommunitySettingsController(int communityId) {
        return new CommunitySettingsPageController(new CommunityService(communityId));
    }

    public static AdminPageController createAdminPageController(int communityId) {
        return new AdminPageController(new CommunityService(communityId));
    }

    public static AddRuleController createAddRuleController(int communityId) {
        return new AddRuleController(new CommunityService(communityId));
    }

}
