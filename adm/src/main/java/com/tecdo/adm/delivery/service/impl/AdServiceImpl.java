package com.tecdo.adm.delivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.adm.api.delivery.mapper.AdMapper;
import com.tecdo.adm.api.delivery.vo.AdVO;
import com.tecdo.adm.api.delivery.vo.BatchAdUpdateVO;
import com.tecdo.adm.api.delivery.vo.SimpleAdUpdateVO;
import com.tecdo.adm.api.delivery.vo.SimpleAdVO;
import com.tecdo.adm.delivery.service.IAdService;
import com.tecdo.adm.delivery.wrapper.AdWrapper;
import com.tecdo.adm.log.service.IBizLogApiService;
import com.tecdo.starter.log.exception.ServiceException;
import com.tecdo.starter.mp.entity.BaseEntity;
import com.tecdo.starter.mp.entity.IdEntity;
import com.tecdo.starter.mp.entity.StatusEntity;
import com.tecdo.starter.mp.enums.BaseStatusEnum;
import com.tecdo.starter.tool.BigTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/3/6
 */
@Service
@RequiredArgsConstructor
public class AdServiceImpl extends ServiceImpl<AdMapper, Ad> implements IAdService {

    private final IBizLogApiService bizLogApiService;

    @Override
    public List<Ad> listByAdGroupId(Integer adGroupId) {
        return baseMapper.selectList(Wrappers.<Ad>lambdaQuery().eq(Ad::getGroupId, adGroupId));
    }

    @Override
    public void deleteByAdGroupIds(List<Integer> adGroupIds) {
        baseMapper.delete(Wrappers.<Ad>lambdaQuery().in(Ad::getGroupId, adGroupIds));
    }

    @Override
    public boolean logicDelete(List<Integer> adIds) {
        if (CollUtil.isEmpty(adIds)) return false;
        Date date = new Date();
        List<StatusEntity> adStatusList = listStatus(adIds);
        List<Ad> entities = adIds.stream().map(id -> {
            Ad entity = new Ad();
            entity.setId(id);
            entity.setStatus(BaseStatusEnum.DELETE.getType());
            entity.setUpdateTime(date);
            return entity;
        }).collect(Collectors.toList());
        updateBatchById(entities);
        bizLogApiService.logByDeleteAd(adIds, adStatusList);
        return true;
    }

    @Override
    public boolean copy(String sourceAdIds, List<Integer> targetAdGroupIds, Integer targetAdStatus) {
        if (StrUtil.isBlank(sourceAdIds) || CollUtil.isEmpty(targetAdGroupIds)) {
            return false;
        }
        List<Ad> sourceAds = listByIds(BigTool.toIntList(sourceAdIds));
        List<Ad> targetAds = targetAdGroupIds.stream()
                .flatMap(groupId -> sourceAds.stream()
                        .map(sourceAd -> {
                            Ad targetAd = BeanUtil.copyProperties(sourceAd, Ad.class);
                            resetBaseEntity(targetAd);
                            targetAd.setGroupId(groupId);
                            targetAd.setStatus(targetAdStatus);
                            return targetAd;
                        }))
                .collect(Collectors.toList());
        saveBatch(targetAds);
        return true;
    }

    @Override
    public boolean editListInfo(SimpleAdUpdateVO vo) {
        Ad entity = getById(vo.getId());
        if (entity == null) {
            return false;
        }
        AdVO beforeVO = Objects.requireNonNull(com.tecdo.starter.tool.util.BeanUtil.copy(entity, AdVO.class));
        entity.setName(vo.getName());
        entity.setRemark(vo.getRemark());
        entity.setUpdateTime(new Date());
        AdVO afterVO = Objects.requireNonNull(com.tecdo.starter.tool.util.BeanUtil.copy(entity, AdVO.class));
        bizLogApiService.logByUpdateAd(beforeVO, afterVO);
        return updateById(entity);
    }

    @Override
    public boolean updateBatch(BatchAdUpdateVO vo) {
        List<Ad> ads = listByIds(vo.getAdIds());
        if (StrUtil.isNotBlank(vo.getDescription()) || StrUtil.isNotBlank(vo.getCta()) || StrUtil.isNotBlank(vo.getTitle())) {
            boolean exist = ads.stream().anyMatch(e -> AdTypeEnum.NATIVE.getType() != e.getType());
            if (exist) {
                throw new ServiceException("您当前的批量操作包含非native的ad！");
            }
        }
        Map<Integer, Ad> beAdMap = ads.stream().collect(Collectors.toMap(IdEntity::getId, Function.identity()));
        Date date = new Date();
        List<Ad> entities = vo.getAdIds().stream().map(id -> {
            Ad entity = new Ad();
            entity.setId(id);
            entity.setTitle(vo.getTitle());
            entity.setDescription(vo.getDescription());
            entity.setCta(vo.getCta());
            entity.setStatus(vo.getStatus());
            entity.setUpdateTime(date);
            return entity;
        }).collect(Collectors.toList());
        updateBatchById(entities);
        bizLogApiService.logByUpdateBatchAd(beAdMap, vo);
        return true;
    }

    @Override
    public List<SimpleAdVO> listSimpleAd(List<Integer> adGroupIds) {
        List<SimpleAdVO> vos = baseMapper.listSimpleAd(adGroupIds);
        vos.forEach(vo -> {
            AdTypeEnum adTypeEnum = AdTypeEnum.of(vo.getType());
            vo.setTypeName(adTypeEnum.getDesc().toLowerCase());
        });
        return vos;
    }

    @Override
    public List<Integer> listIdByGroupIds(List<Integer> adGroupIds) {
        if (CollUtil.isEmpty(adGroupIds)) {
            return new ArrayList<>();
        }
        return baseMapper.listIdByGroupIds(adGroupIds);
    }

    @Override
    public List<StatusEntity> listStatus(List<Integer> ids) {
        return baseMapper.listStatus(ids);
    }

    @Override
    public boolean edit(Ad entity) {
        logByUpdate(entity);
        return updateById(entity);
    }

    private void logByUpdate(Ad after) {
        Ad before = getById(after.getId());
        AdVO beforeVO = AdWrapper.build().entityVO(before);
        AdVO afterVO = AdWrapper.build().entityVO(after);
        bizLogApiService.logByUpdateAd(beforeVO, afterVO);
    }

    private static void resetBaseEntity(BaseEntity entity) {
        entity.setId(null);
        entity.setCreateTime(null);
        entity.setUpdateTime(null);
    }
}
