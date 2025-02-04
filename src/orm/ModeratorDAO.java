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
        return "SELECT * FROM Moderator JOIN User ON Moderator.id = User.id WHERE Moderator.id = ?";
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
        return "DELETE FROM Moderator WHERE id = ?";
    }

    @Override
    protected void setDeleteParams(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);
    }

    @Override
    protected Moderator mapResultSetToEntity(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        LocalDateTime assigned_date = LocalDateTime.parse(resultSet.getString("assigned_date"));
        String nickname = resultSet.getString("nickname");
        String name =resultSet.getString("name");
        String surname = resultSet.getString("surname");
        int community_id = resultSet.getInt("community_id");
        Set<Permits> permits =PermitsManager.createModeratorPermits();
        return new Moderator(id, nickname, name, surname,permits, community_id, assigned_date);
    }
    public void giveWarning(int user_id, int community_id) throws SQLException {
        String selectQuery = "SELECT no_warnings FROM UserWarnings WHERE user_id = ? AND community_id = ?";
        String insertOrUpdateQuery = "INSERT INTO UserWarnings (user_id, community_id, no_warnings) VALUES (?, ?, ?) " +
                "ON CONFLICT(user_id, community_id) DO UPDATE SET no_warnings = no_warnings + 1";
        String deleteQuery = "DELETE FROM UserWarnings WHERE user_id = ? AND community_id = ?";
        String insertBannedQuery = "INSERT INTO BannedUsers (user_id, community_id, ban_date, reason) VALUES (?, ?, ?, ?)";
        String checkBannedQuery = "SELECT 1 FROM BannedUsers WHERE user_id = ? AND community_id = ?";

        try (Connection connection = DBConnection.open_connection();
             PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
             PreparedStatement insertOrUpdateStmt = connection.prepareStatement(insertOrUpdateQuery);
             PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
             PreparedStatement insertBannedStmt = connection.prepareStatement(insertBannedQuery);
             PreparedStatement checkBannedStmt = connection.prepareStatement(checkBannedQuery)) {

            checkBannedStmt.setInt(1, user_id);
            checkBannedStmt.setInt(2, community_id);
            ResultSet bannedRs = checkBannedStmt.executeQuery();

            if (bannedRs.next()) {
                System.out.println("User is already banned");
                return;
            }

            selectStmt.setInt(1, user_id);
            selectStmt.setInt(2, community_id);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next() && rs.getInt("no_warnings") == 2) {
                deleteStmt.setInt(1, user_id);
                deleteStmt.setInt(2, community_id);
                deleteStmt.executeUpdate();

                insertBannedStmt.setInt(1, user_id);
                insertBannedStmt.setInt(2, community_id);
                insertBannedStmt.setString(3, LocalDateTime.now().toString());
                insertBannedStmt.setString(4, "warnings threshold reached");
                insertBannedStmt.executeUpdate();
            } else {
                insertOrUpdateStmt.setInt(1, user_id);
                insertOrUpdateStmt.setInt(2, community_id);
                insertOrUpdateStmt.executeUpdate();
            }
        }
    }
}
