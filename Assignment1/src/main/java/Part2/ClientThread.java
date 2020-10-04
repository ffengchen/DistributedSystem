package Part2;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.Logger;


public class ClientThread implements Runnable{



  int startSkierId;
  int endSkierId;
  int startTime;
  int endTime;
  int liftNum;
  Random random;
  int postRequestNum;
  int getRequestNum;
  String dayId;
  String resortId;
  int num;
  Logger logger;
  List<Record> records;
  String path;

  public ClientThread(int startSkierId, int endSkierId, int startTime,
      int endTime, int liftNum, int postRequestNum,
      int getRequestNum, String dayId, String resortId, int num, Logger logger, List<Record> records, String path) {
        this.startSkierId = startSkierId;
        this.endSkierId = endSkierId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.liftNum = liftNum;
        random = new Random();
        this.postRequestNum = postRequestNum;
        this.getRequestNum = getRequestNum;
        this.dayId = dayId;
        this.resortId = resortId;
        this.num = num;
        this.logger = logger;
        this.records = records;
        this.path = path;
  }



  @Override
  public void run() {
    String skierId = "" + (random.nextInt(endSkierId - startSkierId + 1) + startSkierId);
    for (int i = 0; i < postRequestNum; i++) {
      sendPost(skierId);
    }
    for (int i = 0; i < getRequestNum; i++) {
      sendGet(resortId, dayId, skierId);
    }
    Main.totalLatch.countDown();
    if (num == 1) {
      Main.latch1.countDown();
    } else if (num == 2) {
      Main.latch2.countDown();
    }
  }


  public SkiersApi createAPI() {
    SkiersApi s = new SkiersApi();
    ApiClient client = s.getApiClient();
    client.setBasePath(path);
    return s;
  }








  public void sendGet(String resortId, String dayId, String skiersId) {
    try {
      SkiersApi skiersApi = createAPI();

      Timestamp start = new Timestamp(System.currentTimeMillis());
      ApiResponse<SkierVertical> response = skiersApi.getSkierDayVerticalWithHttpInfo(resortId, dayId, skiersId);
      Timestamp end = new Timestamp(System.currentTimeMillis());

      long latency = end.getTime() - start.getTime();
      Record record = new Record(start.getTime(), "Get", latency, response.getStatusCode());
      records.add(record);
      if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
        Main.successCount.getAndIncrement();
      } else {
        Main.failCount.getAndIncrement();
        logger.error("4XX or 5XX received");
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      Main.failCount.getAndIncrement();
    }
  }

  public void sendPost(String skierId){

    SkiersApi skiersApi = createAPI();

    int liftId = random.nextInt(liftNum) + 1;
    int time = random.nextInt(endSkierId - startSkierId + 1) + startSkierId;
    LiftRide liftRide = new LiftRide();
    liftRide.setTime(String.valueOf(time));
    liftRide.setSkierID(skierId);
    liftRide.setLiftID(String.valueOf(liftId));
    try {
      Timestamp start = new Timestamp(System.currentTimeMillis());
      ApiResponse<Void> response = skiersApi.writeNewLiftRideWithHttpInfo(liftRide);
      Timestamp end = new Timestamp(System.currentTimeMillis());
      long latency = end.getTime() - start.getTime();
      Record record = new Record(start.getTime(), "Post", latency, response.getStatusCode());
      records.add(record);
      if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {

        Main.successCount.getAndIncrement();

      } else {
        Main.failCount.getAndIncrement();
        logger.error("4XX or 5XX received");
      }

    } catch (Exception e) {
      logger.error(e.getMessage());
      Main.failCount.getAndIncrement();

    }






  }


}
