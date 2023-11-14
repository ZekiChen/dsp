package com.tecdo.service.init.doris;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.adm.api.doris.entity.GooglePlayApp;
import com.tecdo.adm.api.doris.mapper.GooglePlayAppMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class GooglePlayAppManager extends ServiceImpl<GooglePlayAppMapper, GooglePlayApp> {

  @Autowired
  private SoftTimer softTimer;
  @Autowired
  private MessageQueue messageQueue;
  @Autowired
  private ThreadPool threadPool;

  private State currentState = State.INIT;
  private long timerId;

  private Map<String, GooglePlayApp> googlePlayAppMap;
  private Map<String, List<String>> categoryBundleMap;
  private Map<String, List<String>> tagBundleMap;

  @Value("${pac.timeout.load.doris.google-play-app}")
  private long loadTimeout;
  @Value("${pac.interval.reload.db.default}")
  private long reloadInterval;

  @Value("${pac.init.reload.interval:60000}")
  private long initReloadInterval;

  private static GooglePlayApp EMPTY = new GooglePlayApp();

  public GooglePlayApp getGoogleApp(String bundleId) {
    return googlePlayAppMap.get(bundleId);
  }

  public GooglePlayApp getGoogleAppOrEmpty(String bundleId) {
    return googlePlayAppMap.getOrDefault(bundleId, EMPTY);
  }

  public List<String> listByCategory(String category) {
    return categoryBundleMap.get(category);
  }

  public List<String> listByTag(String tag) {
    return tagBundleMap.get(tag);
  }

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
    messageQueue.putMessage(EventType.GP_APP_LOAD, params);
  }

  private void startReloadTimeoutTimer(Params params) {
    timerId = softTimer.startTimer(EventType.GP_APP_LOAD_TIMEOUT, params, loadTimeout);
  }

  private void cancelReloadTimeoutTimer() {
    softTimer.cancel(timerId);
  }

  private void startNextReloadTimer(Params params) {
    softTimer.startTimer(EventType.GP_APP_LOAD, params, reloadInterval);
  }

  private void startInitReloadTimer(Params params) {
    softTimer.startTimer(EventType.GP_APP_LOAD, params, initReloadInterval);
  }

  public void switchState(State state) {
    this.currentState = state;
  }

  public void handleEvent(EventType eventType, Params params) {
    switch (eventType) {
      case GP_APP_LOAD:
        handleReload(params);
        break;
      case GP_APP_LOAD_RESPONSE:
        handleLoadResponse(params);
        break;
      case GP_APP_LOAD_ERROR:
        handleLoadError(params);
        break;
      case GP_APP_LOAD_TIMEOUT:
        handleLoadTimeout(params);
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
            long startTime = System.currentTimeMillis();
            LambdaQueryWrapper<GooglePlayApp> wrapper =
                    Wrappers.<GooglePlayApp>lambdaQuery().eq(GooglePlayApp::isFound, 1);
            List<GooglePlayApp> list = list(wrapper);
            Map<String, GooglePlayApp> appMap =
                    list.stream().collect(Collectors.toMap(GooglePlayApp::getBundleId, i -> {
                      if (StringUtils.isNotEmpty(i.getCategorys())) {
                        i.setCategoryList(Arrays.asList(i.getCategorys().split(",")));
                      }
                      if (StringUtils.isNotEmpty(i.getTags())) {
                        i.setTagList(Arrays.asList(i.getTags().split(",")));
                      }
                      return i;
                    }, (o, n) -> n));

            List<GooglePlayApp> appsByCategories = list.stream()
                    .filter(e -> StrUtil.isNotBlank(e.getCategorys()))
                    .collect(Collectors.toList());
            List<GooglePlayApp> appsByTags = list.stream()
                    .filter(e -> StrUtil.isNotBlank(e.getTags()))
                    .collect(Collectors.toList());
            Map<String, List<String>> categoryBundleMap = appsByCategories.stream()
                    .flatMap(e -> Stream.of(e.getCategorys().split(StrUtil.COMMA)))
                    .distinct()
                    .collect(Collectors.toMap(k -> k, v -> new ArrayList<>()));
            categoryBundleMap.forEach((category, bundles) -> appsByCategories.forEach(e -> {
                if (Arrays.asList(e.getCategorys().split(StrUtil.COMMA)).contains(category)) {
                    bundles.add(e.getBundleId());
                }
            }));

            Map<String, List<String>> tagBundleMap = appsByTags.stream()
                    .flatMap(e -> Stream.of(e.getTags().split(StrUtil.COMMA)))
                    .distinct()
                    .collect(Collectors.toMap(k -> k, v -> new ArrayList<>()));
            tagBundleMap.forEach((tag, bundles) -> appsByTags.forEach(e -> {
                if (Arrays.asList(e.getTags().split(StrUtil.COMMA)).contains(tag)) {
                    bundles.add(e.getBundleId());
                }
            }));

            log.info("gp app load time: {}s", (System.currentTimeMillis() - startTime) / 1000);
            params.put(ParamKey.GP_APP_CATEGORY_CACHE_KEY, categoryBundleMap);
            params.put(ParamKey.GP_APP_TAG_CACHE_KEY, tagBundleMap);
            params.put(ParamKey.GP_APP_CACHE_KEY, appMap);
            messageQueue.putMessage(EventType.GP_APP_LOAD_RESPONSE, params);
          } catch (Exception e) {
            log.error("gp app load failure from db", e);
            messageQueue.putMessage(EventType.GP_APP_LOAD_ERROR, params);
          }
        });
        startReloadTimeoutTimer(params);
        switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private void handleLoadResponse(Params params) {
    cancelReloadTimeoutTimer();
    this.categoryBundleMap = params.get(ParamKey.GP_APP_CATEGORY_CACHE_KEY);
    this.tagBundleMap = params.get(ParamKey.GP_APP_TAG_CACHE_KEY);
    this.googlePlayAppMap = params.get(ParamKey.GP_APP_CACHE_KEY);
    switch (currentState) {
      case WAIT_INIT_RESPONSE:
        log.info("gp app load success, size: {}", googlePlayAppMap.size());
        messageQueue.putMessage(EventType.ONE_DATA_READY);
        startNextReloadTimer(params);
        switchState(State.RUNNING);
        break;
      case UPDATING:
        startNextReloadTimer(params);
        switchState(State.RUNNING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private void handleLoadError(Params params) {
    switch (currentState) {
      case WAIT_INIT_RESPONSE:
        cancelReloadTimeoutTimer();
        startInitReloadTimer(params);
        switchState(State.INIT);
        break;
      case UPDATING:
        cancelReloadTimeoutTimer();
        startNextReloadTimer(params);
        switchState(State.RUNNING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private void handleLoadTimeout(Params params) {
    switch (currentState) {
      case WAIT_INIT_RESPONSE:
        log.error("timeout load gp app info");
        startInitReloadTimer(params);
        switchState(State.INIT);
        break;
      case UPDATING:
        log.error("timeout load gp app info");
        startNextReloadTimer(params);
        switchState(State.RUNNING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }
}