package com.pinkcloset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinkcloset.entity.ClothingItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 衣物 Mapper 接口
 * 继承 MyBatis Plus BaseMapper，自带单表 CRUD
 */
@Mapper
public interface ClothingItemMapper extends BaseMapper<ClothingItem> {
}
