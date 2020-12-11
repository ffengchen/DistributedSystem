import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import java.io.IOException;
import java.io.PrintWriter;
//import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;




@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {
  public String queue_name="messageQueue";

  public Connection connection;

  @Override
  public void init() throws ServletException {
    super.init();
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost("ec2-52-10-4-246.us-west-2.compute.amazonaws.com");
    connectionFactory.setUsername("fengchen");
    connectionFactory.setPassword("123456");
    connectionFactory.setVirtualHost("/");
    connectionFactory.setPort(5672);
    try {
      connection = connectionFactory.newConnection();

    } catch (Exception e) {
      e.printStackTrace();
      System.out.print(e);
    }
  }

  @Override
  public void destroy() {
    if(connection != null) {

      try {
        connection.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

    res.setContentType("application/json;charset=UTF-8");
    PrintWriter out = res.getWriter();
    String urlPath = req.getPathInfo();
    String[] urlParts = urlPath.split("/");
    if (urlParts.length == 4) {
      String skierId = urlParts[2];
      String queryParams = req.getQueryString();
      String resortId = queryParams.split("=")[1];
      String ans = dbMethods.getVerticalForSpecificResort(skierId, resortId);
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
      String ans = dbMethods.getVerticalForSpecificDay(skierId, resortId, dayId);
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
    res.setStatus(HttpServletResponse.SC_OK);
    String message = String.join("_", skierId, resortId, time, liftId, dayId, vertical);
    Channel channel = connection.createChannel();

    channel.queueDeclare(queue_name, true, false, false, null);
    channel.basicPublish("", queue_name, null, message.getBytes());
    try {
      channel.close();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }




  }











}
