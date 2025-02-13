package src.managerdatabase;
import src.domainmodel.User;
import src.orm.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class  SetDB {

    public static void main(String[] args) throws SQLException {
//        DBConnection.connect();
//        createDB();
//        DBConnection.disconnect();
          int numberofPosts = 40;
          int numberofCommunities = 10;
          int numberofUser = 100;

          generatefakedata(numberofPosts,numberofCommunities,numberofUser);
    }

    public static void generatePosts() throws SQLException {
        PostDao postDao = new PostDao();
        for (int i = 1; i <= 8; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("title", "Post Title " + i);
            params.put("content", "This is the content of post number " + i);
            params.put("user_id", 1); // Assuming user_id 1 exists
            params.put("community_id", 1); // Assuming community_id 1 exists
            postDao.save(params);
        }
    }

    public static void createDB() {

        createUserTable();
        createCommunityTable();
        createAdminTable();
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
                + " id INTEGER PRIMARY KEY NOT NULL,"
                + " FOREIGN KEY (id) REFERENCES User(id)"
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

        DBConnection.query(sql);
        DBConnection.query(sql2);
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

        DBConnection.query(sql);
    }

    public static void createPostVotesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS PostVotes ("
                + " user_id INTEGER NOT NULL,"
                + " post_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " vote_type INTEGER DEFAULT 0 CHECK (vote_type IN (0,1)),"// 0-->dislike,1-->like
                + " PRIMARY KEY (user_id, post_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (post_id) REFERENCES Post(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void createPostwarningsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS PostWarnings ("
                + " sender_id INTEGER NOT NULL,"
                + " post_id INTEGER NOT NULL,"
                + " PRIMARY KEY (sender_id, post_id),"
                + " FOREIGN KEY (sender_id) REFERENCES User(id),"
                + " FOREIGN KEY (post_id) REFERENCES Post(id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void createCommentWarningsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS CommentWarnings ("
                + " sender_id INTEGER NOT NULL,"
                + " comment_id INTEGER NOT NULL,"
                + " post_id INTEGER NOT NULL,"
                + " PRIMARY KEY (sender_id, comment_id,post_id),"
                + " FOREIGN KEY (sender_id) REFERENCES User(id),"
                + " FOREIGN KEY (comment_id,post_id) REFERENCES Comment(id,post_id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void generatefakedata(int numberofPosts, int numberofCommunity, int numberofUsers) throws SQLException {

        // create fake communities
        for (int i = 0; i <= numberofCommunity; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("title", "Title" + i + 1);
            params.put("description", "description" + i + 1);
            CommunityDAO communityDAO = new CommunityDAO();
            communityDAO.save(params);
        }
        // create fake subscriptions
        for (int i = 0; i <= numberofUsers; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("user_id",i);
            params.put("community_id", (int) (Math.random() * numberofCommunity) + 1);
            SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
            subscriptionDAO.save(params);
        }

        // create fake Posts
        for (int i = 0; i <= numberofPosts; i++) {
            Map<String, Object> params = new HashMap<>();
            int user_id = (((int) (Math.random() * numberofUsers) + 1));
            int community_id = (int) (Math.random() * numberofCommunity) + 1;
            params.put("title", "Title" + i + 1);
            params.put("content", "Content " + i + 1);
            params.put("user_id", +user_id);
            params.put("community_id", community_id);
            PostDao postDao = new PostDao();
            postDao.save(params);

            // create fake PostVotes
            Map<String, Object> params1 = new HashMap<>();
            params1.put("user_id", user_id);
            params1.put("post_id", i + 1);
            params1.put("vote_type", (int )(Math.random() * 2));
            params1.put("community_id", community_id);
            UserDAO userDAO = new UserDAO();
            userDAO.insertPostVotes(params1);
        }

        // create fake comments
        for (int i = 0; i <= numberofPosts; i++) {
            Map<String, Object> params = new HashMap<>();
            int user_id = (((int) (Math.random() * numberofUsers) + 1));
            int community_id = (int) (Math.random() * numberofCommunity) + 1;
            params.put("post_id", i + 1);
            params.put("level", 0);
            params.put("user_id", user_id);
            params.put("content", "Content " + i+1);
            params.put("community_id", community_id);
            CommentDAO commentDAO = new CommentDAO();
            commentDAO.save(params);
        }

    }
}