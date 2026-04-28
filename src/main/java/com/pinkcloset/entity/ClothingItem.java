package com.pinkcloset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 衣物实体类 - 对应数据库 clothing_item 表
 *
 * 分类枚举说明：
 *   TOP        - 上衣
 *   BOTTOM     - 裤子 / 下装
 *   SHOES      - 鞋子
 *   ACCESSORY  - 配饰
 */
@Data
@TableName("clothing_item")
public class ClothingItem {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 衣物名称，如"白色棉质T恤" */
    private String name;

    /**
     * 分类
     * 取值范围：TOP / BOTTOM / SHOES / ACCESSORY
     */
    private String category;

    /** 图片存储路径（相对路径，如 uploads/xxx.jpg） */
    private String imageUrl;

    /** 主色调，如"粉色" */
    private String color;

    /** 风格标签，逗号分隔，如"甜美,少女" */
    private String style;

    /** 上传时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
