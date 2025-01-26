package src.orm;

import src.domainmodel.Admin;
import src.domainmodel.Permits;
import src.domainmodel.PermitsManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public class AdminDAO extends BaseDAO<Admin,Integer>{
    UserDAO userDAO = new UserDAO();

    @Override
    protected String getFindByIdQuery() {
        return userDAO.getFindByIdQuery();
    }

    @Override
    protected void setFindByIdParams(PreparedStatement statement, Integer id) throws SQLException {
        userDAO.setFindByIdParams(statement, id);
    }

    @Override
    protected String getInsertQuery() {
        return userDAO.getInsertQuery();
    }

    @Override
    protected void setInsertParams(PreparedStatement statement, Map<String, Object> parameters) throws SQLException {
        userDAO.setInsertParams(statement, parameters);
    }

    @Override
    protected String getUpdateQuery() {
        return userDAO.getUpdateQuery();
    }

    @Override
    protected void setUpdateParams(PreparedStatement statement, Admin entity) throws SQLException {
        statement.setString(1, entity.getNickname());
        statement.setInt(2, entity.getId());
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM Admin WHERE id = ?";
    }

    @Override
    protected void setDeleteParams(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);

    }

    @Override
    protected Admin mapResultSetToEntity(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String nickname = resultSet.getString("nickname");
        String name = resultSet.getString("name");
        String surname = resultSet.getString("surname");
        Set<Permits> permits = PermitsManager.createAdminPermits();  // Assuming permits need to be fetched from another table
        return new Admin(id, nickname, name, surname, permits);
    }
}
