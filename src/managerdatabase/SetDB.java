package src.managerdatabase;

public class  SetDB {
    public static void createDB(){

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

    }

    public static  void createCommunityTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Community ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " title TEXT NOT NULL,"
                + " description TEXT"
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

    public static void  createUserAccessTable() {
        String sql = "CREATE TABLE IF NOT EXISTS UserAccess ("
                + " email TEXT PRIMARY KEY NOT NULL,"
                + " user_id INTEGER NOT NULL,"
                + " password TEXT NOT NULL,"
                + " authen TEXT,"
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
                + " id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " content TEXT,"
                + " PRIMARY KEY (id, community_id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void createPostTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Post ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " time TEXT,"
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

    public static void createCommentTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Comment ("
                + " id INTEGER NOT NULL,"  // Use SERIAL for auto-incremented integers
                + " post_id INTEGER NOT NULL,"  // UUID type for post_id
                + " time TEXT,"  // Timestamp for the comment
                + " likes INTEGER DEFAULT 0,"  // Default value for likes
                + " dislikes INTEGER DEFAULT 0,"  // Default value for dislikes
                + " content TEXT NOT NULL,"  // Content of the comment
                + " user_id INTEGER NOT NULL,"  // User id for the commenter
                + " PRIMARY KEY (id, post_id),"  // Composite primary key (id, post_id)
                + " FOREIGN KEY (user_id) REFERENCES User(id),"  // Foreign key for user reference
                + " FOREIGN KEY (post_id) REFERENCES Post(id)"  // Foreign key for post reference
                + ");";

        DBConnection.query(sql);
    }

    public static void createCommentHierarchyTable() {
        String sql = "CREATE TABLE IF NOT EXISTS CommentHierarchy ("
                + " parent_id INTEGER NOT NULL, "
                + " post_id INTEGER NOT NULL, "
                + " child_id INTEGER NOT NULL, "
                + " PRIMARY KEY (parent_id, child_id,post_id), "
                + " FOREIGN KEY (parent_id) REFERENCES Comment(id), "
                + " FOREIGN KEY (child_id) REFERENCES Comment(id) "
                + " FOREIGN KEY (post_id) REFERENCES Post(id) "
                + ");";

        DBConnection.query(sql);
    }


    public static void  createBannedUsersTable() {
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

    public static void  createModeratorTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Moderator ("
                + " user_id INTEGER NOT NULL,"
                + " community_id INTEGER NOT NULL,"
                + " assigned_date TEXT,"
                + " permissions TEXT,"
                + " PRIMARY KEY (user_id, community_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (community_id) REFERENCES Community(id)"
                + ");";

        DBConnection.query(sql);
    }

    public static void  createSubscriptionTable() {
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

    public static void  createPostVotesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS PostVotes ("
                + " user_id INTEGER NOT NULL,"
                + " post_id INTEGER NOT NULL,"
                + " vote_type TEXT CHECK(vote_type IN ('like', 'dislike')),"
                + " PRIMARY KEY (user_id, post_id),"
                + " FOREIGN KEY (user_id) REFERENCES User(id),"
                + " FOREIGN KEY (post_id) REFERENCES Post(id)"
                + ");";

        DBConnection.query(sql);
    }
}

