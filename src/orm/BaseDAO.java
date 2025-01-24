package src.orm;
import src.managerdatabase.DBConnection;
import java.sql.*;
import java.util.Map;
import java.util.Optional;

public abstract class BaseDAO<T, ID> {
    public BaseDAO() {
    }

    public Optional<T> findById(ID id) throws SQLException {
        String query = getFindByIdQuery();
        try (Connection connection = DBConnection.get_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            setFindByIdParams(statement, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToEntity(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public void save(Map<String, Object> parameters) throws SQLException {
        String query = getInsertQuery();
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            setInsertParams(statement, parameters);
            statement.executeUpdate();
        }
    }

    public void update(T entity) throws SQLException {
        String query = getUpdateQuery();
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            setUpdateParams(statement, entity);
            statement.executeUpdate();
        }
    }
    public void deleteById(ID id) throws SQLException {
        String query = getDeleteQuery();
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            setDeleteParams(statement, id);
            statement.executeUpdate();
        }
    }

    protected abstract String getFindByIdQuery();

    protected abstract void setFindByIdParams(PreparedStatement statement, ID id) throws SQLException;

    protected abstract String getInsertQuery();

    protected abstract void setInsertParams(PreparedStatement statement, Map<String, Object> parameters) throws SQLException;

    protected abstract String getUpdateQuery();

    protected abstract void setUpdateParams(PreparedStatement statement, T entity) throws SQLException;

    protected abstract String getDeleteQuery();

    protected abstract void setDeleteParams(PreparedStatement statement, ID id) throws SQLException;

    protected abstract T mapResultSetToEntity(ResultSet resultSet) throws SQLException;
}
