package com.tecdo.util;

import com.google.api.client.http.UrlEncodedParser;
import com.tecdo.constant.Constant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class GoogleURIParserAdapter {

  private GoogleURIParserAdapter() {
  }

  private static GoogleURIParserAdapter instance = new GoogleURIParserAdapter();

  public static GoogleURIParserAdapter getInstance() {
    return instance;
  }

  private static Logger logger = LoggerFactory.getLogger(GoogleURIParserAdapter.class);

  public Map<String, String> parseURI(String uri) {
    //new key value pair param object
    Map<String, List<String>> params = new HashMap<>();
    Map<String, String> map = new HashMap<>();

    //get the content behind symbol "?" and parse them to key value pair
    int index = uri.indexOf(Constant.QUESTION_MARK);
    if (0 < index) {
      String content = uri.substring(index + 1);
      try {
        //Parses the given URL-encoded content into the given data object of data key name/value pairs
        UrlEncodedParser.parse(content, params);
        map = params.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
          List<String> values = e.getValue();
          // trim space
          return values.get(0).trim();
        }));

      } catch (Exception e) {
        logger.error(String.format("could not parse content=%s !", content), e);
      }
    }
    return map;
  }
}
