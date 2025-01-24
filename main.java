import src.managerdatabase.DBConnection;
import src.managerdatabase.SetDB;

public class main {
    public static void main(String[] args) {
        DBConnection.connect();
        SetDB.createDB();
        DBConnection.disconnect();
    }
}
