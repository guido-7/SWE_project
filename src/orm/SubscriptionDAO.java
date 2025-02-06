package src.orm;

import src.domainmodel.Subscription;
import src.managerdatabase.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubscriptionDAO extends BaseDAO<Subscription, List<Integer>> {

    @Override
    protected String getFindByIdQuery() {
        return "SELECT * FROM Subscription WHERE user_id = ? AND community_id = ?";
    }

    @Override
    protected void setFindByIdParams(PreparedStatement statement, List<Integer> id) throws SQLException {
        statement.setInt(1, id.get(0)); // user_id
        statement.setInt(2, id.get(1)); // community_id
    }

    @Override
    protected Subscription mapResultSetToEntity(ResultSet resultSet) throws SQLException {
        int userId = resultSet.getInt("user_id");
        int communityId = resultSet.getInt("community_id");
        LocalDateTime subscriptionDate = resultSet.getTimestamp("subscription_date").toLocalDateTime();

        return new Subscription(userId, communityId, subscriptionDate);
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO Subscription (user_id, community_id, subscription_date) VALUES (?, ?, ?)";
    }

    @Override
    protected void setInsertParams(PreparedStatement statement, Map<String, Object> parameters) throws SQLException {
        statement.setInt(1, (int) parameters.get("user_id"));
        statement.setInt(2, (int) parameters.get("community_id"));
        statement.setString(3, LocalDateTime.now().toString());

    }

    @Override
    protected  String getUpdateQuery() {
        return "UPDATE Subscription SET subscription_date = ? WHERE user_id = ? AND community_id = ?";
    }

    @Override
    protected void setUpdateParams(PreparedStatement statement, Subscription entity) throws SQLException {
        statement.setTimestamp(1, Timestamp.valueOf(entity.getSubscription_date()));
        statement.setInt(2, entity.getUser_id());
        statement.setInt(3, entity.getCommunity_id());
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM Subscription WHERE user_id = ? AND community_id = ?";
    }

    @Override
    protected void setDeleteParams(PreparedStatement statement, List<Integer> id) throws SQLException {
        statement.setInt(1, id.get(0)); // user_id
        statement.setInt(2, id.get(1)); // community_id
    }
    public ArrayList<Integer> getCommunityIds(int userId,int numberofCommunities){
        String sql = "SELECT community_id FROM Subscription INNER JOIN Community ON Community.id = Subscription.community_id WHERE user_id = ? ORDER BY (scores * 0.6 + visits * 0.4) DESC LIMIT ?";
        ArrayList<Integer> communityIds = new ArrayList<>();
        try (Connection connection = DBConnection.open_connection() ;
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
}
