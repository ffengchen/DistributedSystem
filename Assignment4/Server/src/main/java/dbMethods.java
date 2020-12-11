import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class dbMethods {
  public static String getVerticalForSpecificResort(String skierId, String resortId) {
    String query
        = "SELECT SUM(vertical) FROM LiftRides"
        + " WHERE skierId = " + skierId
        + " AND resortId =" + "\"" + resortId + "\"";

    String ans = "";
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    try {

      conn = DatabaseConnector.getConnection();
      preparedStatement = conn.prepareStatement(query);
      ResultSet resultSet = preparedStatement.executeQuery();
      if (resultSet.next()) {
        ans = resultSet.getString(1);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {

          DatabaseConnector.closeConnection(conn);
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (Exception se) {
        se.printStackTrace();
      }
    }

    return ans;
  }

  public  static String getVerticalForSpecificDay(String skierId, String resortId, String dayId) {
    String query
        = "SELECT SUM(vertical) FROM LiftRides"
        + " WHERE skierId = " + skierId
        + " AND resortId = " + "\"" + resortId + "\""
        + " AND dayId = " + dayId;


    String ans = "";
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    try {


      conn = DatabaseConnector.getConnection();
      preparedStatement = conn.prepareStatement(query);
      ResultSet resultSet = preparedStatement.executeQuery();
      if (resultSet.next()) {
        ans = resultSet.getString(1);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          DatabaseConnector.closeConnection(conn);
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (Exception se) {
        se.printStackTrace();
      }
    }

    return ans;
  }

  public static boolean addLiftRide(String skierId, String resortId, String dayId, String time, String liftId, String vertical) {
    Connection conn = null;
    boolean ans = false;
    PreparedStatement preparedStatement = null;
    try {

      conn = DatabaseConnector.getConnection();
      String uuid = UUID.randomUUID().toString().replace("-", "");

      String insert = "INSERT INTO LiftRides (skierId, resortId, dayId, time, liftId, vertical, liftrideId)  VALUES (?,?,?,?,?,?,?)";
      preparedStatement = conn.prepareStatement(insert);
      preparedStatement.setString(1, skierId);
      preparedStatement.setString(2, resortId);

      preparedStatement.setString(3, dayId);
      preparedStatement.setString(4, time);

      preparedStatement.setString(5, liftId);

      preparedStatement.setString(6, vertical);
      preparedStatement.setString(7, uuid);
      preparedStatement.executeUpdate();
      ans =  true;
    } catch (Exception e) {
      System.out.println(e);
    } finally {
      try {
        DatabaseConnector.closeConnection(conn);
        preparedStatement.close();

      } catch (Exception e) {
        e.printStackTrace();
      }

    }
    return ans;
  }
}
