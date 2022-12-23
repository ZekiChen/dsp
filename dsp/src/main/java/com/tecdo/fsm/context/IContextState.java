package com.tecdo.fsm.context;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;

public interface IContextState {

  void handleEvent(EventType eventType, Params params, Context context);

}
