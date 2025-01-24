package src.orm;

import src.domainmodel.Community;

import java.sql.*;
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

}
