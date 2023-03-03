package com.tecdo.controller;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;

public interface MessageObserver {

  void handle(EventType eventType, Params param);
}
