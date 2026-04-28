# 粉色衣橱 PinkCloset 🌸 — 本地运行指南

## 目录结构

```
Clothes/
├── sql/
│   └── init.sql                    ← 数据库初始化脚本
├── src/
│   └── main/
│       ├── java/com/pinkcloset/
│       │   ├── PinkClosetApplication.java
│       │   ├── common/Result.java
│       │   ├── config/
│       │   │   ├── MybatisPlusFillHandler.java
│       │   │   └── WebConfig.java
│       │   ├── controller/
│       │   │   ├── ClothingController.java
│       │   │   └── OutfitController.java
│       │   ├── entity/
│       │   │   ├── ClothingItem.java
│       │   │   └── OutfitPlan.java
│       │   └── mapper/
│       │       ├── ClothingItemMapper.java
│       │       └── OutfitPlanMapper.java
│       └── resources/
│           └── application.yml
├── frontend/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── api.js
│       └── components/
│           ├── OutfitLab.vue       ← 搭配实验室（核心）
│           └── Wardrobe.vue        ← 我的衣橱
├── pom.xml
└── uploads/                        ← 图片存储目录（自动创建）
```

---

## 环境要求

| 工具        | 版本要求          |
|-----------|---------------|
| JDK       | 11+           |
| Maven     | 3.6+          |
| MySQL     | 5.7+ / 8.0+   |
| Node.js   | 16+           |
| npm       | 7+            |

---

## Step 1 — 初始化数据库

打开 MySQL 客户端（Navicat / DataGrip / 命令行），执行：

```sql
-- 直接运行 sql/init.sql 文件
source D:/JavaProject/Clothes/sql/init.sql
```

或复制 `sql/init.sql` 内容粘贴执行。

脚本会自动完成：
- 创建数据库 `pink_closet_db`
- 创建 `clothing_item` 和 `outfit_plan` 两张表
- 插入 11 条示例数据（方便立即体验）

---

## Step 2 — 启动后端（Spring Boot）

### 2.1 在 IDE 中运行（推荐）

1. 用 IntelliJ IDEA 打开 `D:/JavaProject/Clothes` 目录
2. 等待 Maven 自动下载依赖
3. 找到 `PinkClosetApplication.java`，右键 → **Run**

### 2.2 用命令行运行

```powershell
cd D:/JavaProject/Clothes

# 编译并打包
mvn clean package -DskipTests

# 运行
java -jar target/pink-closet-1.0.0.jar
```

### 2.3 验证后端

后端默认监听 **http://localhost:8080**

```powershell
# 测试接口是否正常（PowerShell）
Invoke-RestMethod http://localhost:8080/api/clothes
```

若返回 `{ code: 200, data: [...] }` 则后端启动成功 ✅

---

## Step 3 — 启动前端（Vue + Vite）

```powershell
cd D:/JavaProject/Clothes/frontend

# 安装依赖（首次需要，约1~2分钟）
npm install

# 启动开发服务器
npm run dev
```

启动成功后，控制台会显示：

```
  VITE ready in xxx ms

  ➜  Local:   http://localhost:5173/
```

打开浏览器访问 **http://localhost:5173** 即可看到粉色衣橱界面 🌸

---

## Step 4 — 使用说明

### 添加衣物
1. 点击顶部导航「🗂 我的衣橱」
2. 点击右上角「+ 添加衣物」
3. 上传图片（拖拽或点击），填写名称和分类
4. 点击「上传衣物 🌸」

### 搭配实验室
1. 点击顶部导航「✨ 搭配实验室」
2. 下方衣物列表 → 点击某件衣物 → 自动填入对应类别的槽位
3. 也可点击槽位本身，弹出该类别的快速选择弹窗
4. 点击槽位右上角「✕」可清空单个槽位
5. 点击「🎲 随机搭配」自动填满所有槽位
6. 搭配满意后点击「💾 保存此搭配」，起名保存
7. 已保存的搭配在下方横向滚动展示，可点「🔄 应用」重新加载到槽位

---

## 常见问题

### Q: 后端报 `Unknown database 'pink_closet_db'`
**A:** 尚未执行 SQL 初始化脚本，请先完成 Step 1。

### Q: 后端报 `Access denied for user 'root'@'localhost'`
**A:** MySQL 密码不是 `123456`，请修改 `src/main/resources/application.yml` 中的 `password` 字段。

### Q: 前端报 `Network Error` / 接口 404
**A:** 确认后端已在 8080 端口正常运行。Vite 的 `/api` 代理会自动转发请求，无需修改前端代码。

### Q: 上传图片后图片不显示
**A:** 检查 `application.yml` 中的 `upload.path` 路径是否存在写权限。Windows 上路径格式为 `D:/JavaProject/Clothes/uploads/`（结尾需有斜杠）。

### Q: 端口冲突
**A:** 
- 后端改端口：修改 `application.yml` 中 `server.port`
- 前端改端口：修改 `vite.config.js` 中 `server.port`，并同步更新代理 target

---

## 接口文档速查

| 方法     | 路径                    | 说明               |
|--------|-------------------------|------------------|
| GET    | /api/clothes            | 获取衣物列表（支持 ?category= 筛选） |
| POST   | /api/clothes            | 上传衣物（multipart/form-data） |
| DELETE | /api/clothes/{id}       | 删除衣物            |
| GET    | /api/outfits            | 获取所有搭配方案       |
| POST   | /api/outfits            | 保存搭配方案（JSON）   |
| DELETE | /api/outfits/{id}       | 删除搭配方案         |

上传衣物的 form-data 字段：

| 字段       | 类型   | 必填 | 说明                             |
|----------|--------|------|--------------------------------|
| name     | string | ✅   | 衣物名称                          |
| category | string | ✅   | TOP / BOTTOM / SHOES / ACCESSORY |
| color    | string |      | 主色调                           |
| style    | string |      | 风格标签（逗号分隔）                 |
| image    | file   |      | 图片文件（jpg/png/webp）           |

---

*Made with 💗 · PinkCloset 粉色衣橱*
