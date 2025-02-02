package src.orm;

import src.domainmodel.Post;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

public class PostDao extends BaseDAO<Post, Integer>{
    @Override
    protected String getFindByIdQuery() {
        return "SELECT * FROM Post WHERE id = ?";
    }

    @Override
    protected void setFindByIdParams(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);
    }

    @Override
    protected Post mapResultSetToEntity(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        LocalDateTime time = LocalDateTime.parse(resultSet.getString("time"));
        String title = resultSet.getString("title");
        String content = resultSet.getString("content");
        int user = resultSet.getInt("user_id");
        int community = resultSet.getInt("community_id");
        int likes = resultSet.getInt("likes");
        int dislikes = resultSet.getInt("dislikes");
        return new Post(id, time, title, content, user, community, likes, dislikes);
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO Post (time, title, content, user_id, community_id) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParams(PreparedStatement statement, Map<String, Object> parameters) throws SQLException {
        statement.setString(1, LocalDateTime.now().toString());
        statement.setString(2, (String) parameters.get("title"));
        statement.setString(3, (String) parameters.get("content"));
        statement.setInt(4, (Integer) parameters.get("user_id"));
        statement.setInt(5, (Integer) parameters.get("community_id"));
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE Post SET time = ?, title = ?, content = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParams(PreparedStatement statement, Post entity) throws SQLException {
        statement.setString(1, LocalDateTime.now().toString());
        statement.setString(2, entity.getTitle());
        statement.setString(3, entity.getContent());
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM Post WHERE id = ?";
    }

    @Override
    protected void setDeleteParams(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);
    }
}
