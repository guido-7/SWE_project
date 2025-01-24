import src.managerdatabase.DBConnection;
import src.managerdatabase.SetDB;
import src.orm.CommunityDAO;

import java.sql.SQLException;
import java.util.Map;

public class main {
    public static void main(String[] args) throws SQLException {
        DBConnection.connect();
        SetDB.createDB();

        CommunityDAO communityDAO = new CommunityDAO();
        communityDAO.save(Map.of( "title", "WorldNews", "description","A place for major news from around the world" ));
        DBConnection.disconnect();
    }
}
