package com.tecdo.log;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.google.common.net.HttpHeaders;
import com.tecdo.constant.RequestKey;
import com.tecdo.constant.RequestPath;
import com.tecdo.domain.biz.notice.ImpInfoNoticeInfo;
import com.tecdo.domain.biz.notice.NoticeInfo;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.ValidateCode;
import com.tecdo.util.JsonHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Zeki on 2023/5/5
 */
public class NoticeLogger {

    private static final Logger winLog = LoggerFactory.getLogger("win_log");
    private static final Logger lossLog = LoggerFactory.getLogger("loss_log");
    private static final Logger impLog = LoggerFactory.getLogger("imp_log");
    private static final Logger clickLog = LoggerFactory.getLogger("click_log");
    private static final Logger pbLog = LoggerFactory.getLogger("pb_log");
    private static final Logger validateLog = LoggerFactory.getLogger("validate_notice_log");
    private static final Logger impInfoLog = LoggerFactory.getLogger("imp_info_log");

    public static void logWin(HttpRequest httpRequest, NoticeInfo info) {
        String bidSuccessPrice = info.getBidSuccessPrice();
        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("bid_id", info.getBidId());

        map.put("campaign_id", info.getCampaignId());
        map.put("ad_group_id", info.getAdGroupId());
        map.put("ad_id", info.getAdId());
        map.put("creative_id", info.getCreativeId());
        map.put("affiliate_id", info.getAffiliateId());

        map.put("bid_success_price", NumberUtils.isParsable(bidSuccessPrice) ?
                new BigDecimal(bidSuccessPrice).doubleValue() : 0d);

        winLog.info(JsonHelper.toJSONString(map));
    }

    public static void logLoss(HttpRequest httpRequest, NoticeInfo info) {

        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("bid_id", info.getBidId());

        map.put("campaign_id", info.getCampaignId());
        map.put("ad_group_id", info.getAdGroupId());
        map.put("ad_id", info.getAdId());
        map.put("creative_id", info.getCreativeId());
        map.put("affiliate_id", info.getAffiliateId());

        map.put("loss_code", info.getLossCode());

        lossLog.info(JsonHelper.toJSONString(map));
    }

    public static void logImp(HttpRequest httpRequest, NoticeInfo info) {
        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("bid_id", info.getBidId());

        map.put("campaign_id", info.getCampaignId());
        map.put("ad_group_id", info.getAdGroupId());
        map.put("ad_id", info.getAdId());
        map.put("creative_id", info.getCreativeId());
        map.put("affiliate_id", info.getAffiliateId());

        map.put("referer", httpRequest.getHeader(HttpHeaders.REFERER));
        map.put("ua_from_imp", httpRequest.getHeader(HttpHeaders.USER_AGENT));
        map.put("ip_from_imp", httpRequest.getIp());
        map.put("device_id", info.getDeviceId());
        String bidSuccessPrice = info.getBidSuccessPrice();
        map.put("bid_success_price", NumberUtils.isParsable(bidSuccessPrice) ?
                new BigDecimal(bidSuccessPrice).doubleValue() : 0d);

        impLog.info(JsonHelper.toJSONString(map));
    }

    public static void logClick(HttpRequest httpRequest, NoticeInfo info) {
        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("bid_id", info.getBidId());
        map.put("campaign_id", info.getCampaignId());
        map.put("ad_group_id", info.getAdGroupId());
        map.put("ad_id", info.getAdId());
        map.put("creative_id", info.getCreativeId());
        map.put("affiliate_id", info.getAffiliateId());

        map.put("referer", httpRequest.getHeader(HttpHeaders.REFERER));
        map.put("ua_from_click", httpRequest.getHeader(HttpHeaders.USER_AGENT));
        map.put("ip_from_click", httpRequest.getIp());
        map.put("device_id", info.getDeviceId());

        clickLog.info(JsonHelper.toJSONString(map));
    }

    public static void logPb(HttpRequest httpRequest, NoticeInfo info) {
        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("bid_id", info.getBidId());

        map.put("campaign_id", info.getCampaignId());
        map.put("ad_group_id", info.getAdGroupId());
        map.put("ad_id", info.getAdId());
        map.put("creative_id", info.getCreativeId());

        if (info.getEventType() != null) {
            map.put(info.getEventType(), 1);
        }

        if (RequestPath.PB_AE.equals(httpRequest.getPath())) {
            map.put("is_realtime", info.getIsRealtime());
            map.put("buyer_cnt", info.getBuyerCnt());
            map.put("order_amount", info.getOrderAmount());
            map.put("p4p_revenue", info.getP4pRevenue());
            map.put("affi_revenue", info.getAffiRevenue());
            map.put("add_to_wish_cnt", info.getAddToWishCnt());
            map.put("add_to_cart_cnt", info.getAddToCartCnt());

            List<Integer> contentViewList = info.getSessionContentViewList();
            List<Integer> addToCartList = info.getSessionAddToCartList();
            List<Integer> orderItemList = info.getSessionOrderItemList();
            if (CollUtil.isNotEmpty(contentViewList)) {
                map.put("session_content_view_list", StringUtils.collectionToCommaDelimitedString(contentViewList));
                map.put("session_content_view_size", contentViewList.size());
            }
            if (CollUtil.isNotEmpty(addToCartList)) {
                map.put("session_add_to_cart_list", StringUtils.collectionToCommaDelimitedString(addToCartList));
                map.put("session_add_to_cart_size", addToCartList.size());
            }
            if (CollUtil.isNotEmpty(orderItemList)) {
                map.put("session_order_item_list", StringUtils.collectionToCommaDelimitedString(orderItemList));
                map.put("session_order_item_size", orderItemList.size());
            }
            map.put("first_content_view_time", info.getFirstContentViewTime());
            map.put("last_content_view_time", info.getLastContentViewTime());
            map.put("first_add_to_cart_time", info.getFirstAddToCartTime());
            map.put("last_add_to_cart_time", info.getLastAddToCartTime());
            map.put("first_order_time", info.getFirstOrderTime());
            map.put("last_order_time", info.getLastOrderTime());

            if (Objects.equals(info.getUvCnt(), 1)) {
                map.put(RequestKey.EVENT_4, 1);
            }
            if (Objects.equals(info.getMbrCnt(), 1)) {
                map.put(RequestKey.EVENT_5, 1);
            }
        }

        pbLog.info(JsonHelper.toJSONString(map));
    }

    public static void logValidateFailed(String type, NoticeInfo info,
                                      HttpRequest httpRequest, ValidateCode code) {
        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("bid_id", info.getBidId());

        map.put("campaign_id", info.getCampaignId());
        map.put("ad_group_id", info.getAdGroupId());
        map.put("ad_id", info.getAdId());
        map.put("creative_id", info.getCreativeId());
        map.put("affiliate_id", info.getAffiliateId());

        map.put("referer", httpRequest.getHeader(HttpHeaders.REFERER));
        map.put("ua", httpRequest.getHeader(HttpHeaders.USER_AGENT));
        map.put("ip", httpRequest.getIp());
        map.put("device_id", info.getDeviceId());
        String bidSuccessPrice = info.getBidSuccessPrice();
        map.put("bid_success_price", NumberUtils.isParsable(bidSuccessPrice) ?
                new BigDecimal(bidSuccessPrice).doubleValue() : 0d);
        if (info.getEventType() != null) {
            map.put(info.getEventType(), 1);
        }
        map.put("loss_code", info.getLossCode());
        map.put("type", type);
        map.put("code", code.name());

        validateLog.info(JsonHelper.toJSONString(map));
    }

    public static void logImpInfoValidateSuccess(HttpRequest httpRequest, ImpInfoNoticeInfo info) {
        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("bid_id", info.getBidId());

        map.put("referer", httpRequest.getHeader(HttpHeaders.REFERER));
        map.put("ua_from_imp_info", httpRequest.getHeader(HttpHeaders.USER_AGENT));
        map.put("ip_from_imp_info", httpRequest.getIp());

        map.put("width", info.getWidth());
        map.put("height", info.getHeight());
        map.put("z_index", info.getZIndex());
        map.put("max_index", info.getMaxIndex());
        map.put("display", info.getDisplay());
        map.put("view_width", info.getViewWidth());
        map.put("view_height", info.getViewHeight());
        map.put("img_top", info.getImgLeft());
        map.put("img_bottom", info.getImgBottom());
        map.put("img_left", info.getImgLeft());
        map.put("img_right", info.getImgRight());

        impInfoLog.info(JsonHelper.toJSONString(map));
    }
}
