package com.tecdo.service;

import com.tecdo.adm.api.delivery.entity.AffCountryBundleList;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.common.constant.Constant;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.constant.RequestKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.domain.biz.validate.FraudInfo;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.log.ValidateLogger;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.AffCountryBundleListManager;
import com.tecdo.service.init.AffiliateManager;
import com.tecdo.service.init.CheatingDataManager;
import com.tecdo.service.init.IpTableManager;
import com.tecdo.service.init.Pair;
import com.tecdo.service.init.PixalateFraudManager;
import com.tecdo.transform.IProtoTransform;
import com.tecdo.transform.ProtoTransformFactory;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.ResponseHelper;
import com.tecdo.util.SignHelper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.tecdo.common.constant.ConditionConstant.EXCLUDE;
import static com.tecdo.common.constant.ConditionConstant.INCLUDE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateService {

    private final AffiliateManager affiliateManager;
    private final IpTableManager ipTableManager;
    private final AffCountryBundleListManager affCountryBundleListManager;
    private final CheatingDataManager cheatingDataManager;
    private final PixalateFraudManager fraudManager;

    private final MessageQueue messageQueue;
    private final CacheService cacheService;
    private final ThreadPool threadPool;

    @Value("${pac.notice.expire.win}")
    private long winExpire;
    @Value("${pac.notice.expire.imp}")
    private long impExpire;
    @Value("${pac.notice.expire.click}")
    private long clickExpire;
    @Value("${pac.notice.expire.pb}")
    private long pbExpire;
    @Value("${pac.notice.expire.auto-redirect}")
    private long autoRedirectExpire;

    @Value("${pac.request.validate}")
    private boolean needValidateRequest;

    @Value("${pac.request.validate.filter-device:}")
    private String deviceFilter;
    @Value("${pac.request.validate.filter-ip:}")
    private String ipFilter;
    @Value("${pac.request.validate.pixalate.ip.enabled}")
    private boolean pixalateIpEnabled;
    @Value("${pac.request.validate.pixalate.device-id.enabled}")
    private boolean pixalateDeviceIdEnabled;

    public void validateBidRequest(HttpRequest httpRequest) {
        String token = httpRequest.getParamAsStr(RequestKey.TOKEN);
        Affiliate affiliate = affiliateManager.getAffiliate(token);

        if (affiliate == null) {
            log.warn("validate fail! aff doesn't exist, token: {}", token);
            ResponseHelper.badRequest(messageQueue, Params.create(), httpRequest);
            return;
        }

        String api = affiliate.getApi();
        IProtoTransform protoTransform = ProtoTransformFactory.getProtoTransform(api);
        if (protoTransform == null) {
            log.warn("validate fail! bid protocol is not supported, api: {}", api);
            ResponseHelper.noBid(messageQueue, Params.create(), httpRequest);
            return;
        }

        if (StringUtils.isEmpty(httpRequest.getBody())) {
            log.warn("request body is empty, affiliateId: {}", affiliate.getId());
            ResponseHelper.badRequest(messageQueue, Params.create(), httpRequest);
            return;
        }

        BidRequest bidRequest = protoTransform.requestTransform(httpRequest.getBody());
        if (bidRequest == null || !validateBidRequest(bidRequest)) {
            log.warn("validate bidRequest fail, affiliateId: {}, body: {}",
                    affiliate.getId(),
                    httpRequest.getBody());
            ResponseHelper.badRequest(messageQueue, Params.create(), httpRequest);
            return;
        }

        String country = bidRequest.getDevice().getGeo().getCountry();
        String bundle = bidRequest.getApp().getBundle();
        if (!validateAffCountryBundleList(affiliate.getId(), country, bundle)) {
            log.info(
                    "affiliate country bundle list validate fail! ,affiliate: {}, country: {}, bundle: {}",
                    affiliate.getId(),
                    country,
                    bundle);
            ValidateLogger.log("black", bidRequest, affiliate, true);
            ResponseHelper.noBid(messageQueue, Params.create(), httpRequest);
            return;
        }

        Device device = bidRequest.getDevice();
        String ip = Optional.ofNullable(device.getIp()).orElse(device.getIpv6());

        Pair<Boolean, String> ipCheatCheck = cheatingDataManager.check(ip);
        if (ipCheatCheck.left) {
            if (ipFilter.contains(ipCheatCheck.right)) {
                ValidateLogger.log(ipCheatCheck.right, bidRequest, affiliate, true);
                ResponseHelper.noBid(messageQueue, Params.create(), httpRequest);
                return;
            }
            ValidateLogger.log(ipCheatCheck.right, bidRequest, affiliate, false);
        }

        Pair<Boolean, String> deviceCheatCheck = cheatingDataManager.check(device.getIfa());
        if (deviceCheatCheck.left) {
            if (deviceFilter.contains(deviceCheatCheck.right)) {
                ValidateLogger.log(deviceCheatCheck.right, bidRequest, affiliate, true);
                ResponseHelper.noBid(messageQueue, Params.create(), httpRequest);
                return;
            }
            ValidateLogger.log(deviceCheatCheck.right, bidRequest, affiliate, false);
        }

        Pair<Boolean, String> blocked = ipTableManager.ipCheck(ip);
        if (blocked.left) {
            if (needValidateRequest) {
                ValidateLogger.log(blocked.right, bidRequest, affiliate, true);
                ResponseHelper.noBid(messageQueue, Params.create(), httpRequest);
                return;
            }
            ValidateLogger.log(blocked.right, bidRequest, affiliate, false);
        }

        // need query redis,so put into threadPool
        threadPool.execute(() -> {
            FraudInfo ipFraudInfo = getFraudInfoByIp(ip);
            FraudInfo deviceIdFraudInfo = getFraudInfoByDeviceId(device.getIfa());

            logFraudInfo(ipFraudInfo, deviceIdFraudInfo, bidRequest, affiliate);

            if (ipFraudInfo.isFilter() || deviceIdFraudInfo.isFilter()) {
                ResponseHelper.noBid(messageQueue, Params.create(), httpRequest);
                return;
            }

            messageQueue.putMessage(EventType.RECEIVE_BID_REQUEST,
                                    Params.create(ParamKey.BID_REQUEST, bidRequest)
                                          .put(ParamKey.HTTP_REQUEST, httpRequest)
                                          .put(ParamKey.AFFILIATE, affiliate));
        });
    }

    private boolean validateAffCountryBundleList(Integer affiliateId, String country, String bundle) {
        List<AffCountryBundleList> lists = affCountryBundleListManager.listAffCountryBundleList(affiliateId);
        if (CollUtil.isEmpty(lists)) {
            return true;
        }
        for (AffCountryBundleList e : lists) {
            if (country.equals(e.getCountry())) {
                if (StrUtil.isBlank(e.getBundle())) {
                    return true;
                }
                String[] targetBundles = e.getBundle().split(StrUtil.COMMA);
                switch (e.getOperation()) {
                    case INCLUDE:
                        return Arrays.asList(targetBundles).contains(bundle);
                    case EXCLUDE:
                        return !Arrays.asList(targetBundles).contains(bundle);
                    default:
                        log.error("unknown operation for filter aff country bundle list: {}", e.getOperation());
                        return true;
                }
            }
        }
        return true;
    }

    private boolean validateBidRequest(BidRequest bidRequest) {
        // 目标渠道：目前只参与移动端流量的竞价
        if (bidRequest.getApp() == null) {
            return false;
        }
        // 设备信息都不传，不太合理
        if (bidRequest.getDevice() == null) {
            return false;
        }
        // 没有设备id或者设备id非法
        if (bidRequest.getDevice().getIfa() == null
                || bidRequest.getDevice().getIfa().length() != 36
                || Constant.ERROR_DEVICE_ID.equals(bidRequest.getDevice().getIfa())) {
            return false;
        }
        // 缺少ip信息，过滤
        if (StringUtils.isEmpty(bidRequest.getDevice().getIp()) &&
                StringUtils.isEmpty(bidRequest.getDevice().getIpv6())) {
            return false;
        }
        // 没有设备位置信息
        if (bidRequest.getDevice().getGeo() == null) {
            return false;
        }
        // 没有国家信息
        if (bidRequest.getDevice().getGeo().getCountry() == null) {
            return false;
        }
        // 没有bundle信息
        if (StringUtils.isEmpty(bidRequest.getApp().getBundle())) {
            return false;
        }
        // 展示位必须有
        List<Imp> imps = bidRequest.getImp();
        if (CollUtil.isEmpty(imps)) {
            return false;
        }
        // banner / native / video / audio 四个对象只能存在一个
        boolean existNonUnique = imps.stream().anyMatch(imp -> !CreativeHelper.isAdFormatUnique(imp));
        if (existNonUnique) {
            return false;
        }

        return true;
    }

    /**
     * 通知请求校验：
     * 1. 请求参数校验
     * 2. bid_id 合法性
     * 3. 归因窗口限制
     * 4. click / pb 通知是否来源于上层漏斗
     * 5. bid_id 去重校验
     *
     * @return ValidateCode
     */
    public ValidateCode validateNoticeRequest(String bidId, String sign, Integer campaignId,
                                              EventType eventType, Integer affiliateId) {
        if (StrUtil.hasBlank(bidId, sign) || campaignId == null) {
            return ValidateCode.BLANK_VALID_FAILED;
        }
        if (!bidIdValid(bidId, sign, campaignId.toString())) {
            return ValidateCode.BID_ID_VALID_FAILED;
        }
        if (!windowValid(bidId, eventType, affiliateId)) {
            return ValidateCode.WINDOW_VALID_FAILED;
        }
        if (!funnelValid(bidId, eventType)) {
            return ValidateCode.FUNNEL_VALID_FAILED;
        }
        if (!duplicateValid(bidId, eventType)) {
            return ValidateCode.DUPLICATE_VALID_FAILED;
        }
        return ValidateCode.SUCCESS;
    }

    private boolean bidIdValid(String bidId, String sign, String campaignId) {
        return Objects.equals(sign, SignHelper.digest(bidId, campaignId));
    }

    public boolean windowValid(String bidId, EventType eventType, Integer affiliateId) {
        long expire = winExpire;
        Long affImpExpire = affiliateManager.getAffiliate(affiliateId).getImpExpire();
        switch (eventType) {
            case RECEIVE_WIN_NOTICE:
            case RECEIVE_LOSS_NOTICE:
                expire = winExpire;
                break;
            case RECEIVE_IMP_NOTICE:
            case RECEIVE_IMP_INFO_NOTICE:
                expire = affImpExpire != null ? affImpExpire : impExpire;
                break;
            case RECEIVE_CLICK_NOTICE:
                expire = clickExpire;
                break;
            case RECEIVE_FORCE_REQUEST:
                expire = autoRedirectExpire;
                break;
            case RECEIVE_PB_NOTICE:
                expire = pbExpire;
        }
        // bidId 由 32位UUID + 13位时间戳 构成
        if (bidId.length() != 45) {
            return false;
        }
        long createStamp = Long.parseLong(bidId.substring(32, 45));
        long expireStamp = createStamp + (expire * 1000);
        return System.currentTimeMillis() <= expireStamp;
    }

    private boolean funnelValid(String bidId, EventType eventType) {
        switch (eventType) {
            case RECEIVE_CLICK_NOTICE:
                return cacheService.getNoticeCache().hasWin(bidId)
                        || cacheService.getNoticeCache().hasImp(bidId);
            case RECEIVE_PB_NOTICE:
                return cacheService.getNoticeCache().hasClick(bidId);
            default:
                return true;
        }
    }

    private boolean duplicateValid(String bidId, EventType eventType) {
        switch (eventType) {
            case RECEIVE_WIN_NOTICE:
                return cacheService.getNoticeCache().winMark(bidId);
            case RECEIVE_LOSS_NOTICE:
                return cacheService.getNoticeCache().lossMark(bidId);
            case RECEIVE_IMP_NOTICE:
                return cacheService.getNoticeCache().impMark(bidId);
            default:
                return true;
        }
    }

    // 获取欺诈信息
    private FraudInfo getFraudInfoByIp(String ip) {
        if (pixalateIpEnabled) {
            String fraud = cacheService.getPixalateCache().getFraudByIp(ip);
            FraudInfo fraudInfo = parseFraudInfo(fraud);
            if (fraudInfo != null)
                return fraudInfo;
        }
        return new FraudInfo(null, null, false);
    }

    private FraudInfo getFraudInfoByDeviceId(String deviceId) {
        if (pixalateDeviceIdEnabled) {
            String fraud = cacheService.getPixalateCache().getFraudByDeviceId(deviceId);
            FraudInfo fraudInfo = parseFraudInfo(fraud);
            if (fraudInfo != null)
                return fraudInfo;
        }
        return new FraudInfo(null, null, false);
    }

    private FraudInfo parseFraudInfo(String fraud) {
        if (StrUtil.isNotBlank(fraud)) {
            String[] arr = fraud.split(StrUtil.COMMA);
            if (arr.length == 2) {
                String fraudType = arr[0];
                double probability = Double.parseDouble(arr[1]);
                boolean shouldBeFiltered = probability >= fraudManager.getProbability(fraudType);
                return new FraudInfo(fraudType, probability, shouldBeFiltered);
            }
        }
        return null;
    }

    private void logFraudInfo(FraudInfo ipFraudInfo, FraudInfo deviceIdFraudInfo, BidRequest bidRequest, Affiliate affiliate) {
        if (ipFraudInfo.getType() != null || deviceIdFraudInfo.getType() != null) {
            boolean shouldBeFiltered = ipFraudInfo.isFilter() || deviceIdFraudInfo.isFilter();
            Double ipProbability = ipFraudInfo.getProbability();
            Double deviceIdProbability = deviceIdFraudInfo.getProbability();
            String types = Stream.of(ipFraudInfo.getType(), deviceIdFraudInfo.getType())
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(StrUtil.COMMA));
            ValidateLogger.log(types, bidRequest, affiliate, shouldBeFiltered, ipProbability, deviceIdProbability);
        }
    }
}
