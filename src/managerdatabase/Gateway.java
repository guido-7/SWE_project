package src.managerdatabase;

import java.sql.Connection;

public abstract class Gateway {
    static Connection connection = DBConnection.connect();
}
