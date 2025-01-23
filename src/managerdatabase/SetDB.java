package src.managerdatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SetDB {
    public DBConnection DBconn;
    //public static Connection conn;

    public static void main(String[] args) {
        SetDB setDb = new SetDB();
        Connection conn = DBConnection.connect();
        setDb.createCommunityTable();

        DBConnection.disconnect();
    }


    public void createCommunityTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Community ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " title TEXT NOT NULL,"
                + " description TEXT"
                + ");";

        DBConnection.query(sql);
    }

    public void createUserTable() {
        String sql = "CREATE TABLE IF NOT EXISTS User ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " name TEXT,"
                + " surname TEXT,"
                + " password TEXT NOT NULL,"
                + " authen BOOLEAN"
                + ");";

        DBConnection.query(sql);
    }

    public void createUserAccessTable() {
        String sql = "CREATE TABLE IF NOT EXISTS UserAccess ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " user_id INTEGER,"
                + " last_login TIMESTAMP,"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " UNIQUE(user_id)"
                + ");";

        DBConnection.query(sql);
    }

    public void createAdminTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Admin ("
                + " id INTEGER PRIMARY KEY,"
                + " FOREIGN KEY (id) REFERENCES User(id)"
                + ");";

        DBConnection.query(sql);
    }

    public void createRulesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Rules ("
                + " id INTEGER,"
                + " community_id INTEGER,"
                + " content TEXT,"
                + " PRIMARY KEY (id, community_id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public void createPostTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Post ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " orario TIMESTAMP,"
                + " likes INTEGER DEFAULT 0,"
                + " dislikes INTEGER DEFAULT 0,"
                + " content TEXT NOT NULL,"
                + " user_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public void createCommentTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Comment ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " orario TIMESTAMP,"
                + " likes INTEGER DEFAULT 0,"
                + " dislikes INTEGER DEFAULT 0,"
                + " content TEXT NOT NULL,"
                + " user_id INTEGER NOT NULL,"
                + " post_id INTEGER NOT NULL,"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (post_id) REFERENCES Post(id)"
                + ");";

        DBConnection.query(sql);
    }

    public void createCommentHierarchyTable() {
        String sql = "CREATE TABLE IF NOT EXISTS CommentHierarchy ("
                + " parent_id INTEGER,"
                + " child_id INTEGER,"
                + " PRIMARY KEY (parent_id, child_id),"
                + " FOREIGN KEY (parent_id) REFERENCES Comment(id),"
                + " FOREIGN KEY (child_id) REFERENCES Comment(id)"
                + ");";

        DBConnection.query(sql);
    }

    public void createBannedUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS BannedUsers ("
                + " user_id INTEGER,"
                + " community_id INTEGER,"
                + " ban_date TIMESTAMP,"
                + " reason TEXT,"
                + " PRIMARY KEY (user_id, community_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public void createModeratorTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Moderator ("
                + " user_id INTEGER,"
                + " community_id INTEGER,"
                + " assigned_date TIMESTAMP,"
                + " permissions TEXT,"
                + " PRIMARY KEY (user_id, community_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public void createSubscriptionTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Subscription ("
                + " user_id INTEGER,"
                + " community_id INTEGER,"
                + " subscription_date TIMESTAMP,"
                + " PRIMARY KEY (user_id, community_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public void createPostVotesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS PostVotes ("
                + " user_id INTEGER,"
                + " post_id INTEGER,"
                + " vote_type TEXT CHECK(vote_type IN ('like', 'dislike')),"
                + " PRIMARY KEY (user_id, post_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (post_id) REFERENCES Post(id)"
                + ");";

        DBConnection.query(sql);
    }
}

