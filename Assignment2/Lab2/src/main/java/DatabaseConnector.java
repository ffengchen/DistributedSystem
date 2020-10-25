import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;

public class DatabaseConnector {

  static final String DB_URL = "jdbc:mysql://database-2.cwhvdiawe3df.us-west-1.rds.amazonaws.com:3306/distributedsystem?serverTimezone=UTC";
  static final String USER = "admin";
  static final String PASS = "12345678";

  public static Connection getConnection(){
    Connection connection = null;
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      connection = DriverManager.getConnection(DB_URL, USER, PASS);

    } catch (Exception e) {
      System.out.println(e);
    }
    return connection;
  }

  public static void closeConnection(Connection connection) {
    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


}
