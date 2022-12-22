package com.tecdo.controller;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;

public class Controller implements MessageObserver {

  private static Controller instance = new Controller();

  private Controller() {
  }

  public static Controller getInstance() {
    return instance;
  }

  @Override
  public void handle(EventType eventType, Params param) {

  }
}
