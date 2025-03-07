package src.orm;
import src.managerdatabase.DBConnection;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseDAO<T, ID> {
    public BaseDAO() {
    }

    public Optional<T> findById(ID id) throws SQLException {
        String query = getFindByIdQuery();
        try (Connection connection = DBConnection.open_connection();
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

    public int save(Map<String, Object> parameters) throws SQLException {
        String query = getInsertQuery();
        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            setInsertParams(statement, parameters);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Restituisci l'ID generato (long)
                }
            }
        }
        return -1;
    }

    public int save(Connection conn, Map<String, Object> parameters) throws SQLException {
        String query = getInsertQuery();
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            setInsertParams(statement, parameters);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
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

    public Object retrieveSingleAttribute(String tableName, String columnName, String whereClause, Object... params) throws SQLException {
        String query = "SELECT " + columnName + " FROM " + tableName + " WHERE " + whereClause;

        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getObject(1);
                }
            }
        }
        return null; // Se non trova nulla
    }

    public void insertSingleAttribute(String tableName, String columnName, Object value) throws SQLException {
        String query = "INSERT INTO " + tableName + " (" + columnName + ") VALUES (?)";

        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, value);
            statement.executeUpdate();
        }
    }

    public void insertMultipleValues(String tableName, String[] columnNames, Object... values) throws SQLException {
        String columnName = String.join(",", columnNames);
        String placeholders = String.join(",", Collections.nCopies(values.length, "?"));
        String query = "INSERT OR UPDATE INTO " + tableName + " (" + columnName + ") VALUES (" + placeholders + ")";

        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            statement.executeUpdate();
        }
    }

    public void updatesingleAttribute(String tableName, String columnName, Object value, String whereClause, Object... params) throws SQLException {
        String query = "UPDATE " + tableName + " SET " + columnName + " = ? WHERE " + whereClause;

        try (Connection connection = DBConnection.open_connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, value);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 2, params[i]);
            }
            statement.executeUpdate();
        }
    }

}
