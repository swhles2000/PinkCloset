package com.pinkcloset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搭配方案实体类 - 对应数据库 outfit_plan 表
 * 记录用户保存的某套搭配（上衣 + 裤子 + 鞋子 + 可选配饰）
 */
@Data
@TableName("outfit_plan")
public class OutfitPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 搭配名称 */
    private String name;

    /** 上衣 ID */
    private Long topId;

    /** 裤子 ID */
    private Long bottomId;

    /** 鞋子 ID */
    private Long shoesId;

    /** 配饰 ID（可选） */
    private Long accessoryId;

    /** 备注 */
    private String note;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
