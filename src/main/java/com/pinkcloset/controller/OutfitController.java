package com.pinkcloset.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pinkcloset.common.Result;
import com.pinkcloset.entity.ClothingItem;
import com.pinkcloset.entity.OutfitPlan;
import com.pinkcloset.mapper.ClothingItemMapper;
import com.pinkcloset.mapper.OutfitPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搭配方案控制器
 *
 * GET     /api/outfits          获取当前用户的搭配方案（含衣物详情）
 * POST    /api/outfits           保存搭配方案
 * DELETE  /api/outfits/{id}     软删除搭配方案（普通用户）
 */
@Slf4j
@RestController
@RequestMapping("/api/outfits")
@RequiredArgsConstructor
public class OutfitController {

    private final OutfitPlanMapper outfitPlanMapper;
    private final ClothingItemMapper clothingItemMapper;

    /** 获取当前用户的搭配方案，并携带各件衣物详细信息 */
    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam Long userId) {
        List<OutfitPlan> plans = outfitPlanMapper.selectList(
                new LambdaQueryWrapper<OutfitPlan>()
                        .eq(OutfitPlan::getUserId, userId)
                        .orderByDesc(OutfitPlan::getCreateTime));

        List<Map<String, Object>> result = plans.stream().map(plan -> {
            Map<String, Object> vo = new HashMap<>();
            vo.put("id",          plan.getId());
            vo.put("name",        plan.getName());
            vo.put("note",        plan.getNote());
            vo.put("createTime",  plan.getCreateTime());
            vo.put("top",         fetchItem(plan.getTopId()));
            vo.put("bottom",      fetchItem(plan.getBottomId()));
            vo.put("shoes",       fetchItem(plan.getShoesId()));
            vo.put("accessory",   fetchItem(plan.getAccessoryId()));
            return vo;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    /** 保存搭配方案 */
    @PostMapping
    public Result<OutfitPlan> save(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        OutfitPlan plan = new OutfitPlan();
        plan.setUserId(userId);
        plan.setName(body.getOrDefault("name", "我的搭配").toString());
        plan.setTopId(body.get("topId") != null ? Long.valueOf(body.get("topId").toString()) : null);
        plan.setBottomId(body.get("bottomId") != null ? Long.valueOf(body.get("bottomId").toString()) : null);
        plan.setShoesId(body.get("shoesId") != null ? Long.valueOf(body.get("shoesId").toString()) : null);
        plan.setAccessoryId(body.get("accessoryId") != null ? Long.valueOf(body.get("accessoryId").toString()) : null);
        plan.setNote(body.get("note") != null ? body.get("note").toString() : null);

        outfitPlanMapper.insert(plan);
        return Result.success("搭配已保存 🎀", plan);
    }

    /**
     * 软删除搭配方案（普通用户）
     * 使用原生 SQL 设置 deleted=1，绕过 @TableLogic
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        int rows = outfitPlanMapper.softDeleteById(id);
        if (rows == 0) {
            return Result.fail("搭配方案不存在或已被删除");
        }
        return Result.success("搭配已删除", null);
    }

    // ─────────────────────────
    private ClothingItem fetchItem(Long id) {
        if (id == null) return null;
        return clothingItemMapper.selectById(id);
    }
}
