package com.tecdo.util;

import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.n.NativeRequest;
import com.tecdo.domain.openrtb.request.n.NativeRequestAsset;
import com.tecdo.domain.openrtb.response.n.Data;
import com.tecdo.domain.openrtb.response.n.Img;
import com.tecdo.domain.openrtb.response.n.Link;
import com.tecdo.domain.openrtb.response.n.NativeResponse;
import com.tecdo.domain.openrtb.response.n.NativeResponseAsset;
import com.tecdo.domain.openrtb.response.n.Title;
import com.tecdo.entity.Creative;
import com.tecdo.enums.openrtb.DataAssetTypeEnum;
import com.tecdo.enums.openrtb.ImageAssetTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdmGenerator {


  public static String bannerAdm(String clickUrl,
                                 String deepLink,
                                 String imgUrl,
                                 List<String> impTrackUrl,
                                 List<String> clickTrackUrl) {

    String adm = "";

    //todo adm format

    return adm;
  }

  public static NativeResponse nativeAdm(NativeRequest nativeRequest,
                                         AdDTO adDTO,
                                         String clickUrl,
                                         String deepLink,
                                         List<String> impTrackUrl,
                                         List<String> clickTrackUrl) {
    List<NativeRequestAsset> nativeRequestAssets = nativeRequest.getNativeRequestAssets();

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
    nativeResponse.setNativeResponseAssets(responseAssetList);
    nativeResponse.setImptrackers(impTrackUrl);
    Link link = new Link();
    link.setUrl(deepLink);
    link.setFallback(clickUrl);
    link.setClicktrackers(clickTrackUrl);
    nativeResponse.setLink(link);

    return nativeResponse;
  }

}
