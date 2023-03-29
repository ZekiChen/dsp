package com.tecdo.service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ValidateCode {

   SUCCESS(0),
   BLANK_VALID_FAILED(1),
   BID_ID_VALID_FAILED(2),
   WINDOW_VALID_FAILED(3),
   FUNNEL_VALID_FAILED(4),
   DUPLICATE_VALID_FAILED(5),
  ;

   private final int code;
}
