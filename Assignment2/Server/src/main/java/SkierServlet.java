import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;


@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

    res.setContentType("application/json;charset=UTF-8");
    PrintWriter out = res.getWriter();
    String urlPath = req.getPathInfo();
    String[] urlParts = urlPath.split("/");
    if (urlParts.length == 4) {
      String skierId = urlParts[2];
      String queryParams = req.getQueryString();
      String resortId = queryParams.split("=")[1];
      String ans = getVerticalForSpecificResort(skierId, resortId);
      JSONObject jsonObject = new JSONObject();
      if (ans.equals("")) {
        try {
          jsonObject.put("message", "invalid input");
        } catch (JSONException e) {
          e.printStackTrace();
        }
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        //out.println(jsonObject);
        out.write(jsonObject.toString());
      } else {
        try {
          jsonObject.put("resortID", resortId);
          jsonObject.put("totalVert", ans);
        } catch (JSONException e) {
          e.printStackTrace();
        }
        out.println(jsonObject);
        //out.write(jsonObject.toString());

        res.setStatus(HttpServletResponse.SC_OK);
      }
    } else if (urlParts.length == 7) {
      String skierId = urlParts[6];
      String resortId = urlParts[2];
      String dayId = urlParts[4];
      String ans = getVerticalForSpecificDay(skierId, resortId, dayId);
      JSONObject jsonObject = new JSONObject();
      if (ans.equals("")) {
        try {
          jsonObject.put("message", "invalid input");
        } catch (JSONException e) {
          e.printStackTrace();
        }
        out.println(jsonObject);
        //out.write(jsonObject.toString());

        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      } else {

        try {
          jsonObject.put("resortID", resortId);
          jsonObject.put("totalVert", ans);
        } catch (JSONException e) {
          e.printStackTrace();
        }
        out.println(jsonObject);
        //out.write(jsonObject.toString());

        res.setStatus(HttpServletResponse.SC_OK);
      }
    } else {
      JSONObject jsonObject = new JSONObject();
      try {
        jsonObject.put("message", "data not found");
      } catch (JSONException e) {
        e.printStackTrace();
      }
      out.println(jsonObject);
      //out.write(jsonObject.toString());

      res.setStatus(HttpServletResponse.SC_NOT_FOUND);

    }

  }


  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json;charset=UTF-8");
    JsonObject jsonObject = new JsonParser().parse(req.getReader()).getAsJsonObject();
    String skierId = jsonObject.get("skierID").getAsString();
    String resortId = jsonObject.get("resortID").getAsString();
    String time = jsonObject.get("time").getAsString();
    String liftId = jsonObject.get("liftID").getAsString();
    String dayId = jsonObject.get("dayID").getAsString();
    String vertical = String.valueOf(Integer.valueOf(liftId) * 10);
    JSONObject jsonObject1 = new JSONObject();
    PrintWriter out = res.getWriter();
    if (skierId == null || resortId == null || time == null || liftId == null || dayId == null || vertical == null) {
      try {
        jsonObject1.put("message", "invalid inputs");
      } catch (JSONException e) {
        e.printStackTrace();
      }
      out.println(jsonObject1);
      //out.write(jsonObject1.toString());

      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    boolean ans = addLiftRide(skierId, resortId, dayId, time, liftId, vertical);
    if (!ans) {
      try {
        jsonObject1.put("message", "data not found");
      } catch (JSONException e) {
        e.printStackTrace();
      }
      out.println(jsonObject1);
      //out.write(jsonObject1.toString());

      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      try {
        jsonObject1.put("message", "write successful");
      } catch (JSONException e) {
        e.printStackTrace();
      }
      out.println(jsonObject1);
      //out.write(jsonObject1.toString());

      res.setStatus(HttpServletResponse.SC_OK);
    }


  }



  public boolean addLiftRide(String skierId, String resortId, String dayId, String time, String liftId, String vertical) {
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



  public  String getVerticalForSpecificDay(String skierId, String resortId, String dayId) {
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

  public String getVerticalForSpecificResort(String skierId, String resortId) {
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

}
