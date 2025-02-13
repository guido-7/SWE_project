package src.orm;

import src.domainmodel.Moderator;
import src.domainmodel.Permits;
import src.domainmodel.PermitsManager;
import src.managerdatabase.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class ModeratorDAO extends BaseDAO<Moderator, Integer> {

    @Override
    protected String getFindByIdQuery() {
        return "SELECT * FROM Moderator JOIN User ON Moderator.user_id = User.id  WHERE Moderator.user_id = ?";
    }

    @Override
    protected void setFindByIdParams(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO Moderator (name, email) VALUES (?, ?)";
    }

    @Override
    protected void setInsertParams(PreparedStatement statement, Map<String, Object> parameters) throws SQLException {
        statement.setString(1, (String) parameters.get("name"));
        statement.setString(2, (String) parameters.get("email"));
    }

    @Override
    protected String getUpdateQuery() {
        return "";
    }

    @Override
    protected void setUpdateParams(PreparedStatement statement, Moderator entity) throws SQLException {
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM Moderator WHERE user_id = ?";
    }

    @Override
    protected void setDeleteParams(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);
    }

    @Override
    protected Moderator mapResultSetToEntity(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("user_id");
        LocalDateTime assigned_date = LocalDateTime.parse(resultSet.getString("assigned_date"));
        String nickname = resultSet.getString("nickname");
        String name =resultSet.getString("name");
        String surname = resultSet.getString("surname");
        int community_id = resultSet.getInt("community_id");
        Set<Permits> permits =PermitsManager.createModeratorPermits();
        return new Moderator(id, nickname, name, surname,permits, community_id, assigned_date);
    }

    public boolean isModerator(int moderator_id, int community_id){
        String query = "SELECT * FROM Moderator WHERE user_id = ? AND community_id = ?";
        try( Connection connection = DBConnection.open_connection();
                PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, moderator_id);
            statement.setInt(2, community_id);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
    }
        return false;
    }
}
