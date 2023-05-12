package com.tecdo.service.init;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Strings;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.entity.IpTable;
import com.tecdo.mapper.IpTableMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IpTableManager extends ServiceImpl<IpTableMapper, IpTable> {

  private final SoftTimer softTimer;
  private final MessageQueue messageQueue;
  private final ThreadPool threadPool;

  private State currentState = State.INIT;
  private long timerId;

  private Map<String, List<IpItem>> ipItemMap;

  @Value("${pac.timeout.load.db.default}")
  private long loadTimeout;
  @Value("${pac.interval.reload.db.default}")
  private long reloadInterval;

  @AllArgsConstructor
  private enum State {
    INIT(1, "init"),
    WAIT_INIT_RESPONSE(2, "waiting init response"),
    RUNNING(3, "init success, now is running"),
    UPDATING(4, "updating");

    private int code;
    private String desc;

    @Override
    public String toString() {
      return code + " - " + desc;
    }
  }

  public void init(Params params) {
    messageQueue.putMessage(EventType.IP_TABLE_LOAD, params);
  }

  private void startReloadTimeoutTimer(Params params) {
    timerId = softTimer.startTimer(EventType.IP_TABLE_LOAD_TIMEOUT, params, loadTimeout);
  }

  private void cancelReloadTimeoutTimer() {
    softTimer.cancel(timerId);
  }

  private void startNextReloadTimer(Params params) {
    softTimer.startTimer(EventType.IP_TABLE_LOAD, params, reloadInterval);
  }

  public void switchState(State state) {
    this.currentState = state;
  }

  public void handleEvent(EventType eventType, Params params) {
    switch (eventType) {
      case IP_TABLE_LOAD:
        handleReload(params);
        break;
      case IP_TABLE_LOAD_RESPONSE:
        handleResponse(params);
        break;
      case IP_TABLE_LOAD_ERROR:
        handleError(params);
        break;
      case IP_TABLE_LOAD_TIMEOUT:
        handleTimeout(params);
        break;
      default:
        log.error("Can't handle event, type: {}", eventType);
    }
  }

  private void handleReload(Params params) {
    switch (currentState) {
      case INIT:
      case RUNNING:
        threadPool.execute(() -> {
          try {
            Map<String, List<IpTable>> ipTableList =
              list().stream().collect(Collectors.groupingBy(IpTable::getType));
            Map<String, List<IpItem>> ipItemMap = convertAndMerge(ipTableList);
            params.put(ParamKey.IP_TABLE_CACHE_KEY, ipItemMap);
            messageQueue.putMessage(EventType.IP_TABLE_LOAD_RESPONSE, params);
          } catch (Exception e) {
            log.error("ip table load failure from db", e);
            messageQueue.putMessage(EventType.IP_TABLE_LOAD_ERROR, params);
          }
        });
        startReloadTimeoutTimer(params);
        switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private void handleResponse(Params params) {
    switch (currentState) {
      case WAIT_INIT_RESPONSE:
        messageQueue.putMessage(EventType.ONE_DATA_READY);
      case UPDATING:
        cancelReloadTimeoutTimer();
        this.ipItemMap = params.get(ParamKey.IP_TABLE_CACHE_KEY);
        log.info("ip table load success");
        startNextReloadTimer(params);
        switchState(State.RUNNING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private void handleError(Params params) {
    switch (currentState) {
      case WAIT_INIT_RESPONSE:
      case UPDATING:
        cancelReloadTimeoutTimer();
        startNextReloadTimer(params);
        switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private void handleTimeout(Params params) {
    switch (currentState) {
      case WAIT_INIT_RESPONSE:
      case UPDATING:
        startNextReloadTimer(params);
        switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  public Pair<Boolean, String> ipCheck(String ip) {
    long ipNum = transformIpToNumber(ip);
    for (Map.Entry<String, List<IpItem>> entry : ipItemMap.entrySet()) {
      String type = entry.getKey();
      List<IpItem> list = entry.getValue();
      int low = 0;
      int high = list.size() - 1;
      int middle;
      while (low <= high) {
        middle = (low + high) / 2;
        if (list.get(middle).startIp <= ipNum) {
          if (list.get(middle).endIp >= ipNum) {
            return Pair.of(true, type);
          } else {
            low = middle + 1;
          }
        } else {
          high = middle - 1;
        }
      }
    }
    return Pair.of(false, "");
  }

  private Map<String, List<IpItem>> convertAndMerge(Map<String, List<IpTable>> ipTableList) {
    Map<String, List<IpItem>> listMap = new HashMap<>();
    ipTableList.forEach((type, list) -> {
      List<IpItem> ipItemList = list.stream().map(this::convert).collect(Collectors.toList());
      List<IpItem> merge = merge(ipItemList);
      listMap.put(type, merge);
    });
    return listMap;
  }

  private IpItem convert(IpTable ipTable) {
    return IpItem.of(transformIpToNumber(ipTable.getStartIp()),
                     transformIpToNumber(ipTable.getEndIp()),
                     ipTable.getType());
  }

  /**
   * 区间合并
   */
  private List<IpItem> merge(List<IpItem> ipItemList) {
    if (ipItemList.size() == 0) {
      return ipItemList;
    }
    ipItemList.sort((x, y) -> (int) (x.startIp - y.startIp));

    List<IpItem> merged = new ArrayList<>();
    for (IpItem ipItem : ipItemList) {
      long l = ipItem.startIp, r = ipItem.endIp;
      IpItem topRight;
      if (merged.size() == 0 || (topRight = merged.get(merged.size() - 1)).endIp < l - 1) {
        merged.add(IpItem.of(l, r, ipItem.type));
      } else {
        topRight.endIp = Math.max(topRight.endIp, r);
      }
    }
    return merged;
  }


  /**
   * ip转long
   * ip: a.b.c.d -> a * 2^24 + b * 2^16 + c * 2^8 + d
   */
  public static long transformIpToNumber(String ip) {
    long ret = 0L;
    String[] numbers = null;
    if (!Strings.isNullOrEmpty(ip) && 4 == (numbers = ip.split("\\.")).length) {
      for (int i = 0; i <= 3; i++) {
        long n = Long.parseLong(numbers[i]);
        ret += n << (3 - i) * 8;
      }
    }
    return ret;
  }
}
