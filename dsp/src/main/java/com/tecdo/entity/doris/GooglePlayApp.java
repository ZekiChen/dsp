package com.tecdo.entity.doris;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@TableName("google_play_app")
public class GooglePlayApp implements Serializable {


    private String bundleId;

    @TableField("is_found")
    private boolean found;


    private String categorys;

    private String tags;

    private String score;

    private Long downloads;

    private Long reviews;

    @TableField(exist = false)
    private List<String> categoryList;

    @TableField(exist = false)
    private List<String> tagList;
}