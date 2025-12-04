import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DBUtil {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        try (InputStream input = DBUtil.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            if (input == null) {
                throw new RuntimeException("Could not find app.properties on classpath");
            }

            Properties props = new Properties();
            props.load(input);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            connection = DriverManager.getConnection(url, user, password);
            return connection;
        } catch (IOException e) {
            throw new RuntimeException("Error loading app.properties", e);
        }
    }
}
