package com.tecdo.util;

import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.constant.FormatKey;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.n.NativeRequest;
import com.tecdo.domain.openrtb.request.n.NativeRequestAsset;
import com.tecdo.domain.openrtb.response.n.Data;
import com.tecdo.domain.openrtb.response.n.Img;
import com.tecdo.domain.openrtb.response.n.Link;
import com.tecdo.domain.openrtb.response.n.NativeResponse;
import com.tecdo.domain.openrtb.response.n.NativeResponseAsset;
import com.tecdo.domain.openrtb.response.n.Title;
import com.tecdo.enums.openrtb.DataAssetTypeEnum;
import com.tecdo.enums.openrtb.ImageAssetTypeEnum;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdmGenerator {

  public static String bannerAdm(String clickUrl,
                                 String deepLink,
                                 String imgUrl,
                                 List<String> impTrackUrl,
                                 List<String> clickTrackUrl,
                                 String impInfoUrl) {
    String finalClickUrl = StringUtils.firstNonBlank(deepLink, clickUrl);
    StringBuilder impDivListBuilder = new StringBuilder();
    String impDivTemplate = "<img src=\"{impTrack}\" style=\"display:none\"/>";
    for (String s : impTrackUrl) {
      impDivListBuilder.append(impDivTemplate.replace("{impTrack}", s));
    }
    StringBuilder clickTrackBuilder = new StringBuilder();
    for (String s : clickTrackUrl) {
      clickTrackBuilder.append("\"").append(s).append("\"").append(",");
    }
    clickTrackBuilder.delete(clickTrackBuilder.length() - 1, clickTrackBuilder.length());
    String admTemplate = StringConfigUtil.getBannerTemplate();
    String adm = admTemplate.replace(FormatKey.CLICK_URL, finalClickUrl)
                            .replace(FormatKey.IMG_URL, imgUrl)
                            .replace(FormatKey.IMP_DIV_LIST, impDivListBuilder.toString())
                            .replace(FormatKey.CLICK_TRACK_URL_LIST, clickTrackBuilder.toString())
                            .replace(FormatKey.IMP_INFO_URL, impInfoUrl);
    return adm;
  }

  public static NativeResponse nativeAdm(NativeRequest nativeRequest,
                                         AdDTO adDTO,
                                         String clickUrl,
                                         String deepLink,
                                         List<String> impTrackUrl,
                                         List<String> clickTrackUrl) {
    List<NativeRequestAsset> nativeRequestAssets = nativeRequest.getAssets();

    List<NativeResponseAsset> responseAssetList = new ArrayList<>();
    for (NativeRequestAsset asset : nativeRequestAssets) {
      NativeResponseAsset nativeResponseAsset = new NativeResponseAsset();
      nativeResponseAsset.setId(asset.getId());
      nativeResponseAsset.setRequired(asset.getRequired());
      if (asset.getImg() != null) {
        Img img = new Img();
        Integer type = asset.getImg().getType();
        Creative creative;
        if (Objects.equals(type, ImageAssetTypeEnum.ICON.getValue())) {
          creative = adDTO.getCreativeMap().get(adDTO.getAd().getIcon());
        } else {
          creative = adDTO.getCreativeMap().get(adDTO.getAd().getImage());
        }
        img.setUrl(creative.getUrl());
        img.setW(creative.getWidth());
        img.setH(creative.getHeight());
        nativeResponseAsset.setImg(img);
      }
      if (asset.getTitle() != null) {
        Title title = new Title();
        Integer len = asset.getTitle().getLen();
        String titleString = adDTO.getAd().getTitle();
        if (len != null && len > 0) {
          titleString = titleString.substring(0, Math.min(len, titleString.length()));
        }
        title.setText(titleString);
        nativeResponseAsset.setTitle(title);
      }
      if (asset.getData() != null) {
        Integer type = asset.getData().getType();
        Integer len = asset.getData().getLen();
        String value = null;
        switch (DataAssetTypeEnum.of(type)) {
          case ctatext:
            value = adDTO.getAd().getCta();
            break;
          case DESC:
            value = adDTO.getAd().getDescription();
            break;
          case SPONSORED:
            value = adDTO.getAd().getName();
            break;
          default:
        }
        if (value == null) {
          continue;
        }
        if (len != null && len > 0) {
          value = value.substring(0, Math.min(len, value.length()));
        }
        Data data = new Data();
        data.setValue(value);
        nativeResponseAsset.setData(data);
      }
      responseAssetList.add(nativeResponseAsset);
    }
    NativeResponse nativeResponse = new NativeResponse();
    nativeResponse.setVer(nativeRequest.getVer());
    nativeResponse.setAssets(responseAssetList);
    nativeResponse.setImptrackers(impTrackUrl);
    Link link = new Link();
    link.setUrl(StringUtils.firstNonBlank(deepLink, clickUrl));
    link.setFallback(clickUrl);
    link.setClicktrackers(clickTrackUrl);
    nativeResponse.setLink(link);

    return nativeResponse;
  }

}
