package Part2;

import io.swagger.client.ApiClient;
import io.swagger.client.api.SkiersApi;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Main {

  static int numThreads = 256;
  static int numSkiers = 50000;
  static int liftNum = 40;
  static String dayId = "1";
  static String resortId = "SliverMt";
  //static String path = "http://localhost:8080/skiers/";
  //static String path = "http://ec2-54-193-221-11.us-west-1.compute.amazonaws.com:8080/Lab2_war_exploded/skiers";
  static String path;
  static CountDownLatch latch1;
  static CountDownLatch latch2;
  static CountDownLatch totalLatch;
  private static Logger logger = Logger.getLogger(Main.class);
  static AtomicInteger successCount = new AtomicInteger(0);
  static AtomicInteger failCount = new AtomicInteger(0);
  static List<Record> records = Collections.synchronizedList(new ArrayList<Record>());
  static long time;



  public static void main(String[] args) throws InterruptedException {
    configure();
    PropertyConfigurator.configure("log4j.properties");
    latch1 = new CountDownLatch(numThreads / 4 / 10);
    latch2 = new CountDownLatch(numThreads / 10);
    totalLatch = new CountDownLatch(numThreads / 4 + numThreads + numThreads / 4);
    Timestamp start = new Timestamp(System.currentTimeMillis());

    phase1();
    latch1.await();
    phase2();
    latch2.await();
    phase3();
    totalLatch.await();

    Timestamp end = new Timestamp(System.currentTimeMillis());

    System.out.println("Number of successful requests: " + successCount);
    System.out.println("Number of unsuccessful requests: " + failCount);
    time = end.getTime() - start.getTime();
//    System.out.println("Wall time: " + (double)time / 1000 + "s");
//    System.out.println("Throughput: " + ((successCount.get() + failCount.get()) / ((double)time / 1000)));
    writeCSV();
    calculate();
  }

  public static void phase1(){
    for (int i = 0; i < numThreads / 4; i++) {
      ClientThread thread = new ClientThread(1, numSkiers / (numThreads / 4), 1, 90,
          liftNum, 100, 5, dayId, resortId, 1, logger, records, path);
      Thread t = new Thread(thread);
      t.start();
    }
  }

  public static void phase2(){
    for (int i = 0; i < numThreads; i++) {
      ClientThread thread = new ClientThread(1, numSkiers / numThreads, 91, 360,
          liftNum, 100, 5, dayId, resortId, 2, logger, records, path);
      Thread t = new Thread(thread);
      t.start();
    }
  }

  public static void phase3(){
    for (int i = 0; i < numThreads / 4; i++) {
      ClientThread thread = new ClientThread(1, numSkiers / (numThreads / 4), 361, 420,
          liftNum, 100, 10, dayId, resortId, 3, logger, records, path);
      Thread t = new Thread(thread);
      t.start();
    }
  }

  public static void writeCSV() {
    try {
      FileWriter csvWriter = new FileWriter("records" + numThreads + ".csv");
      csvWriter.append("Start time,Request type,Latency,Response code");
      csvWriter.append("\n");

    for (Record record : records) {
      csvWriter.append(record.toString());
      csvWriter.append("\n");
    }
      csvWriter.flush();
      csvWriter.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public static void calculate() {
    Collections.sort(records, new Comparator<Record>() {
      @Override
      public int compare(Record a, Record b) {
        return (int) (a.getLatency() - b.getLatency());
      }
    });
    long totalWallTime = 0;
    long maxResponseTime = 0;
    int size = records.size();
    if (size == 0) {
      System.out.println("No Records");
      return;
    }
    long median = size % 2 == 0 ? (records.get(size / 2).getLatency() + records.get(size / 2 - 1).getLatency()) / 2 : records.get(size / 2).getLatency();
    for (Record record : records) {
      totalWallTime += record.getLatency();
      maxResponseTime = Math.max(maxResponseTime, record.getLatency());
    }

    double mean = (double)totalWallTime / size;
    long p99 = records.get((int)(size * 0.99) - 1).getLatency();
    System.out.println("Mean response time: " + mean + "ms");
    System.out.println("Median response time: " + median + "ms");
    System.out.println("Total wall time:" + (double)time / 1000 + "s");
    System.out.println("Throughput: " + size / ((double)time / 1000 ));
    System.out.println("P99 response time: " + p99 + "ms");
    System.out.println("Max response time: " + maxResponseTime + "ms");



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
