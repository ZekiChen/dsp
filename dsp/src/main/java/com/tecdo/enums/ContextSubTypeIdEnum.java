package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ContextSubTypeIdEnum {

  TEN(10, "General or mixed content"),
  TWO(11,
      "Primarily article content (which of course could include images, etc as part of the article)"),
  THREE(12, "Primarily video content"),
  THIRTEEN(13, "Primarily audio content"),
  FOURTEEN(14, "Primarily image content"),
  FIFTEEN(15, "User-generated content - forums, comments, etc"),
  TWENTY(20, "General social content such as a general social network"),
  TWENTY_ONE(21, "Primarily email content"),
  TWENTY_TWO(22, "Primarily chat/IM content"),
  THIRTY(30, "Content focused on selling products, whether digital or physical"),
  THIRTY_ONE(31, "Application store/marketplace"),
  THIRTY_TWO(32, "Product reviews site primarily (which may sell product secondarily)");

  /**
   * 值
   */
  private final Integer value;
  /**
   * 描述
   */
  private final String desc;
}
