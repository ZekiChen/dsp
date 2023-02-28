package com.tecdo.job.controller;


import com.tecdo.common.util.Params;
import com.tecdo.job.constant.EventType;

public interface MessageObserver {

  void handle(EventType eventType, Params param);
}
