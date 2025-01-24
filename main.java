import src.managerdatabase.DBConnection;
import src.managerdatabase.SetDB;
import src.orm.CommunityDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class main {
    public static void main(String[] args) throws SQLException {
        DBConnection.connect();
        SetDB.createDB();
        DBConnection.disconnect();


    }
}
