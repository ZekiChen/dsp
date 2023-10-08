package com.tecdo.fsm.context.state;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.fsm.context.Context;

public interface IContextState {

  void handleEvent(EventType eventType, Params params, Context context);

}
