package Part1;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
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
  CountDownLatch latch;
  Logger logger;
  int num;
  String path;



  public ClientThread(int startSkierId, int endSkierId, int startTime,
      int endTime, int liftNum, int postRequestNum,
      int getRequestNum, String dayId, String resortId, Logger logger, int num, String path) {
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
        this.logger = logger;
        this.num = num;
        this.path = path;
  }



  @Override
  public void run() {

    try {


      String skierId = "" + (random.nextInt(endSkierId - startSkierId + 1) + startSkierId);
      for (int i = 0; i < postRequestNum; i++) {
        sendPost(skierId);
      }

      for (int i = 0; i < getRequestNum; i++) {
        sendGet(resortId, dayId, skierId);
      }


    }catch (Exception e) {
      e.printStackTrace();
    }
    Main.latch3.countDown();
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



  public  void sendGet(String resortId, String dayId, String skiersId) {
    try {
      SkiersApi skiersApi = createAPI();
      ApiResponse<SkierVertical> response = skiersApi.getSkierDayVerticalWithHttpInfo(resortId, dayId, skiersId);
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

  public  void sendPost(String skierId){
    int liftId = random.nextInt(liftNum) + 1;
    int time = random.nextInt(endSkierId - startSkierId + 1) + startSkierId;
    LiftRide liftRide = new LiftRide();
    liftRide.setTime(String.valueOf(time));
    liftRide.setSkierID(skierId);
    liftRide.setLiftID(String.valueOf(liftId));
    try {
      SkiersApi skiersApi = createAPI();
      ApiResponse<Void> response = skiersApi.writeNewLiftRideWithHttpInfo(liftRide);
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
