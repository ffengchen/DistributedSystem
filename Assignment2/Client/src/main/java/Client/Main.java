package Client;

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
  static int numThreads = 512;
  static int numSkiers = 20000;
  static int liftNum = 40;
  static String dayId = "1";
  static String resortId = "SliverMt";
  //static String path = "http://localhost:8080/Server_war_exploded/skiers";
  //static String path = "http://ec2-34-219-42-134.us-west-2.compute.amazonaws.com:8080/Server_war/skiers";
  //static String path = "http://loadbalancer-362151779.us-west-2.elb.amazonaws.com:8080/Server_war/skiers";
  static String path;
  static CountDownLatch latch1;
  static CountDownLatch latch2;
  static CountDownLatch latch3;
  private static Logger logger = Logger.getLogger(Main.class);
  static AtomicInteger successCount =  new AtomicInteger(0);
  static AtomicInteger failCount = new AtomicInteger(0);
  static List<Record> records = Collections.synchronizedList(new ArrayList<Record>());
  static long time;

  public static void main(String[] args) throws InterruptedException {
    configure();
    PropertyConfigurator.configure("log4j.properties");
    latch1 = new CountDownLatch(numThreads / 4 / 10);
    latch2 = new CountDownLatch(numThreads / 10);
    latch3 = new CountDownLatch(numThreads / 4 + numThreads + numThreads / 4);


    Timestamp start = new Timestamp(System.currentTimeMillis());
    phase();
    Timestamp end = new Timestamp(System.currentTimeMillis());

    time = end.getTime() - start.getTime();
    writeCSV();
    calculate();
//    System.out.println("Wall time: " + (double)time / 1000 + "s");
//    System.out.println("Throughput: " + ((successCount.get() + failCount.get()) / ((double)time / 1000)));

  }

  public static void phase() throws InterruptedException {
    ClientThread thread1 = new ClientThread(1, numSkiers / (numThreads / 4), 1, 90,
        liftNum, 1000, 5, dayId, resortId, logger, 1, path, records);
    ClientThread thread2 = new ClientThread(1, numSkiers / numThreads, 91, 360,
        liftNum, 1000, 5, dayId, resortId, logger, 2, path, records);
    ClientThread thread3 = new ClientThread(1, numSkiers / (numThreads / 4), 361, 420,
        liftNum, 1000, 10, dayId, resortId, logger, 3, path, records);
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
    path = ip + ":" + port + "/Server_war/skiers";
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
    System.out.println("Number of successful requests: " + successCount);
    System.out.println("Number of unsuccessful requests: " + failCount);
    System.out.println("Mean response time: " + mean + "ms");
    System.out.println("Median response time: " + median + "ms");
    System.out.println("Total wall time:" + (double)time / 1000 + "s");
    System.out.println("Throughput: " + size / ((double)time / 1000 ));
    System.out.println("P99 response time: " + p99 + "ms");
    System.out.println("Max response time: " + maxResponseTime + "ms");



  }
  public static void writeCSV() {
    try {
      FileWriter csvWriter = new FileWriter("records_loadbalanceradded_" + numThreads + ".csv");
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


}
