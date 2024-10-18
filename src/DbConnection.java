import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    private static Connection con;

    public static Connection getConnection() {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Database URL, username, and password
            String url = "jdbc:mysql://localhost:3306/rmibanksystem";
            String user = "root";
            String password = ""; // Replace with your actual password if needed

            // Try to establish the connection
            con = DriverManager.getConnection(url, user, password);
            System.out.println("Connection successful!");

        } catch (ClassNotFoundException e) {
            // Could not find the JDBC driver class
            System.out.println("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            // Could not connect to the database
            System.out.println("Connection failed: " + e.getMessage());
        }
        return con;
    }
}
