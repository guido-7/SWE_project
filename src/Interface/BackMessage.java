package src.Interface;

import java.sql.SQLException;

@FunctionalInterface
public interface BackMessage {
    void onResult(boolean confirmed) throws SQLException;
}
