package com.pinkcloset.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pinkcloset.common.Result;
import com.pinkcloset.dto.LoginRequest;
import com.pinkcloset.dto.RegisterRequest;
import com.pinkcloset.entity.User;
import com.pinkcloset.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户接口：注册 + 登录 + 注销 + 头像上传
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    /** 图片保存根目录 */
    @Value("${upload.path}")
    private String uploadPath;

    // ──────────────────────────────────────────────
    //  注册
    // ──────────────────────────────────────────────
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest req) {

        // 1. 必填校验
        if (!StringUtils.hasText(req.getName())) {
            return Result.fail("姓名不能为空");
        }
        if (!StringUtils.hasText(req.getPhone())) {
            return Result.fail("手机号不能为空");
        }
        if (!StringUtils.hasText(req.getPassword())) {
            return Result.fail("密码不能为空");
        }
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            return Result.fail("两次密码输入不一致");
        }
        // 密码强度：至少8位，含大写字母、小写字母、数字、特殊字符
        if (!isStrongPassword(req.getPassword())) {
            return Result.fail("密码需包含大写字母、小写字母、数字及特殊字符，且至少 8 位");
        }

        // 2. 手机号格式简单校验（11位数字）
        if (!req.getPhone().matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式不正确");
        }

        // 3. 邮箱格式校验（如果填了）
        if (StringUtils.hasText(req.getEmail()) &&
                !req.getEmail().matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            return Result.fail("邮箱格式不正确");
        }

        // 4. 检查手机号是否已注册（包含已注销用户，注销后不释放）
        //    @TableLogic 会自动加 deleted=0，这里需要手动用原生 SQL 或绕过
        Long phoneCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, req.getPhone())
                        .last("OR deleted = 1"));
        if (phoneCount > 0) {
            return Result.fail("该手机号已被注册（包括已注销账号），不可重复使用");
        }

        // 5. 检查邮箱是否已注册（同上，包含已注销用户）
        if (StringUtils.hasText(req.getEmail())) {
            Long emailCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getEmail, req.getEmail())
                            .last("OR deleted = 1"));
            if (emailCount > 0) {
                return Result.fail("该邮箱已被注册（包括已注销账号），不可重复使用");
            }
        }

        // 6. 密码明文存储
        String pwd = req.getPassword();

        // 7. 构建并保存
        User user = new User();
        user.setName(req.getName());
        user.setGender(req.getGender());
        user.setAge(req.getAge());
        user.setPhone(req.getPhone());
        user.setEmail(StringUtils.hasText(req.getEmail()) ? req.getEmail() : null);
        user.setPassword(pwd);
        user.setDeleted(0);

        userMapper.insert(user);
        return Result.success("注册成功", null);
    }

    // ──────────────────────────────────────────────
    //  登录
    // ──────────────────────────────────────────────
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest req) {

        if (!StringUtils.hasText(req.getAccount())) {
            return Result.fail("账号不能为空");
        }
        if (!StringUtils.hasText(req.getPassword())) {
            return Result.fail("密码不能为空");
        }

        // 判断账号是手机号还是邮箱
        boolean isPhone = req.getAccount().matches("^1[3-9]\\d{9}$");
        boolean isEmail = req.getAccount().contains("@");

        User user = null;
        if (isPhone) {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, req.getAccount()));
        } else if (isEmail) {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, req.getAccount()));
        } else {
            return Result.fail("请输入有效的手机号或邮箱");
        }

        if (user == null) {
            return Result.fail("账号不存在");
        }

        // 比对密码（明文比对）
        if (!req.getPassword().equals(user.getPassword())) {
            return Result.fail("密码错误");
        }

        // 登录成功：返回用户基本信息（密码已被 @JsonIgnore 屏蔽）
        Map<String, Object> data = new HashMap<>();
        data.put("id",     user.getId());
        data.put("name",   user.getName());
        data.put("phone",  user.getPhone());
        data.put("email",  user.getEmail());
        data.put("gender", user.getGender());
        data.put("age",    user.getAge());
        data.put("avatar", user.getAvatar());

        return Result.success("登录成功", data);
    }

    // ──────────────────────────────────────────────
    //  注销账号（软删除：UPDATE deleted=1）
    // ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public Result<?> deleteAccount(@PathVariable Long id) {
        // @TableLogic 的 selectById 会自动加 deleted=0，查不到已注销的
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail("用户不存在或已注销");
        }
        // 软删除：标记 deleted=1，不真删
        userMapper.update(null,
                new LambdaUpdateWrapper<User>()
                        .eq(User::getId, id)
                        .set(User::getDeleted, 1));
        return Result.success("账号已注销", null);
    }

    // ──────────────────────────────────────────────
    //  上传 / 更新用户头像
    // ──────────────────────────────────────────────
    @PostMapping("/avatar")
    public Result<?> uploadAvatar(@RequestParam("userId") Long userId,
                                   @RequestParam("avatar") MultipartFile avatar) {
        // 校验用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (avatar == null || avatar.isEmpty()) {
            return Result.fail("请选择头像图片");
        }

        // 校验文件类型
        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail("只能上传图片文件");
        }

        // 限制文件大小 5MB
        if (avatar.getSize() > 5 * 1024 * 1024) {
            return Result.fail("头像图片不能超过 5MB");
        }

        try {
            // 保存头像到 avatar 子目录
            String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String dir = uploadPath + "avatar" + File.separator + datePart + File.separator;
            File directory = new File(dir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalName = avatar.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

            avatar.transferTo(new File(dir + fileName));

            String avatarUrl = "/uploads/avatar/" + datePart + "/" + fileName;

            // 删除旧头像文件
            if (StringUtils.hasText(user.getAvatar())) {
                deleteLocalFile(user.getAvatar());
            }

            // 更新数据库
            userMapper.update(null,
                    new LambdaUpdateWrapper<User>()
                            .eq(User::getId, userId)
                            .set(User::getAvatar, avatarUrl));

            log.info("用户 {} 头像更新成功：{}", userId, avatarUrl);
            return Result.success("头像上传成功", avatarUrl);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.fail("头像上传失败：" + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    //  密码强度校验：大写 + 小写 + 数字 + 特殊字符，且 >= 8 位
    // ──────────────────────────────────────────────
    private boolean isStrongPassword(String pwd) {
        if (pwd == null || pwd.length() < 8) return false;
        boolean hasUpper   = pwd.chars().anyMatch(Character::isUpperCase);
        boolean hasLower   = pwd.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit   = pwd.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = pwd.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;':\",./<>?`~\\".indexOf(c) >= 0);
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    // ──────────────────────────────────────────────
    //  删除本地图片文件
    // ──────────────────────────────────────────────
    private void deleteLocalFile(String imageUrl) {
        // imageUrl 格式：/uploads/avatar/2026/04/xxx.jpg 或 /uploads/2026/04/xxx.jpg
        String relativePath = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;
        String localPath = uploadPath + relativePath.replaceFirst("^uploads/", "");
        File file = new File(localPath);
        if (file.exists()) {
            boolean deleted = file.delete();
            log.info("删除文件 {} {}", localPath, deleted ? "成功" : "失败");
        }
    }
}
