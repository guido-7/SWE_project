package src.orm;

import src.domainmodel.CommentWarnings;
import src.domainmodel.Community;
import src.domainmodel.Rule;
import src.domainmodel.PostWarnings;
import src.managerdatabase.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

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
        int subscribers = resultSet.getInt("subs");
        int monthlyVisits = resultSet.getInt("visits");
        int score = resultSet.getInt("scores");
        return new Community(id, title, description, subscribers, monthlyVisits, score);
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
            System.out.println("No title found with this query");
        }
        return communities;
    }

    public ArrayList<PostWarnings> getPostWarnings(int communityId) {
        UserDAO userDAO = new UserDAO();
        PostDAO postDao = new PostDAO();
        ArrayList<PostWarnings> reports = new ArrayList<>();
        String sql = "SELECT * FROM PostWarnings WHERE community_id = ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, communityId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                            int sender_id = resultSet.getInt("sender_id");
                            int post_id = resultSet.getInt("post_id");
                            String sender_nickname = userDAO.getNicknameById(sender_id);
                            ArrayList<Object> data = postDao.getTitleAndUserById(post_id, communityId);
                            String title = (String) data.getFirst();
                            String content = (String) data.get(1);
                            int reported_user_id = (int) data.getLast();
                            String reported_nickname = userDAO.getNicknameById(reported_user_id);
                            reports.add(new PostWarnings(sender_id, sender_nickname,content, post_id, reported_user_id ,reported_nickname,title));
                }
                return reports;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    public ArrayList<CommentWarnings>  getCommentWarnings(int communityId) {
        UserDAO userDAO = new UserDAO();
        PostDAO postDao = new PostDAO();
        CommentDAO commentDAO = new CommentDAO();
        ArrayList<CommentWarnings> reports = new ArrayList<>();
        String sql = "SELECT sender_id, comment_id, post_id  FROM CommentWarnings WHERE community_id = ?";
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, communityId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {

                    int sender_id = resultSet.getInt("sender_id");
                    int post_id = resultSet.getInt("post_id");
                    int comment_id = resultSet.getInt("comment_id");

                    String sender_nickname = userDAO.getNicknameById(resultSet.getInt("sender_id"));
                    String title = postDao.getTitle(post_id, communityId);
                    ArrayList<Object> data = commentDAO.getContentAndUserById(comment_id, post_id);
                    String content = (String) data.getFirst();
                    int reported_user_id = (int) data.getLast();

                    String reported_nickname = userDAO.getNicknameById(reported_user_id);
                    int level= commentDAO.getLevelById(comment_id, post_id);
                    reports.add(new CommentWarnings(sender_id, sender_nickname, content, post_id, reported_user_id,reported_nickname,title, comment_id, level));

                }
                return reports;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
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

    public void saveRules(int CommunityId,Map<Integer,ArrayList<String>> rulesMapping) {
        for ( Integer key : rulesMapping.keySet() ) {
            ArrayList<String> titleAndContent = rulesMapping.get(key);
            String title = titleAndContent.getFirst();
            String content = titleAndContent.getLast();
            String sql = "INSERT INTO Rules (community_id,title, content, priority) VALUES ( ?, ?, ? , ? )";
            try (Connection connection = DBConnection.open_connection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, CommunityId);
                    statement.setString(2,title);
                    statement.setString(3,content);
                    statement.setInt(4, key);
                    statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();

            }
        }
    }
}

