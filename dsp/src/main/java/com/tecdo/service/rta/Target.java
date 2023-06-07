package com.tecdo.service.rta;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Target {

  private int advType;
  private boolean target;
  private String token;  // Lazada
  private String landingPage;  // AE

}