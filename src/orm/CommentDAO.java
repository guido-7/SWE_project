package src.orm;

import src.domainmodel.Comment;
import src.managerdatabase.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//List Integer: post_id, comment_id
public class CommentDAO extends BaseDAO<Comment, List<Integer>> {

    @Override
    protected String getFindByIdQuery() {
        return "SELECT * FROM Comment WHERE post_id = ? AND id = ?";
    }

    @Override
    protected void setFindByIdParams(PreparedStatement statement, List<Integer> id) throws SQLException {
        statement.setInt(1, id.get(0));
        statement.setInt(2, id.get(1));
    }

    @Override
    protected Comment mapResultSetToEntity(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int post_id = resultSet.getInt("post_id");
        int level = resultSet.getInt("level");
        LocalDateTime time = LocalDateTime.parse(resultSet.getString("time"));
        int likes = resultSet.getInt("likes");
        int dislikes = resultSet.getInt("dislike");
        String content = resultSet.getString("content");
        int user_id = resultSet.getInt("user_id");
        boolean is_modified = resultSet.getBoolean("is_modified");
        return new Comment(id, post_id, level, user_id, content, likes, dislikes, time, is_modified);
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO Comment (post_id, level, user_id, content, time,community_id) VALUES (?, ?, ?, ?, ?, ?)";
        //section-->logica-->comment.save()-->commento padre e commenti figli-->commento to a child-->
        //comment(paretent.getlevle+1,parent.)
    }

    @Override
    protected void setInsertParams(PreparedStatement statement, Map<String, Object> parameters) throws SQLException {
        statement.setInt(1, (int) parameters.get("post_id"));
        statement.setInt(2, (int) parameters.get("level"));
        statement.setInt(3, (int) parameters.get("user_id"));
        statement.setString(4, (String) parameters.get("content"));
        statement.setString(5, LocalDateTime.now().toString());
        statement.setInt(6, (int) parameters.get("community_id"));
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE Comment SET time = ?, content = ?, is_modified = 1 WHERE id = ?";
    }

    @Override
    protected void setUpdateParams(PreparedStatement statement, Comment entity) throws SQLException {
        statement.setTimestamp(1, java.sql.Timestamp.valueOf(entity.getTime()));
        statement.setString(2, entity.getContent());
        statement.setInt(3, entity.getId());
    }

    @Override
    protected String getDeleteQuery() {
        return "WITH RECURSIVE descendants(id) AS ( " +
                "SELECT ? " +
                "UNION " +
                "SELECT c.id FROM CommentHierarchy c " +
                "JOIN descendants d ON c.parent_id = d.id " +
                ")  " +
                "DELETE FROM Comment WHERE id IN (SELECT id FROM descendants)";
    }

    @Override
    protected void setDeleteParams(PreparedStatement statement, List<Integer> id) throws SQLException {
        statement.setInt(1, id.get(0));
        statement.setInt(2, id.get(1));
    }

    public void saveCommentRelation(Map<String, Object> parameters) throws SQLException {
        String query = getInsertQueryCommentRelation();
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            setInsertParamsCommentRelation(statement, parameters);
            statement.executeUpdate();
        }
    }

    public String getInsertQueryCommentRelation() {
        return "INSERT INTO CommentHierarchy (post_id, parent_id, child_id) VALUES (?, ?, ?)";
    }

    protected void setInsertParamsCommentRelation(PreparedStatement statement, Map<String, Object> parameters) throws SQLException {
        statement.setInt(1, (int) parameters.get("post_id"));
        statement.setInt(2, (int) parameters.get("parent_id"));
        statement.setInt(3, (int) parameters.get("child_id"));
    }

    public boolean save(Comment comment, Integer parentId, Integer parentLevel) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        if ((parentLevel + 1) != comment.getLevel()) {
            return false;
        }

        try {
            conn = DBConnection.open_connection();
            conn.prepareStatement("PRAGMA foreign_keys = ON").executeUpdate();
            conn.setAutoCommit(false);  // Start transaction

            // 1. Insert the comment
            String insertSQL = """
                INSERT INTO Comment (post_id, level, user_id, content)
                VALUES (?, ?, ?, ?)
            """;

            stmt = conn.prepareStatement(insertSQL);
            stmt.setInt(1, comment.getPost_id());
            stmt.setInt(2, comment.getLevel());
            stmt.setInt(3, comment.getUser_id());
            stmt.setString(4, comment.getContent());
            stmt.executeUpdate();

            conn.commit();


            // 2. Get the generated ID
            String getIdSQL = """
                SELECT id 
                FROM Comment 
                WHERE rowid = last_insert_rowid()
            """;

            stmt = conn.prepareStatement(getIdSQL);
            rs = stmt.executeQuery();

            if (rs.next()) {
                comment.setId(rs.getInt("id"));
                System.out.println("Generated ID: " + comment.getId());
            }

            // 3. If this is a reply, add to hierarchy
            if (parentId != null) {
                String hierarchySQL = """
                    INSERT INTO CommentHierarchy (post_id, parent_id, child_id)
                    VALUES (?, ?, ?)
                """;

                stmt = conn.prepareStatement(hierarchySQL);
                stmt.setInt(1, comment.getPost_id());
                stmt.setInt(2, parentId);
                stmt.setInt(3, comment.getId());
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public ArrayList<Integer> getCommunityIds(int userId, int numberofCommunities) {
        String sql = "SELECT community_id,count(user_id) as commento FROM Comment WHERE user_id = ? GROUP BY community_id ORDER BY commento  DESC LIMIT ?";
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

    public ArrayList<Object> getContentAndUserById(int comment_id, int post_id) {
        ArrayList<Object> data = new ArrayList<>();
        try (Connection connection = DBConnection.open_connection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT content, user_id FROM Comment WHERE id = ? AND post_id = ?");
            statement.setInt(1, comment_id);
            statement.setInt(1, post_id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                data.add(resultSet.getString("content"));
                data.add(resultSet.getInt("user_id"));
                return data;
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return null;
    }

    public int getLevelById(int comment_id, int post_id) {
        try (Connection connection = DBConnection.open_connection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT level FROM Comment WHERE id = ? AND post_id = ?");
            statement.setInt(1, comment_id);
            statement.setInt(2, post_id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("level");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            }
        return -1;
    }
}



