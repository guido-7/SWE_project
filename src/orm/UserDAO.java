package src.orm;

import src.domainmodel.*;
import src.managerdatabase.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
        String sql = "INSERT INTO PostVotes (user_id, post_id,community_id, vote_type) VALUES (?, ?, ?, ?)";
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
}
