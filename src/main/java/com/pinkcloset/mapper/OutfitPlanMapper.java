package com.pinkcloset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinkcloset.entity.OutfitPlan;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 搭配方案 Mapper 接口
 */
@Mapper
public interface OutfitPlanMapper extends BaseMapper<OutfitPlan> {

    /**
     * 查询所有搭配方案（含已软删除），绕过 @TableLogic
     */
    @Select("SELECT * FROM outfit_plan WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<OutfitPlan> selectAllIncludingDeleted(@Param("userId") Long userId);

    /**
     * 软删除搭配方案：设置 deleted=1（绕过 @TableLogic）
     */
    @Update("UPDATE outfit_plan SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int softDeleteById(@Param("id") Long id);

    /**
     * 恢复已软删除搭配方案（绕过 @TableLogic）
     */
    @Update("UPDATE outfit_plan SET deleted = 0 WHERE id = #{id} AND deleted = 1")
    int restoreById(@Param("id") Long id);

    /**
     * 物理删除搭配方案（绕过 @TableLogic）
     */
    @Delete("DELETE FROM outfit_plan WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    /**
     * 物理删除某用户的所有搭配方案（绕过 @TableLogic，彻底删除用户时用）
     */
    @Delete("DELETE FROM outfit_plan WHERE user_id = #{userId}")
    int hardDeleteByUserId(@Param("userId") Long userId);
}
