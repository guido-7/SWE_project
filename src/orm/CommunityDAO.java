package src.orm;

import src.domainmodel.Community;
import src.domainmodel.Rule;
import src.managerdatabase.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommunityDAO extends BaseDAO<Community, Integer> {

    @Override
    protected String getFindByIdQuery() {
        return "SELECT * FROM Community WHERE id = ?";
    }

    @Override
    protected void setFindByIdParams(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);
    }

    @Override
    protected Community mapResultSetToEntity(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String title = resultSet.getString("title");
        String description = resultSet.getString("description");
        return new Community(id, title, description);
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO Community ( title, description) VALUES ( ?, ?)";
    }
    // CommunityDAO.save(Community(id,title,description))

    @Override
    protected void setInsertParams(PreparedStatement statement, Map<String,Object> parameters) throws SQLException {
        statement.setString(1, (String) parameters.get("title"));
        statement.setString(2, (String) parameters.get("description"));
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE Community SET title = ?, description = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParams(PreparedStatement statement, Community entity) throws SQLException {
        statement.setString(1, entity.getTitle());
        statement.setString(2, entity.getDescription());
        statement.setInt(3, entity.getId());
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM Community WHERE id = ?";
    }

    @Override
    protected void setDeleteParams(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);
    }

    public Map<Integer, Double> getScore(Map<Integer, Integer> idCountMap) {
        Map<Integer, Double> score = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder("SELECT id, scores * 0.6 + visits * 0.4 FROM Community WHERE id IN (");
        for (int id : idCountMap.keySet()) {
            sql.append(id).append(",");
        }
        sql.deleteCharAt(sql.length() - 1).append(")");

        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                double calculatedScore = resultSet.getDouble(2);
                score.put(id, calculatedScore * idCountMap.get(id));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return score;
    }

    public ArrayList<Integer> getCommunityIds( int numberofCommunities){
        String sql = "SELECT id FROM Community  ORDER BY (scores * 0.6 + visits * 0.4) DESC LIMIT ?";
        ArrayList<Integer> communityIds = new ArrayList<>();
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, numberofCommunities);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                communityIds.add(resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return communityIds;
    }

    public ArrayList<Community> searchByTitle(String query) {
        ArrayList<Community> communities = new ArrayList<>();
        String sql = "SELECT * FROM Community WHERE title LIKE ? LIMIT 10";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + query + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    communities.add(mapResultSetToEntity(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return communities;
    }

    public List<Rule> getCommunityRules(int communityId) {
        List<Rule> rules = new ArrayList<>();
        String sql = "SELECT * FROM Rules WHERE community_id = ? ORDER BY priority ASC ";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, communityId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String content = resultSet.getString("content");
                int priority = resultSet.getInt("priority");
                rules.add(new Rule(id, communityId, title, content, priority));
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return rules;
    }
}
