package src.managerdatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserGateway extends Gateway {
    public UserGateway() {
        connection = DBConnection.connect();
    }

    public void addUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO Users (username, password) VALUES (?, ?)";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
