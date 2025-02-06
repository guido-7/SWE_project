package src.orm;

import src.domainmodel.*;
import src.managerdatabase.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public class UserDAO extends BaseDAO<User,Integer> {

    @Override
    protected String getFindByIdQuery() {
        return "SELECT * FROM User WHERE id = ?";
    }

    @Override
    protected void setFindByIdParams(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO User (nickname, name, surname) VALUES (?, ?, ?)";
    }

    @Override
    protected void setInsertParams(PreparedStatement statement, Map<String, Object> parameters) throws SQLException {
        statement.setString(1, (String) parameters.get("nickname"));
        statement.setString(2, (String) parameters.get("name"));
        statement.setString(3, (String) parameters.get("surname"));
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE User SET nickname = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParams(PreparedStatement statement, User entity) throws SQLException {
        statement.setString(1, entity.getNickname());
        statement.setInt(2, entity.getId());
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM User WHERE id = ?";
    }

    @Override
    protected void setDeleteParams(PreparedStatement statement, Integer integer) throws SQLException {
        statement.setInt(1, integer);

    }

    @Override
    protected User mapResultSetToEntity(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String nickname = resultSet.getString("nickname");
        String name = resultSet.getString("name");
        String surname = resultSet.getString("surname");
        Set<Permits> permits = PermitsManager.createAdminPermits();  // Assuming permits need to be fetched from another table
        return new Admin(id, nickname, name, surname, permits);
    }

    public boolean isValidUser(String email, String password) throws SQLException {
        String query = "SELECT COUNT(*) FROM UserAccess WHERE email = ? AND password = ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    return true; // Email e password corrette
                }
            }
        }
        return false; // Credenziali errate
    }


}
