package com.tecdo.service.response;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.response.custom.vivo.VivoAdm;
import com.tecdo.domain.openrtb.response.custom.vivo.VivoTrack;
import com.tecdo.service.track.ClickTrackBuilder;
import com.tecdo.service.track.ImpTrackBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/9/15
 */
@Component
@RequiredArgsConstructor
public class VivoResponseBuilder {

    private final ImpTrackBuilder impTrackBuilder;
    private final ClickTrackBuilder clickTrackBuilder;

    public Object buildAdm(String packageName) {
        VivoAdm vivoAdm = new VivoAdm();
        vivoAdm.setAppPackage(packageName);
        return vivoAdm;
    }

    public List<VivoTrack> buildTracks(String sysImpTrack, String sysClickTrack,
                                       AdDTOWrapper wrapper, BidRequest bidRequest,
                                       Affiliate affiliate, String sign) {
        List<VivoTrack> vivoTracks = impTrackBuilder.build(sysImpTrack, wrapper, sign, bidRequest, affiliate)
                .stream()
                .map(url -> VivoTrack.builder().eventType(1).url(url).build())
                .collect(Collectors.toList());
        vivoTracks.addAll(clickTrackBuilder.build(sysClickTrack, wrapper, sign, bidRequest, affiliate)
                .stream()
                .map(url -> VivoTrack.builder().eventType(2).url(url).build())
                .collect(Collectors.toList()));
        return vivoTracks;
    }
}
