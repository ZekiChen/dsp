package com.tecdo.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.domain.openrtb.request.*;
import com.tecdo.domain.openrtb.request.n.Asset;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 物料格式 过滤
 * <p>
 * Created by Zeki on 2023/1/3
 **/
@Component
public class CreativeFormatFilter extends AbstractRecallFilter {

    private static final String BANNER_W_ATTR = "banner_w";
    private static final String BANNER_H_ATTR = "banner_h";
    private static final String VIDEO_W_ATTR = "video_w";
    private static final String VIDEO_H_ATTR = "video_h";
    private static final String NATIVE_W_ATTR = "native_w";
    private static final String NATIVE_H_ATTR = "native_h";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, List<TargetCondition> conditions) {
        conditions = conditions.stream().filter(e ->
                StrUtil.containsAny(e.getAttribute(), BANNER_W_ATTR, BANNER_H_ATTR, VIDEO_W_ATTR, VIDEO_H_ATTR, NATIVE_W_ATTR, NATIVE_H_ATTR)
        ).collect(Collectors.toList());
        if (CollUtil.isEmpty(conditions)) {
            return true;
        }
        Banner banner = imp.getBanner();
        Video video = imp.getVideo();
        Native native1 = imp.getNative1();
        for (TargetCondition condition : conditions) {
            switch (condition.getAttribute()) {
                case BANNER_W_ATTR:
                    if (banner == null) {
                        return false;
                    }
                    if (banner.getW() != null) {
                        if (!ConditionUtil.compare(banner.getW().toString(), condition.getOperation(), condition.getValue())) {
                            return false;
                        }
                    } else {
                        if (CollUtil.isEmpty(banner.getFormat())) {
                            return false;
                        }
                        List<Integer> wList = banner.getFormat().stream().map(Format::getW).collect(Collectors.toList());
                        if (wList.stream().noneMatch(w -> ConditionUtil.compare(w.toString(), condition.getOperation(), condition.getValue()))) {
                            return false;
                        }
                    }
                    break;
                case BANNER_H_ATTR:
                    if (banner == null) {
                        return false;
                    }
                    if (banner.getH() != null) {
                        if (!ConditionUtil.compare(banner.getH().toString(), condition.getOperation(), condition.getValue())) {
                            return false;
                        }
                    } else {
                        if (CollUtil.isEmpty(banner.getFormat())) {
                            return false;
                        }
                        List<Integer> hList = banner.getFormat().stream().map(Format::getH).collect(Collectors.toList());
                        if (hList.stream().noneMatch(h -> ConditionUtil.compare(h.toString(), condition.getOperation(), condition.getValue()))) {
                            return false;
                        }
                    }
                    break;
                case VIDEO_W_ATTR:
                    if (video == null || video.getW() == null) {
                        return false;
                    }
                    if (!ConditionUtil.compare(video.getW().toString(), condition.getOperation(), condition.getValue())) {
                        return false;
                    }
                    break;
                case VIDEO_H_ATTR:
                    if (video == null || video.getH() == null) {
                        return false;
                    }
                    if (!ConditionUtil.compare(video.getH().toString(), condition.getOperation(), condition.getValue())) {
                        return false;
                    }
                    break;
                case NATIVE_W_ATTR:
                    if (native1 == null || native1.getNativeRequest() == null || CollUtil.isEmpty(native1.getNativeRequest().getAssets())) {
                        return false;
                    }
                    boolean hitFlag = false;
                    for (Asset asset : native1.getNativeRequest().getAssets()) {
                        if (asset.getImg() == null || asset.getImg().getW() == null) {
                            return false;
                        }
                        if (ConditionUtil.compare(asset.getImg().getW().toString(), condition.getOperation(), condition.getValue())) {
                            hitFlag = true;
                        }
                    }
                    if (!hitFlag) {
                        return false;
                    }
                    break;
                case NATIVE_H_ATTR:
                    if (native1 == null || native1.getNativeRequest() == null || CollUtil.isEmpty(native1.getNativeRequest().getAssets())) {
                        return false;
                    }
                    hitFlag = false;
                    for (Asset asset : native1.getNativeRequest().getAssets()) {
                        if (asset.getImg() == null || asset.getImg().getH() == null) {
                            return false;
                        }
                        if (ConditionUtil.compare(asset.getImg().getH().toString(), condition.getOperation(), condition.getValue())) {
                            hitFlag = true;
                        }
                    }
                    if (!hitFlag) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }
}
