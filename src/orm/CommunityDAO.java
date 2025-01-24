package src.orm;

import src.domainmodel.Community;

import java.sql.*;

public class CommunityDAO extends BaseDAO<Community, Integer> {

    @Override
    protected String getFindByIdQuery() {
        return "SELECT * FROM community WHERE id = ?";
    }

    @Override
    protected void setFindByIdParams(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);
    }

    @Override
    protected Community mapResultSetToEntity(ResultSet resultSet) throws SQLException {
        Community community = new Community();
        return community;
    }
}
