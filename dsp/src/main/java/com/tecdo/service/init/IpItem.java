package com.tecdo.service.init;

public class IpItem {

  private IpItem(long startIp, long endIp, String type) {
    this.startIp = startIp;
    this.endIp = endIp;
    this.type = type;
  }

  public long startIp;

  public long endIp;

  public String type;

  public static IpItem of(long startIp, long endIp, String type) {
    return new IpItem(startIp, endIp, type);
  }

}
