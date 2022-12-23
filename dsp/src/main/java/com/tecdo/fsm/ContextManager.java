package com.tecdo.fsm;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.fsm.context.Context;
import com.tecdo.server.request.HttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ContextManager {

  private Logger logger = LoggerFactory.getLogger(ContextManager.class);

  private ContextManager() {
  }

  private static ContextManager instance = new ContextManager();

  public static ContextManager getInstance() {
    return instance;
  }

  private final int maxPoolSize = 1000;
  private Map<Long, Context> contextMap = new HashMap<>();
  private Queue<Context> contextPool = new LinkedList<>();

  public void handleEvent(EventType eventType, Params params) {

  }

  private void handleBidRequestComplete(Params params) {
    releaseContext(params.get(ParamKey.REQUEST_ID));
  }

  private void dispatchToContext(EventType eventType, Params params) {
    long requestId = params.get(ParamKey.REQUEST_ID);
    Context context = getContext(requestId);
    if (context == null) {
      logger.warn("requestId:{},can't get a context to handel event:{}", requestId, eventType);
      return;
    }
    context.handleEvent(eventType, params);
  }

  private Context getContext(long requestId) {
    return contextMap.get(requestId);
  }

  private Context retrieveContext(HttpRequest request) {
    long requestId = request.getRequestId();
    Context context = contextPool.poll();
    if (context == null) {
      context = new Context();
    }
    context.init(request);
    contextMap.put(requestId, context);
    return context;
  }

  private void releaseContext(long requestId) {
    Context context = contextMap.remove(requestId);
    if (context != null) {
      context.reset();
      if (contextPool.size() < maxPoolSize) {
        contextPool.offer(context);
      }
    } else {
      logger.error("release context error,requestId:{}", requestId);
    }
  }
}
