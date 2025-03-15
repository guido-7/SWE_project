package src.controllers.factory;

import src.businesslogic.CommentService;
import src.businesslogic.CommunityService;
import src.businesslogic.PostService;
import src.controllers.*;
import src.domainmodel.Comment;
import src.domainmodel.Post;
import src.domainmodel.PostWarnings;

import java.sql.SQLException;
import java.util.ArrayList;

public class  ComponentFactory {

    public static CommentController createCommentController(Comment comment) {
        return new CommentController(new CommentService(comment));
    }

    public static ModeratorDecisionController createModeratorDecisionController(ArrayList<PostWarnings> postWarnings, int communityId) {
        return new ModeratorDecisionController(postWarnings, new CommunityService(communityId));
    }

    public static PostController createPostController(Post post) throws SQLException {
        return new PostController(new PostService(post));
    }

    public static PinnedPostController createPinnedPostController(int communityId) {
        return new PinnedPostController(new CommunityService(communityId));
    }

    public static SubInfoComponentController createSubInfoComponentController(CommunityService communityService) {
        return new SubInfoComponentController(communityService);
    }

    public static PostReplyController createPostReplyController(Post post) throws SQLException {
        return new PostReplyController(new PostController(new PostService(post)));
    }

    public static RulesController createRulesController(int communityId, int rulesId) {
        return new RulesController( new CommunityService(communityId), rulesId);
    }
}
