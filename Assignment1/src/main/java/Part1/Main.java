package Part1;

import java.sql.Timestamp;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Main {

  static int numThreads = 128;
  static int numSkiers = 20000;
  static int liftNum = 40;
  static String dayId = "1";
  static String resortId = "SliverMt";
  //static String path = "http://localhost:8080/skiers/";
  //static String path = "http://ec2-54-193-221-11.us-west-1.compute.amazonaws.com:8080/Lab2_war_exploded/skiers";
  static String path;
  static CountDownLatch latch1;
  static CountDownLatch latch2;
  static CountDownLatch latch3;
  private static Logger logger = Logger.getLogger(Main.class);
  static AtomicInteger successCount =  new AtomicInteger(0);
  static AtomicInteger failCount = new AtomicInteger(0);
  public static void main(String[] args) throws InterruptedException {
    configure();
    PropertyConfigurator.configure("log4j.properties");
    latch1 = new CountDownLatch(numThreads / 4 / 10);
    latch2 = new CountDownLatch(numThreads / 10);
    latch3 = new CountDownLatch(numThreads / 4 + numThreads + numThreads / 4);


    Timestamp start = new Timestamp(System.currentTimeMillis());
    phase();
    Timestamp end = new Timestamp(System.currentTimeMillis());
    System.out.println("Number of successful requests: " + successCount);
    System.out.println("Number of unsuccessful requests: " + failCount);
    long time = end.getTime() - start.getTime();
    System.out.println("Wall time: " + (double)time / 1000 + "s");
    System.out.println("Throughput: " + ((successCount.get() + failCount.get()) / ((double)time / 1000)));

  }

  public static void phase() throws InterruptedException {
    ClientThread thread1 = new ClientThread(1, numSkiers / (numThreads / 4), 1, 90,
        liftNum, 100, 5, dayId, resortId, logger, 1, path);
    ClientThread thread2 = new ClientThread(1, numSkiers / numThreads, 91, 360,
        liftNum, 100, 5, dayId, resortId, logger, 2, path);
    ClientThread thread3 = new ClientThread(1, numSkiers / (numThreads / 4), 361, 420,
        liftNum, 100, 10, dayId, resortId, logger, 3, path);
    for (int i = 0; i < numThreads / 4; i++) {
      Thread t = new Thread(thread1);
      t.start();
    }
      latch1.await();

    for (int i = 0; i < numThreads; i++) {
      Thread t = new Thread(thread2);
      t.start();
    }
    latch2.await();
    for (int i = 0; i < numThreads / 4; i++) {
      Thread t = new Thread(thread3);
      t.start();
    }
    latch3.await();

  }

  public static void configure() {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Please input the maximum number of threads to run");
    numThreads = scanner.nextInt();
    System.out.println("Please input the number of skiers to generate lift rides");
    numSkiers = scanner.nextInt();
    System.out.println("Please input the number of ski lifts");
    liftNum = scanner.nextInt();
    System.out.println("Please input the ski day number");
    dayId = scanner.next();
    System.out.println("Please input the resort name which is the resortID");
    resortId = scanner.next();
    System.out.println("Please input the IP address of the server");
    String ip = scanner.next();
    System.out.println("Please input the Port address of the server");
    String port = scanner.next();
    path = ip + ":" + port + "/Lab2_war_exploded/skiers";
  }


}
