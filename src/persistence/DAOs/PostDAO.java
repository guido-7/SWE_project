package src.persistence.DAOs;

import src.domainmodel.Post;
import src.persistence.dbmanager.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDAO extends BaseDAO<Post, Integer> {

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

    public ArrayList<Post> getPosts(int communityId, int PostCount, int offset) {
        ArrayList<Post> posts = new ArrayList<>();
        try (Connection connection = DBConnection.open_connection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Post WHERE community_id = ? ORDER BY (time * 0.6 + likes * 0.4)    DESC LIMIT ? OFFSET ?");
            statement.setInt(1, communityId);
            statement.setInt(2, PostCount);
            statement.setInt(3, offset);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                posts.add(mapResultSetToEntity(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public List<Integer> getCommunityIds(int id, int numberofposts) {
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
        try {
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

    public ArrayList<Post> searchByTitle(String query, int communityId) {
        ArrayList<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM Post WHERE community_id = ? AND title LIKE ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, communityId);
            statement.setString(2, "%" + query + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(mapResultSetToEntity(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public ArrayList<Post> getFilteredPosts(int communityId, String query, int limit, int offset) {
        ArrayList<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM Post WHERE community_id = ? AND title LIKE ? LIMIT ? OFFSET ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, communityId);
            statement.setString(2, "%" + query + "%");
            statement.setInt(3, limit);
            statement.setInt(4, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(mapResultSetToEntity(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public List<Post> getPostsByUser(int userId) {
        List<Post> posts = new ArrayList<>();
        try (Connection connection = DBConnection.open_connection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Post WHERE user_id = ?");
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                posts.add(mapResultSetToEntity(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public List<Post> getSavedPosts(int userId, int limit, int offset) {
        List<Post> posts = new ArrayList<>();
        try (Connection connection = DBConnection.open_connection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Post WHERE id IN (SELECT post_id FROM SavedPost WHERE user_id = ?) LIMIT ? OFFSET ?");
            statement.setInt(1, userId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                posts.add(mapResultSetToEntity(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public boolean isLiked(int userId, int postId) {
        String query = "SELECT COUNT(*) FROM PostVotes WHERE post_id = ? AND user_id = ? AND vote_type = 1";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, postId);
            statement.setInt(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isDisliked(int userId, int postId) {
        String query = "SELECT COUNT(*) FROM PostVotes WHERE post_id = ? AND user_id = ? AND vote_type = 0";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, postId);
            statement.setInt(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addPostWarning(Post post, int senderId) {
        String query = "INSERT INTO PostWarnings (sender_id, post_id, community_id) VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, senderId);
            statement.setInt(2, post.getId());
            statement.setInt(3, post.getCommunityId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void signalPost(int senderId, int postId, int communityId) {
        String query = "INSERT INTO PostWarnings (sender_id, post_id, community_id) VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, senderId);
            statement.setInt(2, postId);
            statement.setInt(3, communityId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isReported(int senderId, int postId) {
        String query = "SELECT COUNT(*) FROM PostWarnings WHERE sender_id = ? AND post_id = ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, senderId);
            statement.setInt(2, postId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Integer> getPinnedPosts(int communityId) {
        List<Integer> postIds = new ArrayList<>();
        String query = "SELECT post_id FROM PinnedPost WHERE community_id = ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, communityId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    postIds.add(resultSet.getInt("post_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero dei post pinnati", e);
        }
        return postIds;
    }

    public void insertPinnedPost(Map<String, Object> parameters) {
        String query = "INSERT INTO PinnedPost (post_id, community_id) VALUES ( ?, ?)";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, (Integer) parameters.get("post_id"));
            statement.setInt(2, (Integer) parameters.get("community_id"));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removePinnedPost(int postId, int communityId) {
        String query = "DELETE FROM PinnedPost WHERE post_id = ? and community_id = ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, postId);
            statement.setInt(2, communityId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isPinned(int postId, int communityId) {
        String query = "SELECT COUNT(*) FROM PinnedPost WHERE post_id = ? AND community_id = ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, postId);
            statement.setInt(2, communityId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();

       }
        return false;
    }

    public List<Post> findByCommunityId(int communityId) {
        List<Post> posts = new ArrayList<>();
        try (Connection connection = DBConnection.open_connection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Post WHERE community_id = ?");
            statement.setInt(1, communityId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                posts.add(mapResultSetToEntity(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
    }
        return posts;
    }

    public Map<Integer, Integer> getSenders(ArrayList<Post> posts) {
        Map<Integer, Integer> senderIds = new HashMap<>();
        String query = "SELECT sender_id FROM PostWarnings WHERE post_id = ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (Post post : posts) {
                statement.setInt(1, post.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        senderIds.put(resultSet.getInt("sender_id"), post.getId());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return senderIds;
    }
}