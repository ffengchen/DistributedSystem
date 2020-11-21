import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.UUID;

public class Consumer {
  public static String queue_name="messageQueue";
  public static int threadNum = 10;

  public static void main(String[] argv) throws Exception {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost("ec2-18-237-198-95.us-west-2.compute.amazonaws.com");
    connectionFactory.setUsername("fengchen");
    connectionFactory.setPassword("123456");
    connectionFactory.setVirtualHost("/");
    connectionFactory.setPort(5672);
    final Connection connection = connectionFactory.newConnection();


    for (int i = 0; i < threadNum; i++) {
      ConsumerThread consumerThread = new ConsumerThread(connection, queue_name);
      Thread thread = new Thread(consumerThread);
      thread.start();
    }
  }

}
