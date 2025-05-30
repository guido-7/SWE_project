package src.persistence.DAOs;

import src.domainmodel.Community;
import src.domainmodel.Subscription;
import src.persistence.dbmanager.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

    public boolean isSubscribed(int userId,int communityId) {
        String sql = "SELECT * FROM Subscription WHERE user_id = ? AND community_id = ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, communityId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void subscribe(int userId, int communityId) throws SQLException {
        save(Map.of("user_id", userId, "community_id", communityId));
    }

    public void unsubscribe(int userId, int communityId) throws SQLException {
        deleteById(List.of(userId, communityId));
    }

    public static List<Community> getSubscribedCommunities(String query,int userId) {
        String sql = "SELECT * FROM Subscription INNER JOIN Community ON Community.id = Subscription.community_id WHERE user_id = ? AND  Community.title LIKE  ? ";
        List<Community> communities = new ArrayList<>();
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, "%" + query + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int communityId = resultSet.getInt("community_id");
                String communityTitle = resultSet.getString("title");
                String description = resultSet.getString("description");
                communities.add(new Community(communityId, communityTitle,description));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return communities;
    }

    public ArrayList<Integer> getSubs(int maxSubscribedNo, int communityId,int offset) {
        String sql = "SELECT user_id FROM Subscription WHERE community_id = ? ORDER BY subscription_date DESC LIMIT ? OFFSET ?";
        ArrayList<Integer> communityIds = new ArrayList<>();
        try (Connection connection = DBConnection.open_connection() ;
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, communityId);
            statement.setInt(2, maxSubscribedNo);
            statement.setInt(3, offset);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                communityIds.add(resultSet.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return communityIds;

    }

    public synchronized Map<Integer, String> getFilteredSubs(int communityId, String searchTerm, int maxnumberOfSubsShown, int offset) {
        Map<Integer, String> filteredSubs = new HashMap<>();
        String sql = "SELECT User.id, User.nickname FROM Subscription JOIN User ON Subscription.user_id = User.id WHERE community_id = ? AND User.nickname LIKE ? LIMIT ? OFFSET ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, communityId);
            statement.setString(2, "%" + searchTerm + "%");
            statement.setInt(3, maxnumberOfSubsShown);
            statement.setInt(4, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    filteredSubs.put(resultSet.getInt("id"), resultSet.getString("nickname"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filteredSubs;
    }
}