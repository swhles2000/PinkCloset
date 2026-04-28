package com.pinkcloset.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pinkcloset.common.Result;
import com.pinkcloset.entity.ClothingItem;
import com.pinkcloset.mapper.ClothingItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 衣物管理控制器
 *
 * GET    /api/clothes              获取衣物列表（可按 category 筛选）
 * POST   /api/clothes              上传衣物（含图片）
 * DELETE /api/clothes/{id}         删除衣物
 */
@Slf4j
@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothingController {

    private final ClothingItemMapper clothingItemMapper;

    /** 图片保存根目录，来自 application.yml */
    @Value("${upload.path}")
    private String uploadPath;

    /** 后端服务地址（用于拼接图片访问 URL） */
    @Value("${server.port:8080}")
    private String serverPort;

    // ─────────────────────────────────────────────────
    //  GET /api/clothes  获取衣物列表
    //  可选参数：category（TOP / BOTTOM / SHOES / ACCESSORY）
    // ─────────────────────────────────────────────────
    @GetMapping
    public Result<List<ClothingItem>> list(
            @RequestParam(required = false) String category) {

        LambdaQueryWrapper<ClothingItem> wrapper = new LambdaQueryWrapper<>();

        // 若传入分类参数则进行过滤
        if (StringUtils.hasText(category)) {
            wrapper.eq(ClothingItem::getCategory, category.toUpperCase());
        }

        // 按上传时间降序排列
        wrapper.orderByDesc(ClothingItem::getCreateTime);

        List<ClothingItem> items = clothingItemMapper.selectList(wrapper);
        return Result.success(items);
    }

    // ─────────────────────────────────────────────────
    //  POST /api/clothes  上传衣物
    //  支持 multipart/form-data，含图片文件
    // ─────────────────────────────────────────────────
    @PostMapping
    public Result<ClothingItem> upload(
            @RequestParam("name")           String name,
            @RequestParam("category")       String category,
            @RequestParam(value = "color",  required = false) String color,
            @RequestParam(value = "style",  required = false) String style,
            @RequestParam(value = "image",  required = false) MultipartFile image) {

        ClothingItem item = new ClothingItem();
        item.setName(name);
        item.setCategory(category.toUpperCase());
        item.setColor(color);
        item.setStyle(style);

        // 处理图片上传
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = saveImage(image);
                item.setImageUrl(imageUrl);
            } catch (IOException e) {
                log.error("图片上传失败", e);
                return Result.fail("图片上传失败：" + e.getMessage());
            }
        }

        clothingItemMapper.insert(item);
        log.info("衣物上传成功：{}", item);
        return Result.success("上传成功", item);
    }

    // ─────────────────────────────────────────────────
    //  DELETE /api/clothes/{id}  删除衣物
    // ─────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ClothingItem item = clothingItemMapper.selectById(id);
        if (item == null) {
            return Result.fail("衣物不存在");
        }

        // 同时删除本地图片文件
        if (StringUtils.hasText(item.getImageUrl())) {
            deleteLocalFile(item.getImageUrl());
        }

        clothingItemMapper.deleteById(id);
        log.info("衣物删除成功，ID={}", id);
        return Result.success("删除成功", null);
    }

    // ═══════════════════════════════════════
    //  私有工具方法
    // ═══════════════════════════════════════

    /**
     * 将上传的图片文件保存到本地磁盘
     * 返回前端可访问的相对 URL 路径，如 /uploads/2026/04/abc.jpg
     */
    private String saveImage(MultipartFile file) throws IOException {
        // 按日期建子目录，避免单目录文件过多
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String dir = uploadPath + datePart + File.separator;

        File directory = new File(dir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".jpg";
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        file.transferTo(new File(dir + fileName));

        // 返回前端访问路径
        return "/uploads/" + datePart + "/" + fileName;
    }

    /**
     * 删除本地图片文件
     * imageUrl 格式：/uploads/2026/04/xxx.jpg
     */
    private void deleteLocalFile(String imageUrl) {
        // 去掉开头 "/" 拼接本地路径
        String relativePath = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;
        // uploadPath 已是根目录（含尾部斜线），只需替换 uploads/ 前缀
        String localPath = uploadPath + relativePath.replaceFirst("^uploads/", "");
        File file = new File(localPath);
        if (file.exists()) {
            boolean deleted = file.delete();
            log.info("删除图片文件 {} {}", localPath, deleted ? "成功" : "失败");
        }
    }
}
