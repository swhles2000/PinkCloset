package com.pinkcloset.controller;

import com.pinkcloset.common.Result;
import com.pinkcloset.dto.AdminLoginRequest;
import com.pinkcloset.entity.ClothingItem;
import com.pinkcloset.entity.OutfitPlan;
import com.pinkcloset.entity.User;
import com.pinkcloset.entity.UserRoot;
import com.pinkcloset.mapper.ClothingItemMapper;
import com.pinkcloset.mapper.OutfitPlanMapper;
import com.pinkcloset.mapper.UserMapper;
import com.pinkcloset.mapper.UserRootMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 超级管理员接口
 *
 * POST   /api/admin/login              管理员登录
 * GET    /api/admin/users               获取所有用户（含已注销）
 * GET    /api/admin/users/{id}/clothes  获取某用户的所有衣物（含已删除）
 * PUT    /api/admin/users/{id}/freeze   冻结用户（软删除）
 * PUT    /api/admin/users/{id}/restore  恢复已注销用户
 * DELETE /api/admin/users/{id}          彻底删除用户及关联数据
 *
 * 衣物管理：
 * PUT    /api/admin/clothes/{id}/soft        管理员软删除衣物
 * GET    /api/admin/clothes/{id}/restore      恢复已软删除的衣物
 * DELETE /api/admin/clothes/{id}/hard         彻底删除衣物
 *
 * 搭配方案管理：
 * PUT    /api/admin/outfits/{id}/soft         管理员软删除搭配方案
 * GET    /api/admin/outfits/{id}/restore      恢复已软删除的搭配方案
 * DELETE /api/admin/outfits/{id}/hard         彻底删除搭配方案
 * GET    /api/admin/users/{id}/outfits      获取某用户的所有搭配方案（含已删除）
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRootMapper userRootMapper;
    private final UserMapper userMapper;
    private final ClothingItemMapper clothingItemMapper;
    private final OutfitPlanMapper outfitPlanMapper;

    // ──────────────────────────────────────────────
    //  管理员登录
    // ──────────────────────────────────────────────
    @PostMapping("/login")
    public Result<?> login(@RequestBody AdminLoginRequest req) {
        if (!StringUtils.hasText(req.getUsername()) || !StringUtils.hasText(req.getPassword())) {
            return Result.fail("用户名和密码不能为空");
        }

        UserRoot admin = userRootMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRoot>()
                        .eq(UserRoot::getUsername, req.getUsername()));
        if (admin == null) {
            return Result.fail("管理员账号不存在");
        }

        if (!req.getPassword().equals(admin.getPassword())) {
            return Result.fail("密码错误");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id",       admin.getId());
        data.put("username", admin.getUsername());

        return Result.success("管理员登录成功", data);
    }

    // ──────────────────────────────────────────────
    //  获取所有用户（含已注销的，绕过 @TableLogic）
    // ──────────────────────────────────────────────
    @GetMapping("/users")
    public Result<?> getAllUsers() {
        List<User> allUsers = userMapper.selectAllIncludingDeleted();
        return Result.success(allUsers);
    }

    // ──────────────────────────────────────────────
    //  获取某用户的所有衣物（含已删除，绕过 @TableLogic）
    // ──────────────────────────────────────────────
    @GetMapping("/users/{userId}/clothes")
    public Result<?> getUserClothes(@PathVariable Long userId) {
        List<ClothingItem> items = clothingItemMapper.selectAllIncludingDeleted(userId);
        return Result.success(items);
    }

    // ──────────────────────────────────────────────
    //  获取某用户的所有搭配方案（含已删除，绕过 @TableLogic）
    // ──────────────────────────────────────────────
    @GetMapping("/users/{userId}/outfits")
    public Result<?> getUserOutfits(@PathVariable Long userId) {
        List<OutfitPlan> plans = outfitPlanMapper.selectAllIncludingDeleted(userId);
        return Result.success(plans);
    }

    // ──────────────────────────────────────────────
    //  管理员软删除衣物（deleted=1）
    // ──────────────────────────────────────────────
    @PutMapping("/clothes/{id}/soft")
    public Result<?> softDeleteClothing(@PathVariable Long id) {
        int rows = clothingItemMapper.softDeleteById(id);
        if (rows == 0) {
            return Result.fail("软删除失败，衣物可能不存在或已被删除");
        }
        return Result.success("衣物已软删除", null);
    }

    // ──────────────────────────────────────────────
    //  恢复已软删除的衣物
    // ──────────────────────────────────────────────
    @PutMapping("/clothes/{id}/restore")
    public Result<?> restoreClothing(@PathVariable Long id) {
        int rows = clothingItemMapper.restoreById(id);
        if (rows == 0) {
            return Result.fail("恢复失败，衣物可能不存在或未被删除");
        }
        return Result.success("衣物已恢复", null);
    }

    // ──────────────────────────────────────────────
    //  彻底删除衣物（物理删除）
    // ──────────────────────────────────────────────
    @DeleteMapping("/clothes/{id}/hard")
    public Result<?> hardDeleteClothing(@PathVariable Long id) {
        clothingItemMapper.hardDeleteById(id);
        return Result.success("衣物已彻底删除", null);
    }

    // ──────────────────────────────────────────────
    //  管理员软删除搭配方案（deleted=1）
    // ──────────────────────────────────────────────
    @PutMapping("/outfits/{id}/soft")
    public Result<?> softDeleteOutfit(@PathVariable Long id) {
        int rows = outfitPlanMapper.softDeleteById(id);
        if (rows == 0) {
            return Result.fail("软删除失败，搭配方案可能不存在或已被删除");
        }
        return Result.success("搭配方案已软删除", null);
    }

    // ──────────────────────────────────────────────
    //  恢复已软删除的搭配方案
    // ──────────────────────────────────────────────
    @PutMapping("/outfits/{id}/restore")
    public Result<?> restoreOutfit(@PathVariable Long id) {
        int rows = outfitPlanMapper.restoreById(id);
        if (rows == 0) {
            return Result.fail("恢复失败，搭配方案可能不存在或未被删除");
        }
        return Result.success("搭配方案已恢复", null);
    }

    // ──────────────────────────────────────────────
    //  彻底删除搭配方案（物理删除）
    // ──────────────────────────────────────────────
    @DeleteMapping("/outfits/{id}/hard")
    public Result<?> hardDeleteOutfit(@PathVariable Long id) {
        outfitPlanMapper.hardDeleteById(id);
        return Result.success("搭配方案已彻底删除", null);
    }

    // ──────────────────────────────────────────────
    //  冻结用户（软删除，deleted=1）
    // ──────────────────────────────────────────────
    @PutMapping("/users/{id}/freeze")
    public Result<?> freezeUser(@PathVariable Long id) {
        List<User> all = userMapper.selectAllIncludingDeleted();
        User target = all.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
        if (target == null) {
            return Result.fail("用户不存在");
        }
        if (target.getDeleted() == 1) {
            return Result.fail("该用户已处于冻结/注销状态");
        }

        int rows = userMapper.freezeById(id);
        if (rows == 0) {
            return Result.fail("冻结失败，用户状态可能已变更");
        }
        return Result.success("用户已冻结", null);
    }

    // ──────────────────────────────────────────────
    //  恢复已注销用户
    // ──────────────────────────────────────────────
    @PutMapping("/users/{id}/restore")
    public Result<?> restoreUser(@PathVariable Long id) {
        List<User> all = userMapper.selectAllIncludingDeleted();
        User target = all.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
        if (target == null) {
            return Result.fail("用户不存在");
        }
        if (target.getDeleted() == 0) {
            return Result.fail("该用户处于正常状态，无需恢复");
        }

        int rows = userMapper.restoreById(id);
        if (rows == 0) {
            return Result.fail("恢复失败，用户状态可能已变更");
        }
        return Result.success("用户已恢复", null);
    }

    // ──────────────────────────────────────────────
    //  彻底删除用户及关联数据（不可恢复）
    // ──────────────────────────────────────────────
    @DeleteMapping("/users/{id}")
    public Result<?> hardDeleteUser(@PathVariable Long id) {
        // 1. 物理删除该用户的所有搭配方案
        outfitPlanMapper.hardDeleteByUserId(id);
        // 2. 物理删除该用户的所有衣物
        clothingItemMapper.hardDeleteByUserId(id);
        // 3. 彻底删除用户（物理删除，绕过 @TableLogic）
        userMapper.hardDeleteById(id);

        return Result.success("用户及关联数据已彻底删除", null);
    }
}
