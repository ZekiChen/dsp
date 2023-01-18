package com.tecdo.fsm;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.entity.Affiliate;
import com.tecdo.fsm.context.Context;
import com.tecdo.server.request.HttpRequest;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ContextManager {

  private final int maxPoolSize = 1000;
  private final Map<Long, Context> contextMap = new HashMap<>();
  private final Queue<Context> contextPool = new LinkedList<>();

  public void handleEvent(EventType eventType, Params params) {
    switch (eventType) {
      case RECEIVE_BID_REQUEST:
        Context context = retrieveContext(params.get(ParamKey.HTTP_REQUEST),
                                          params.get(ParamKey.BID_REQUEST),
                                          params.get(ParamKey.AFFILIATE));
        context.handleEvent(eventType, params);
        break;
      case BID_REQUEST_COMPLETE:
        handleBidRequestComplete(params);
        break;
      default:
        dispatchToContext(eventType, params);
    }
  }

  private void handleBidRequestComplete(Params params) {
    releaseContext(params.get(ParamKey.REQUEST_ID));
  }

  private void dispatchToContext(EventType eventType, Params params) {
    long requestId = params.get(ParamKey.REQUEST_ID);
    Context context = getContext(requestId);
    if (context == null) {
      log.warn("requestId:{},can't get a context to handel event:{}", requestId, eventType);
      return;
    }
    context.handleEvent(eventType, params);
  }

  private Context getContext(long requestId) {
    return contextMap.get(requestId);
  }

  private Context retrieveContext(HttpRequest httpRequest,
                                  BidRequest bidRequest,
                                  Affiliate affiliate) {
    long requestId = httpRequest.getRequestId();
    Context context = contextPool.poll();
    if (context == null) {
      context = new Context();
    }
    context.init(httpRequest, bidRequest, affiliate);
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
      log.error("release context error,requestId:{}", requestId);
    }
  }
}
