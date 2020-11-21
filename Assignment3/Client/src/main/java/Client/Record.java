package Client;

import java.util.Date;

public class Record {
private String start;
private String requestType;
private long latency;
private int responseCode;

  public Record(long startTime, String requestType, long latency, int responseCode) {
    start = new Date(startTime).toString();
    this.requestType = requestType;
    this.latency = latency;
    this.responseCode = responseCode;

  }



  public long getLatency() {
    return latency;
  }

  @Override
  public String toString() {
    return start + "," + requestType + "," + latency + "," + responseCode;
  }
}
