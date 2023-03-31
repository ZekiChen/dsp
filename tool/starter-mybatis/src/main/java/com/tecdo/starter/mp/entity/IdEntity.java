package com.tecdo.starter.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 基础实体：只包含 主键ID
 *
 * Created by Zeki on 2022/8/24
 **/
@Data
public class IdEntity implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
}