package src.orm;

import src.domainmodel.*;
import src.managerdatabase.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
        Set<Permits> permits = PermitsManager.createUserPermits();  // Assuming permits need to be fetched from another table
        return new User(id, nickname, name, surname, permits, Role.USER);
    }

    public ArrayList<Integer> getCommunityIds(int userId, int numberofCommunities){
        String sql = "SELECT community_id, SUM(vote_type) AS total_votes FROM PostVotes WHERE user_id = ? GROUP BY community_id ORDER BY total_votes DESC LIMIT ?";
        ArrayList<Integer> communityIds = new ArrayList<>();

        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, numberofCommunities);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                communityIds.add(resultSet.getInt("community_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return communityIds;
    }

    public void insertPostVotes(Map<String,Object> parameters) throws SQLException {
        String sql = "INSERT OR REPLACE INTO PostVotes (user_id, post_id,community_id, vote_type) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, (int) parameters.get("user_id"));
            statement.setInt(2, (int) parameters.get("post_id"));
            statement.setInt(3, (int) parameters.get("community_id"));
            statement.setInt(4, (int) parameters.get("vote_type"));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removePostVotes(Map<String,Object> parameters) throws SQLException {
        String sql = "DELETE FROM PostVotes WHERE user_id = ? AND post_id = ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, (int) parameters.get("user_id"));
            statement.setInt(2, (int) parameters.get("post_id"));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isValidUser(String email, String password) throws SQLException {
        String query = "SELECT COUNT(*) FROM UserAccess WHERE email = ? AND password = ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    return true; // correct Email and password
                }
            }
        }
        return false; // wrong Email or password
    }

    public boolean isRegisteredUser(String email) {
        String query = "SELECT COUNT(*) FROM User WHERE nickname = ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    return true; // user already registered
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // user not registered
    }

    // nickname name surname
    public void registerUser(String nickname, String name, String surname) {
        String query = "INSERT INTO User (nickname, name, surname) VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, nickname);
            statement.setString(2, name);
            statement.setString(3, surname);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // id nickname password
    public void registerUserAccess(int id, String nickname, String password) {
        String query = "INSERT INTO UserAccess (email, user_id, password) VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, nickname);
            statement.setInt(2, id);
            statement.setString(3, password);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getUserId(String userId) {
        String query = "SELECT id FROM User WHERE nickname = ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getNicknameById(int user_id){
        String query = "SELECT nickname FROM User WHERE id = ?";
        Connection connection = DBConnection.open_connection();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, user_id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public Map<String,String> getUserInfo(int id) {
        String query = "SELECT * FROM User WHERE id = ?";
        Map<String,String> userInfo = new HashMap<>();
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    userInfo.put("nickname", resultSet.getString("nickname"));
                    userInfo.put("name", resultSet.getString("name"));
                    userInfo.put("surname", resultSet.getString("surname"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userInfo;
    }

    public User createUser(String nickname) throws SQLException {
        return findById(getUserId(nickname)).orElse(null);
    }
}
