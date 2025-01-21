package src;
import java.sql.*;

public class Demo {
    public static void main(String[] args) throws SQLException {
        String sql = "select capo from capi_abbigliamento where id=2";

        String url = "jdbc:postgresql://localhost:5432/postgres";
        String username = "postgres";
        String password = "Alban.123c";

        Connection conn = DriverManager.getConnection(url, username, password);
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        rs.next();
        String name = rs.getString(1);
        System.out.println(name);
        st.close();
    }
}
