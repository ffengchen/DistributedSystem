import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

  static final String DB_URL = "jdbc:mysql://database-2-instance-1-us-west-2b.cl1nigr1wqx0.us-west-2.rds.amazonaws.com:3306/ikkyone?serverTimezone=UTC";
  static final String USER = "root";
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
