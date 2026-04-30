package com.pinkcloset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinkcloset.entity.ClothingItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 衣物 Mapper 接口
 * 继承 MyBatis Plus BaseMapper，自带单表 CRUD
 */
@Mapper
public interface ClothingItemMapper extends BaseMapper<ClothingItem> {

    /**
     * 查询所有衣物（含已软删除），绕过 @TableLogic
     */
    @Select("SELECT * FROM clothing_item WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ClothingItem> selectAllIncludingDeleted(@Param("userId") Long userId);

    /**
     * 查询某用户所有衣物（含已软删除），按分类过滤，绕过 @TableLogic
     */
    @Select("SELECT * FROM clothing_item WHERE user_id = #{userId} AND category = #{category} ORDER BY create_time DESC")
    List<ClothingItem> selectAllIncludingDeletedByCategory(@Param("userId") Long userId, @Param("category") String category);

    /**
     * 软删除衣物：设置 deleted=1（绕过 @TableLogic）
     */
    @Update("UPDATE clothing_item SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int softDeleteById(@Param("id") Long id);

    /**
     * 恢复已软删除衣物（绕过 @TableLogic）
     */
    @Update("UPDATE clothing_item SET deleted = 0 WHERE id = #{id} AND deleted = 1")
    int restoreById(@Param("id") Long id);

    /**
     * 物理删除衣物（绕过 @TableLogic）
     */
    @Delete("DELETE FROM clothing_item WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    /**
     * 物理删除某用户的所有衣物（绕过 @TableLogic，彻底删除用户时用）
     */
    @Delete("DELETE FROM clothing_item WHERE user_id = #{userId}")
    int hardDeleteByUserId(@Param("userId") Long userId);
}
