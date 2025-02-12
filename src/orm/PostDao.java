package src.orm;

import src.domainmodel.Post;
import src.managerdatabase.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    public ArrayList<Post> getPosts(int CommunityId, int PostCount,int Offset) {

        ArrayList<Post> posts = new ArrayList<>();

        try(Connection connection = DBConnection.open_connection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Post WHERE community_id = ? ORDER BY (time * 0.6 + likes * 0.4)    DESC LIMIT ? OFFSET ?");
            statement.setInt(1, CommunityId);
            statement.setInt(2, PostCount);
            statement.setInt(3, Offset);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                posts.add(mapResultSetToEntity(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }
    public List<Integer> getCommunityIds(int id , int numberofposts) {
        List<Integer> community_ids = new ArrayList<>();
        try (Connection connection = DBConnection.open_connection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Post WHERE community_id = ? ORDER BY time DESC LIMIT ?");
            statement.setInt(1, id);
            statement.setInt(2, numberofposts);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                System.out.println(resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Object> getTitleAndUserById(int postId, int communityId) {
        ArrayList<Object> data = new ArrayList<>();
        Connection connection = DBConnection.open_connection();
        try{
            PreparedStatement statement = connection.prepareStatement("SELECT title,user_id,content FROM Post WHERE id = ? AND community_id = ?");
            statement.setInt(1, postId);
            statement.setInt(2, communityId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                data.add(resultSet.getString("title"));
                data.add(resultSet.getString("content"));
                data.add(resultSet.getInt("user_id"));
                return data;
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return data;
    }
    public String getTitle(int postId, int communityId) {
        Connection connection = DBConnection.open_connection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT title FROM Post WHERE id = ? AND community_id = ?");
            statement.setInt(1, postId);
            statement.setInt(2, communityId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("title");
            }
        } catch (SQLException e) {
            e.printStackTrace();

    }
        return null;
    }
}