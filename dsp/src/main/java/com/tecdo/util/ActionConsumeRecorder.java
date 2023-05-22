package com.tecdo.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A recorder that use to record any key actions and can output the elapsed time between the actions.
 */
public class ActionConsumeRecorder {
  public ActionConsumeRecorder() {
    mRecordMap = new LinkedHashMap<>();
    mResultMap = new LinkedHashMap<>();
  }

  private final Map<String, Long> mRecordMap;
  private final Map<String, Double> mResultMap;

  public void reset() {
    mRecordMap.clear();
    mResultMap.clear();
  }

  public void tick(String action) {
    // ). record action start timestamp
    mRecordMap.put(action, System.nanoTime());
  }

  public void stop() {
    // ). get final timestamp
    final long finalTimestamp = System.nanoTime();

    // ). calculate interval except the last one
    Map.Entry<String, Long> temp = null;
    for (Map.Entry<String, Long> entry : mRecordMap.entrySet()) {
      if (temp == null) {
        temp = entry;
      } else {
        final double interval = (entry.getValue() - temp.getValue()) / 1000_000.0d;
        mResultMap.put(temp.getKey(), interval);
        temp = entry;
      }
    }

    // ). calculate the last interval
    if (temp != null) {
      final double interval = (finalTimestamp - temp.getValue()) / 1000_000.0d;
      mResultMap.put(temp.getKey(), interval);
    }

    // clear record map
    mRecordMap.clear();
  }

  public Map<String, Double> consumes() {
    return mResultMap;
  }

  public double total() {
    return consumes().values().stream().reduce(0.0, Double::sum);
  }
}
