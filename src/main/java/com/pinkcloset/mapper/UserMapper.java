package com.pinkcloset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinkcloset.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户 Mapper — 继承 BaseMapper 获得基础 CRUD
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 物理删除用户（绕过 @TableLogic 的软删除机制）
     */
    @Delete("DELETE FROM user WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    /**
     * 查询所有用户（包含已软删除的），绕过 @TableLogic
     */
    @Select("SELECT * FROM user ORDER BY create_time DESC")
    List<User> selectAllIncludingDeleted();

    /**
     * 冻结用户：设置 deleted=1（绕过 @TableLogic）
     */
    @Update("UPDATE user SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int freezeById(@Param("id") Long id);

    /**
     * 恢复用户：设置 deleted=0（绕过 @TableLogic）
     */
    @Update("UPDATE user SET deleted = 0 WHERE id = #{id} AND deleted = 1")
    int restoreById(@Param("id") Long id);
}
