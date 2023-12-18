package com.tecdo.filter;

import cn.hutool.core.io.resource.ResourceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tecdo.adm.api.delivery.entity.*;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.domain.openrtb.request.n.NativeRequest;
import com.tecdo.filter.factory.RecallFiltersFactory;
import com.tecdo.filter.util.FilterChainHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对 广告召回所有过滤器 进行测试
 * <p>
 * Created by Zeki on 2023/2/2
 */
@SpringBootTest
public class RecallFilterTest {

    @Autowired
    private RecallFiltersFactory filtersFactory;

    private AbstractRecallFilter firstFilter;
    private BidRequest bidRequest;
    private Affiliate affiliate;
    private AdDTO adDTO;
    private AdDTOWrapper adDTOWrapper;

    @BeforeEach
    public void init() {
        this.firstFilter = initAllRecallFilter().get(0);
        this.affiliate = initDefaultAffiliate();
        this.adDTO = initDefaultAdDTO();
        this.adDTOWrapper = new AdDTOWrapper("","",adDTO);
        this.bidRequest = initDefaultBidRequest();
    }

    @Test
    public void test_all_filter_by_banner_request() {
        List<Imp> imps = bidRequest.getImp();
        imps.forEach(imp -> FilterChainHelper.executeFilter("", firstFilter, adDTOWrapper, bidRequest, imp, affiliate));
    }

    @Test
    public void test_CreativeFormatFilter_by_banner_request_which_is_use_format() {
        this.bidRequest = initBidRequest("example-bid-request/banner-format.json");
        adDTO.setCreativeMap(buildCreativeMap("example-creative/creatives-format.json"));
        CreativeFormatFilter filter = filtersFactory.getCreativeFormatFilter();
        bidRequest.getImp().forEach(imp -> filter.doFilter(bidRequest, imp, adDTOWrapper, affiliate));
    }

    @Test
    public void test_CreativeFormatFilter_by_native_request() {
        this.bidRequest = initBidRequest("example-bid-request/native.json");
        adDTO.getAd().setType(AdTypeEnum.NATIVE.getType());
        adDTO.setCreativeMap(buildCreativeMap("example-creative/creatives-format.json"));
        CreativeFormatFilter filter = filtersFactory.getCreativeFormatFilter();
        NativeRequest nativeRequest = buildNativeRequest("example-bid-request/native-nativeRequest.json");
        bidRequest.getImp().forEach(imp -> {
            imp.getNative1().setNativeRequest(nativeRequest);
            filter.doFilter(bidRequest, imp, adDTOWrapper, affiliate);
        });
    }

    @Test
    public void test_ImpFrequencyFilter_and_ClickFrequencyFilter() {
        adDTO.setConditionMap(buildConditions("example-condition/conditions-frequency.json"));
        ImpFrequencyFilter impFilter = filtersFactory.getImpFrequencyFilter();
        ClickFrequencyFilter clickFilter = filtersFactory.getClickFrequencyFilter();
        bidRequest.getImp().forEach(imp -> impFilter.doFilter(bidRequest, imp, adDTOWrapper, affiliate));
        bidRequest.getImp().forEach(imp -> clickFilter.doFilter(bidRequest, imp, adDTOWrapper, affiliate));
    }

    @Test
    public void test_BudgetFilter() {
        BudgetFilter filter = filtersFactory.getBudgetFilter();
        bidRequest.getImp().forEach(imp -> filter.doFilter(bidRequest, imp, adDTOWrapper, affiliate));
    }

    // ====================================================================================================

    private List<AbstractRecallFilter> initAllRecallFilter() {
        List<AbstractRecallFilter> filters = filtersFactory.createFilters();
        FilterChainHelper.assemble(filters);
        return filters;
    }

    /**
     * 目前只支持 banner 和 native 广告
     */
    private BidRequest initDefaultBidRequest() {
        String json = ResourceUtil.readUtf8Str("example-bid-request/banner.json");
        return JSON.parseObject(json, BidRequest.class);
    }

    @Nullable
    private BidRequest initBidRequest(String resourcePath) {
        return JSON.parseObject(ResourceUtil.readUtf8Str(resourcePath), BidRequest.class);
    }

    private Affiliate initDefaultAffiliate() {
        Affiliate affiliate = new Affiliate();
        affiliate.setId(1);
        affiliate.setName("渠道A");
        affiliate.setSecret("tokenA");
        affiliate.setApi("o_2.5_n_1.1");
        affiliate.setStatus(1);
        Date currentTime = new Date();
        affiliate.setCreateTime(currentTime);
        affiliate.setUpdateTime(currentTime);
        return affiliate;
    }

    private AdDTO initDefaultAdDTO() {
        return AdDTO.builder()
                .ad(buidAd())
                .creativeMap(buildCreativeMap("example-creative/creatives.json"))
                .adGroup(buildAdGroup())
                .conditionMap(buildConditions("example-condition/conditions.json"))
                .campaign(buildCampaign())
                .campaignRtaInfo(buildCampaignRtaInfo())
                .build();
    }

    private Map<String, TargetCondition> buildConditions(String resourcePath) {
        String json = ResourceUtil.readUtf8Str(resourcePath);
        List<TargetCondition> conditions = JSON.parseArray(json, TargetCondition.class);
        return conditions.stream().collect(Collectors.toMap(TargetCondition::getAttribute, e -> e));
    }

    private Ad buidAd() {
        Ad ad = new Ad();
        ad.setId(1);
        ad.setGroupId(1);
        ad.setName("AD_A");
        ad.setType(1);
        ad.setImage(2);
        ad.setIcon(1);
        ad.setTitle("广告A");
        ad.setDescription("描述A");
        ad.setCta("CTA文本A");
        ad.setVideo(null);
        ad.setStatus(1);
        ad.setCreateTime(new Date());
        ad.setUpdateTime(new Date());
        return ad;
    }

    private Map<Integer, Creative> buildCreativeMap(String resourcePath) {
        String json = ResourceUtil.readUtf8Str(resourcePath);
        return JSON.parseObject(json, new TypeReference<Map<Integer, Creative>>() {});
    }

    private AdGroup buildAdGroup() {
        AdGroup adGroup = new AdGroup();
        adGroup.setId(1);
        adGroup.setCampaignId(1);
        adGroup.setName("groupA");
        adGroup.setClickUrl("click_url_A");
        adGroup.setDeeplink(null);
        adGroup.setImpTrackUrls("imp_track_url_A,imp_track_url_B");
        adGroup.setClickTrackUrls("click_track_url_A,click_track_url_B");
        adGroup.setDailyBudget(100.55D);
        adGroup.setBidStrategy(1);
        adGroup.setOptPrice(10.05D);
        adGroup.setStatus(1);
        adGroup.setCreateTime(new Date());
        adGroup.setUpdateTime(new Date());
        return adGroup;
    }

    private Campaign buildCampaign() {
        Campaign campaign = new Campaign();
        campaign.setId(1);
        campaign.setName("campaignA");
        campaign.setDailyBudget(100.0D);
        campaign.setPackageName("appA");
        campaign.setCategory("categoryA,categoryB");
        campaign.setDomain(null);
        campaign.setStatus(1);
        campaign.setCreateTime(new Date());
        campaign.setUpdateTime(new Date());
        return campaign;
    }

    private CampaignRtaInfo buildCampaignRtaInfo() {
        CampaignRtaInfo campaignRtaInfo = new CampaignRtaInfo();
        campaignRtaInfo.setId(1);
        campaignRtaInfo.setCampaignId(1);
        campaignRtaInfo.setAdvCampaignId("10086");
        campaignRtaInfo.setAdvMemId(10086);
        campaignRtaInfo.setRtaFeature(1);
        campaignRtaInfo.setCreateTime(new Date());
        campaignRtaInfo.setUpdateTime(new Date());
        return campaignRtaInfo;
    }

    private NativeRequest buildNativeRequest(String resourcePath) {
        String json = ResourceUtil.readUtf8Str(resourcePath);
        return JSON.parseObject(json, NativeRequest.class);
    }
}
