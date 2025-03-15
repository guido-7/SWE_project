package src.orm;

import src.domainmodel.Rule;
import src.managerdatabase.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RulesDAO extends BaseDAO<Rule, List<Integer>> {

    @Override
    protected String getFindByIdQuery() {
        return "SELECT * FROM Rules WHERE id = ? AND community_id = ?";
    }

    @Override
    protected void setFindByIdParams(PreparedStatement statement, List<Integer> id) throws SQLException {
        statement.setInt(1, id.get(0)); // id regola
        statement.setInt(2, id.get(1)); // id community
    }

    @Override
    protected Rule mapResultSetToEntity(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int communityId = resultSet.getInt("community_id");
        String title = resultSet.getString("title");  // Recupera il nuovo campo "title"
        String content = resultSet.getString("content");
        int priority = resultSet.getInt("priority");
        return new Rule(id, communityId, title, content, priority);  // Includi "title"
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO Rules (community_id, title, content, priority) VALUES (?, ?, ?, ?)";  // Aggiungi "title"
    }

    @Override
    protected void setInsertParams(PreparedStatement statement, Map<String, Object> parameters) throws SQLException {
        statement.setInt(1, (int) parameters.get("community_id"));
        statement.setString(2, (String) parameters.get("title"));  // Aggiungi "title"
        statement.setString(3, (String) parameters.get("content"));
        statement.setInt(4, (int) parameters.getOrDefault("priority", 0));
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE Rules SET title = ?, content = ?, priority = ? WHERE id = ? AND community_id = ?";  // Aggiungi "title"
    }

    @Override
    protected void setUpdateParams(PreparedStatement statement, Rule entity) throws SQLException {
        statement.setString(1, entity.getTitle());  // Usa il nuovo attributo "title"
        statement.setString(2, entity.getContent());
        statement.setInt(3, entity.getPriority());
        statement.setInt(4, entity.getId());
        statement.setInt(5, entity.getCommunity_id());
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM Rules WHERE id = ? AND community_id = ?";
    }

    @Override
    protected void setDeleteParams(PreparedStatement statement, List<Integer> id) throws SQLException {
        statement.setInt(1, id.get(0)); // id regola
        statement.setInt(2, id.get(1)); // id community
    }



}
