import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;
import java.util.List;
import com.google.gson.JsonObject;

public class ConsumerThread implements Runnable {

  public com.rabbitmq.client.Connection connection;
  public String queueName;

  public ConsumerThread(com.rabbitmq.client.Connection connection, String queueName) {
    this.connection = connection;
    this.queueName = queueName;
  }




  public boolean addLiftRide(String skierId, String resortId, String dayId, String time,
      String liftId, String vertical) {
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
      ans = true;
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


  @Override
  public void run() {
    try {
      Channel channel = connection.createChannel();
      channel.queueDeclare(queueName, true, false, false, null);
      channel.basicQos(1);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        String[] postData = message.split("_");
        String resortID = postData[1];
        String dayID = postData[4];
        String skierID = postData[0];
        String time = postData[2];
        String liftID = postData[3];
        int vertical = Integer.valueOf(liftID) * 10;

        addLiftRide(skierID, resortID, dayID, time, liftID, String.valueOf(vertical));
      };

      channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      e.getStackTrace();
    }
  }
}
