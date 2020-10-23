import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;

public class DatabaseConnector {
  private static BasicDataSource dataSource;

  static final String DB_URL = "jdbc:mysql://localhost:3306/ikkyone?serverTimezone=UTC";
  static final String USER = "root";
  static final String PASS = "12345678";

  public static Connection getConnection(){
    Connection connection = null;
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      connection = DriverManager.getConnection(DB_URL, USER, PASS);


//      dataSource = new BasicDataSource();
//      dataSource.setUrl(DB_URL);
//      dataSource.setUsername(USERNAME);
//      dataSource.setPassword(PASSWORD);
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
