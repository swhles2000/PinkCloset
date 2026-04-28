package com.pinkcloset.controller;

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

/**
 * 搭配方案控制器
 *
 * GET  /api/outfits          获取所有保存的搭配方案（含衣物详情）
 * POST /api/outfits           保存一套搭配方案
 * DELETE /api/outfits/{id}   删除搭配方案
 */
@Slf4j
@RestController
@RequestMapping("/api/outfits")
@RequiredArgsConstructor
public class OutfitController {

    private final OutfitPlanMapper outfitPlanMapper;
    private final ClothingItemMapper clothingItemMapper;

    /** 获取所有搭配方案，并携带各件衣物详细信息 */
    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        List<OutfitPlan> plans = outfitPlanMapper.selectList(null);

        // 为每个方案填充衣物详情，方便前端直接渲染
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
        }).collect(java.util.stream.Collectors.toList());

        return Result.success(result);
    }

    /** 保存搭配方案 */
    @PostMapping
    public Result<OutfitPlan> save(@RequestBody OutfitPlan plan) {
        outfitPlanMapper.insert(plan);
        return Result.success("搭配已保存 🎀", plan);
    }

    /** 删除搭配方案 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        outfitPlanMapper.deleteById(id);
        return Result.success("搭配已删除", null);
    }

    // ─────────────────────────
    private ClothingItem fetchItem(Long id) {
        if (id == null) return null;
        return clothingItemMapper.selectById(id);
    }
}
