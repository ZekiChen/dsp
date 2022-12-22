package com.tecdo.controller;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;


public interface MessageObserver {

  void handle(EventType eventType, Params param);
}
