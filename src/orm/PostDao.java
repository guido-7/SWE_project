package src.orm;

import src.domainmodel.Community;
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
        LocalDateTime time = resultSet.getTimestamp("time").toLocalDateTime();
        String content = resultSet.getString("content");
        int user = resultSet.getInt("user_id");
        int community = resultSet.getInt("community_id");
        int likes = resultSet.getInt("likes");
        int dislikes = resultSet.getInt("dislikes");
        return new Post(id, time, content, user, community, likes, dislikes);
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO Post (time, content, user_id, community_id) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParams(PreparedStatement statement, Map<String, Object> parameters) throws SQLException {
        statement.setTimestamp(1, java.sql.Timestamp.valueOf((LocalDateTime) parameters.get("time")));
        statement.setString(2, (String) parameters.get("content"));
        statement.setInt(3, (Integer) parameters.get("user_id"));
        statement.setInt(4, (Integer) parameters.get("community_id"));
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE Post SET time = ?, content = ?, user_id = ?, community_id = ?, likes = ?, dislikes = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParams(PreparedStatement statement, Post entity) throws SQLException {
        statement.setTimestamp(1, java.sql.Timestamp.valueOf(entity.getTime()));
        statement.setString(2, entity.getContent());
        statement.setInt(3, entity.getUserId());
        statement.setInt(4, entity.getCommunityId());
        statement.setInt(5, entity.getLikes());
        statement.setInt(6, entity.getDislikes());
        statement.setInt(7, entity.getId());
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
