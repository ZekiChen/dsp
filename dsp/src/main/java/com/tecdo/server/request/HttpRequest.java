package com.tecdo.server.request;

import com.tecdo.constant.Constant;
import com.tecdo.util.GoogleURIParserAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.CharsetUtil;

public class HttpRequest {

  private static Logger logger = LoggerFactory.getLogger(HttpRequest.class);

  private String uri;
  private String path;
  private String method;
  private String ip;

  // uri query parameter
  private Map<String, String> params = new HashMap<>();
  // request header
  private Map<String, String> headers = new HashMap<>();

  private String body;
  private long requestId;
  private ChannelHandlerContext channelContext;

  public HttpRequest(long requestId,
                     ChannelHandlerContext channelContext,
                     FullHttpRequest httpRequest) {
    this.requestId = requestId;
    this.channelContext = channelContext;
    this.uri = httpRequest.uri();
    this.method = httpRequest.method().name();
    this.path = parsePath(uri);
    this.params = GoogleURIParserAdapter.getInstance().parseURI(uri);
    this.body = parseBody(httpRequest);
    this.ip = getRemoteIP(channelContext, httpRequest);
    HttpHeaders httpHeaders = httpRequest.headers();
    for (String name : httpHeaders.names()) {
      headers.put(name, httpHeaders.getAllAsString(name).get(0));
    }
  }

  private String parsePath(String uri) {
    int index = uri.indexOf(Constant.QUESTION_MARK);
    if (0 < index) {
      uri = uri.substring(0, index);
    }
    return uri;
  }


  /**
   * Parse body
   */
  private String parseBody(FullHttpRequest req) {
    final String POST = "POST";
    if (!req.method().name().equals(POST)) {
      return null;
    }

    //get param content
    String content = null;
    ByteBuf buf = req.content();
    int readable = buf.readableBytes();
    if (0 < readable) {
      byte[] bytes = new byte[readable];
      buf.readBytes(bytes);
      content = new String(bytes, CharsetUtil.UTF_8);
    }

    if (content == null) {
      return null;
    }

    return content;
  }

  public String getBody() {
    return body;
  }


  public int getParamAsInt(String key) {
    String value = getParamAsStr(key);
    if (null == value) {
      return 0;
    }
    if (NumberUtils.isParsable(value)) {
      return NumberUtils.toInt(value);
    } else {
      logger.warn("could not parse param as int:{} !", value);
      return 0;
    }
  }

  public double getParamAsDouble(String key) {
    String value = getParamAsStr(key);
    if (null == value) {
      return 0;
    }
    if (NumberUtils.isParsable(value)) {
      return NumberUtils.toDouble(value);
    } else {
      logger.warn("could not parse param as int:{} !", value);
      return 0;
    }
  }


  public String getParamAsStr(String key) {
    String param = params.get(key);
    return param;
  }


  public String getParamAsStr(String key, String defaultVal) {
    String val = getParamAsStr(key);
    return null == val ? defaultVal : val;
  }

  private String getRemoteIP(ChannelHandlerContext ctx, FullHttpRequest request) {
    final Pattern remotePattern = Pattern.compile("^/[0-9]{1,3}(.[0-9]{1,3}){3}:[0-9]{2,}$");
    // 1. try to get ip address from headers
    String ip = request.headers().get(Constant.HEADER_IP_KEY);
    if (StringUtils.isNotBlank(ip)) {
      String[] ips = ip.split(Constant.COMMA);
      String ipv4 = fetchFirstAvailablePublicIp(ips);
      if (ipv4 != null) {
        return ipv4;
      }
    }

    // 2. if there is no available ipv4 info in header, try to get it from channel info
    ip = ctx.channel().remoteAddress().toString();
    if (StringUtils.isNotBlank(ip) && remotePattern.matcher(ip).matches()) {
      ip = ip.substring(1, ip.indexOf(":"));
    }
    return ip;
  }

  // get the first of available public ipv4 from the array of ip address
  private String fetchFirstAvailablePublicIp(String[] ipAddresses) {
    for (String ipAddress : ipAddresses) {

      InetAddress address;
      try {
        address = InetAddress.getByName(ipAddress.trim());
      } catch (UnknownHostException e) {
        logger.error("parse IP: {} error. ", ipAddress, e);
        continue;
      }

      if (address instanceof Inet4Address && !address.isSiteLocalAddress() &&
          !address.isLoopbackAddress()) {
        return address.getHostAddress();
      }
    }
    return null;
  }


  public String getHeader(String name) {
    return headers.get(name);
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public Map<String, String> getParams() {
    return params;
  }


  public String getUri() {
    return uri;
  }

  public String getPath() {
    return path;
  }

  public String getIp() {
    return ip;
  }

  public ChannelHandlerContext getChannelContext() {
    return channelContext;
  }

  public long getRequestId() {
    return requestId;
  }

  public String getMethod() {
    return method;
  }
}
