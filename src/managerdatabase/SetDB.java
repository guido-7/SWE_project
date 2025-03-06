package src.managerdatabase;

import src.domainmodel.Comment;
import src.domainmodel.Post;
import src.domainmodel.User;
import src.orm.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetDB {

    public static void main(String[] args) throws SQLException {
        DBConnection.connect();
        createDB();
        DBConnection.disconnect();
        final int numberofPosts = 40;
        final int numberofCommunities = 10;
        final int numberofUser = 100;
        final int numberofComments = 40;

        generatefakedata(numberofPosts, numberofCommunities, numberofUser, numberofComments);
    }

    public static void createDB() {
        createUserTable();
        createCommunityTable();
        createCommentTable();
        createPostTable();
        createBannedUsersTable();
        createCommentHierarchyTable();
        createModeratorTable();
        createPostVotesTable();
        createRulesTable();
        createSubscriptionTable();
        createUserAccessTable();
        createCommentWarningsTable();
        createPostwarningsTable();
        createUserDescription();
        createSavedPost();
        createCommentVotesTable();
        createTimeOutTable();
        createAdminTable();
    }

    public static void createCommunityTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Community ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " title TEXT NOT NULL,"
                + " description TEXT,"
                + " visits INTEGER DEFAULT 0,"
                + " scores REAL DEFAULT 1,"
                + " subs INTEGER DEFAULT 0"
                + ");";

        DBConnection.query(sql);
    }

    public static void createUserTable() {
        String sql = "CREATE TABLE IF NOT EXISTS User ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nickname TEXT NOT NULL,"
                + " name TEXT NOT NULL,"
                + " surname TEXT NOT NULL"
                + ");";

        DBConnection.query(sql);
    }

    public static void createUserAccessTable() {
        String sql = "CREATE TABLE IF NOT EXISTS UserAccess ("
                + " email TEXT PRIMARY KEY NOT NULL,"
                + " user_id INTEGER NOT NULL,"
                + " password TEXT NOT NULL,"
                + " authen INTEGER DEFAULT 0 CHECK (authen IN (0, 1)),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " UNIQUE(user_id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void createAdminTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Admin ("
                + " user_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " PRIMARY KEY (user_id, community_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void createRulesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Rules ("
                + " id INTEGER,"
                + " community_id INTEGER NOT NULL,"
                + " title TEXT NOT NULL,"
                + " content TEXT,"
                + " priority INTEGER DEFAULT 0,"
                + " PRIMARY KEY (id, community_id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        String sql2 = "CREATE TRIGGER IF NOT EXISTS AutoIncrementRulesId "
                + "AFTER INSERT ON Rules "
                + "WHEN NEW.id IS NULL "
                + "BEGIN "
                + "UPDATE Rules "
                + "SET id = COALESCE((SELECT MAX(id) FROM Rules WHERE community_id = NEW.community_id), 0) + 1 "
                + "WHERE rowid = NEW.rowid; "
                + "END;";

        // case scenario I insert a new rule of priority 8 with 10 at the moment
        // if I delete rule 2 every rule will be shifted by 1 above (-1)
        // I have  1 2    3 4 5  and I add 3
        String InsertTrigger = "CREATE TRIGGER IF NOT EXISTS InsertAdjustPriority "
                + "BEFORE INSERT ON Rules "
                + "FOR EACH ROW "
                + "WHEN NEW.priority IS NOT NULL "
                + "BEGIN "
                + "UPDATE Rules "
                + "SET priority = priority + 1 "
                + "WHERE community_id = NEW.community_id "
                + "AND priority >= NEW.priority;"
                + "END;";

        // if I delete rule 2 every rule will be shifted by 1 above (-1)
        // I have  1 2 3 4 5  and I delete 3
        String DeleteTrigger = "CREATE TRIGGER IF NOT EXISTS DeleteAdjustPriority "
                + "AFTER DELETE ON Rules "
                + "FOR EACH ROW "
                + "WHEN OLD.priority IS NOT NULL "
                + "BEGIN "
                + "UPDATE Rules "
                + "SET priority = priority - 1 "
                + "WHERE community_id = OLD.community_id "
                + "AND priority >= OLD.priority "
                + "AND id != OLD.id; "
                + "END;";

        DBConnection.query(sql);
        DBConnection.query(sql2);
        DBConnection.query(InsertTrigger);
        DBConnection.query(DeleteTrigger);
    }

    public static void createPostTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Post ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " time TEXT,"
                + " title TEXT NOT NULL,"
                + " content TEXT NOT NULL,"
                + " user_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " likes INTEGER DEFAULT 0,"
                + " dislikes INTEGER DEFAULT 0,"
                + " is_modified INTEGER DEFAULT 0 CHECK (is_modified IN (0, 1)),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void createCommentTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Comment ("
                + " id INTEGER,"
                + " post_id INTEGER NOT NULL,"
                + " level INTEGER DEFAULT 0,"
                + " user_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " content TEXT NOT NULL,"
                + " likes INTEGER DEFAULT 0,"
                + " dislikes INTEGER DEFAULT 0,"
                + " time TEXT,"
                + " is_modified INTEGER DEFAULT 0 CHECK (is_modified IN (0, 1)),"
                + " PRIMARY KEY (id, post_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (post_id) REFERENCES Post(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        String sql2 = "CREATE TRIGGER IF NOT EXISTS AutoIncrementCommentId "
                + "AFTER INSERT ON Comment "
                + "WHEN NEW.id IS NULL "
                + "BEGIN "
                + "UPDATE Comment "
                + "SET id = COALESCE((SELECT MAX(id) FROM Comment WHERE post_id = NEW.post_id), 0) + 1 "
                + "WHERE rowid = NEW.rowid; "
                + "END;";

        DBConnection.query(sql);
        DBConnection.query(sql2);
    }

    public static void createUserDescription() {
        String sql = "CREATE TABLE IF NOT EXISTS UserDescription ("
                + " user_id INTEGER PRIMARY KEY NOT NULL,"
                + " description TEXT,"
                + " FOREIGN KEY (user_id) REFERENCES User(id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void createCommentHierarchyTable() {
        String sql = "CREATE TABLE IF NOT EXISTS CommentHierarchy ("
                + " post_id INTEGER NOT NULL, "
                + " parent_id INTEGER NOT NULL, "
                + " child_id INTEGER NOT NULL, "
                + " PRIMARY KEY (child_id, parent_id, post_id), "
                + " FOREIGN KEY (child_id, post_id) REFERENCES Comment(id, post_id), "
                + " FOREIGN KEY (parent_id, post_id) REFERENCES Comment(id, post_id), "
                + " FOREIGN KEY (post_id) REFERENCES Post(id) "
                + ");";

        DBConnection.query(sql);
    }

    public static void createBannedUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS BannedUsers ("
                + " user_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " ban_date TEXT,"
                + " reason TEXT,"
                + " PRIMARY KEY (user_id, community_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void createModeratorTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Moderator ("
                + " user_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " assigned_date TEXT,"
                + " PRIMARY KEY (user_id, community_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void createSubscriptionTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Subscription ("
                + " user_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " subscription_date TEXT,"
                + " PRIMARY KEY (user_id, community_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        String sql2 = "CREATE TRIGGER IF NOT EXISTS update_subs"
                + " AFTER INSERT ON Subscription"
                + " FOR EACH ROW"
                + " BEGIN"
                + " UPDATE Community"
                + " SET subs = (SELECT COUNT(*) FROM Subscription WHERE community_id = NEW.community_id)"
                + " WHERE id = NEW.community_id;"
                + " END;";

        String sql3 = "CREATE TRIGGER IF NOT EXISTS delete_subs"
                + " AFTER DELETE ON Subscription"
                + " FOR EACH ROW"
                + " BEGIN"
                + " UPDATE Community"
                + " SET subs = (SELECT COUNT(*) FROM Subscription WHERE community_id = OLD.community_id)"
                + " WHERE id = OLD.community_id;"
                + " END;";

        DBConnection.query(sql);
        DBConnection.query(sql2);
        DBConnection.query(sql3);
    }

    public static void createPostVotesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS PostVotes ("
                + " user_id INTEGER NOT NULL,"
                + " post_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " vote_type INTEGER DEFAULT 0 CHECK (vote_type IN (0,1)),"// 0-->dislike,1-->like
                + " PRIMARY KEY (user_id, post_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,"
                + " FOREIGN KEY (post_id) REFERENCES Post(id) ON DELETE CASCADE,"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        String sql2 = "CREATE TRIGGER IF NOT EXISTS update_post_likes_dislikes"
                + " AFTER INSERT ON PostVotes"
                + " FOR EACH ROW"
                + " BEGIN"
                + " UPDATE Post"
                + " SET likes = (SELECT COUNT(*) FROM PostVotes WHERE post_id = NEW.post_id AND vote_type = 1),"
                + " dislikes = (SELECT COUNT(*) FROM PostVotes WHERE post_id = NEW.post_id AND vote_type = 0)"
                + " WHERE id = NEW.post_id;"
                + " END;";

        String sql3 = "CREATE TRIGGER IF NOT EXISTS delete_post_likes_dislikes"
                + " AFTER DELETE ON PostVotes"
                + " FOR EACH ROW"
                + " BEGIN"
                + " UPDATE Post"
                + " SET likes = (SELECT COUNT(*) FROM PostVotes WHERE post_id = OLD.post_id AND vote_type = 1),"
                + " dislikes = (SELECT COUNT(*) FROM PostVotes WHERE post_id = OLD.post_id AND vote_type = 0)"
                + " WHERE id = OLD.post_id;"
                + " END;";

        DBConnection.query(sql);
        DBConnection.query(sql2);
        DBConnection.query(sql3);
    }

    public static void createCommentVotesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS CommentVotes ("
                + " user_id INTEGER NOT NULL,"
                + " comment_id INTEGER NOT NULL,"
                + " post_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " vote_type INTEGER DEFAULT 0 CHECK (vote_type IN (0, 1)),"
                + " PRIMARY KEY (user_id, comment_id, post_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,"
                + " FOREIGN KEY (comment_id, post_id) REFERENCES Comment(id, post_id) ON DELETE CASCADE,"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        String sql2 = "CREATE TRIGGER IF NOT EXISTS update_comment_likes_dislikes"
                + " AFTER INSERT ON CommentVotes"
                + " FOR EACH ROW"
                + " BEGIN"
                + " UPDATE Comment"
                + " SET likes = (SELECT COUNT(*) FROM CommentVotes WHERE comment_id = NEW.comment_id AND post_id = NEW.post_id AND vote_type = 1),"
                + " dislikes = (SELECT COUNT(*) FROM CommentVotes WHERE comment_id = NEW.comment_id AND post_id = NEW.post_id AND vote_type = 0)"
                + " WHERE id = NEW.comment_id AND post_id = NEW.post_id;"
                + " END;";

        String sql3 = "CREATE TRIGGER IF NOT EXISTS delete_comment_likes_dislikes"
                + " AFTER DELETE ON CommentVotes"
                + " FOR EACH ROW"
                + " BEGIN"
                + " UPDATE Comment"
                + " SET likes = (SELECT COUNT(*) FROM CommentVotes WHERE comment_id = OLD.comment_id AND post_id = OLD.post_id AND vote_type = 1),"
                + " dislikes = (SELECT COUNT(*) FROM CommentVotes WHERE comment_id = OLD.comment_id AND post_id = OLD.post_id AND vote_type = 0)"
                + " WHERE id = OLD.comment_id AND post_id = OLD.post_id;"
                + " END;";

        DBConnection.query(sql);
        DBConnection.query(sql2);
        DBConnection.query(sql3);
    }

    public static void createPostwarningsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS PostWarnings ("
                + " sender_id INTEGER NOT NULL,"
                + " post_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " PRIMARY KEY (sender_id, post_id),"
                + " FOREIGN KEY (sender_id) REFERENCES User(id),"
                + " FOREIGN KEY (post_id) REFERENCES Post(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void createCommentWarningsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS CommentWarnings ("
                + " sender_id INTEGER NOT NULL,"
                + " comment_id INTEGER NOT NULL,"
                + " post_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " PRIMARY KEY (sender_id, comment_id,post_id),"
                + " FOREIGN KEY (sender_id) REFERENCES User(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id),"
                + " FOREIGN KEY (comment_id,post_id) REFERENCES Comment(id,post_id)"
                + ");";

        DBConnection.query(sql);
    }

    private static void createSavedPost() {
        String sql = "CREATE TABLE IF NOT EXISTS SavedPost ("
                + " user_id INTEGER NOT NULL,"
                + " post_id INTEGER NOT NULL,"
                + " PRIMARY KEY (user_id, post_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (post_id) REFERENCES Post(id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void createTimeOutTable() {
        String sql = "CREATE TABLE IF NOT EXISTS TimeOut ("
                + " user_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " end_time_out_date TEXT,"
                + " PRIMARY KEY (user_id, community_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void generatefakedata(final int numberofPosts, final int numberofCommunity, final int numberofUsers, final int numberofComments) throws SQLException {
        UserDAO userDAO = new UserDAO();
        CommunityDAO communityDAO = new CommunityDAO();
        SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
        PostDAO postDAO = new PostDAO();
        CommentDAO commentDAO = new CommentDAO();

        // create fake users
        for (int i = 1; i <= numberofUsers; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("nickname", "User " + i);
            params.put("name", "Name " + i);
            params.put("surname", "Surname " + i);
            userDAO.save(params);
        }
        System.out.println("Users created");

        // create fake communities
        for (int i = 1; i <= numberofCommunity; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("title", "Community Title " + i);
            params.put("description", "Description " + i);
            communityDAO.save(params);
        }
        System.out.println("Communities created");

        // create fake rules
        for (int communityId = 1; communityId <= numberofCommunity; communityId++) {
            Map<Integer, ArrayList<String>> ruleParams = new HashMap<>();
            for (int j = 1; j <= 3; j++) {
                ArrayList<String> rule = new ArrayList<>();
                rule.add("Rule Title " + j);
                rule.add("Rule Content " + j);
                ruleParams.put(j, rule);
            }
            communityDAO.saveRules(communityId, ruleParams);
        }
        System.out.println("Rules created");

        // create fake subscriptions
        for (int i = 1; i <= numberofUsers; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("user_id", i);
            params.put("community_id", (int) (Math.random() * numberofCommunity) + 1);
            subscriptionDAO.save(params);
        }
        System.out.println("Subscriptions created");

        // create fake Posts
        for (int i = 1; i <= numberofPosts; i++) {
            Map<String, Object> params = new HashMap<>();
            int user_id = (int) ((Math.random() * numberofUsers) + 1);
            int community_id = (int) ((Math.random() * numberofCommunity) + 1);
            params.put("title", "Title Post " + i);
            params.put("content", "Content " + i);
            params.put("user_id", user_id);
            params.put("community_id", community_id);
            postDAO.save(params);
        }
        System.out.println("Posts created");

        // create fake PostVotes
        for (int i = 1; i <= (numberofPosts * 2); i++) {
            Post p1 = postDAO.findById((int) ((Math.random() * numberofPosts) + 1)).orElse(null);
            Map<String, Object> params1 = new HashMap<>();
            params1.put("user_id", (i % numberofUsers) + 1);
            params1.put("post_id", p1.getId());
            params1.put("vote_type", (int) (Math.random() * 2));
            params1.put("community_id", p1.getCommunityId());
            userDAO.insertPostVotes(params1);
        }
        System.out.println("Posts created\n");

        // create fake comments
        for (int i = 1; i <= (numberofPosts * 2); i++) {
            Map<String, Object> params = new HashMap<>();
            int user_id = (int) ((Math.random() * numberofUsers) + 1);
            Post post = postDAO.findById((i % numberofPosts) + 1).orElse(null);
            params.put("post_id", (i % numberofPosts) + 1);
            params.put("level", 0);
            params.put("user_id", user_id);
            params.put("content", "Content " + i + 1);
            params.put("community_id", post.getCommunityId());
            commentDAO.save(params);
        }
        System.out.println("Comments created\n");

        // create fake CommentVotes
        for (int i = 1; i <= (numberofComments)-1; i++) {
            Comment c1 = commentDAO.findById( List.of(i%numberofComments, 1)).orElse(null);
            Map<String, Object> params1 = new HashMap<>();
            params1.put("user_id", (i % numberofUsers) + 1);
            params1.put("comment_id", c1.getId());
            params1.put("post_id", c1.getPost_id());
            params1.put("vote_type", (int) (Math.random() * 2));
            Post post = postDAO.findById(c1.getPost_id()).orElse(null);
            params1.put("community_id", post.getCommunityId());
            userDAO.insertCommentVotes(params1);
        }

        // create fake Post Warnings
        for (int i = 1; i <= (numberofPosts / 2); i++) {
            User user = userDAO.findById((int) ((Math.random() * numberofUsers) + 1)).orElse(null);
            Post post = postDAO.findById((int) ((Math.random() * numberofPosts) + 1)).orElse(null);
            postDAO.addPostWarning(post, user.getId());
        }
        System.out.println("Post Warnings created\n");

    }
}