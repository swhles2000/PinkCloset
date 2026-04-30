# 粉色衣橱 PinkCloset 🌸

> 个人衣物管理与智能搭配系统 | Spring Boot + Vue 3 + MySQL

## 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [环境要求](#环境要求)
- [快速开始（通用）](#快速开始通用)
- [按系统部署运行](#按系统部署运行)
  - [Windows](#windows)
  - [macOS](#macos)
  - [Linux](#linux)
- [配置说明](#配置说明)
- [使用说明](#使用说明)
- [接口文档](#接口文档)
- [常见问题](#常见问题)

---

## 功能特性

- 👗 **衣物管理** — 上传、编辑、删除、按分类筛选（上衣/裤子/鞋子/配饰）
- ✨ **智能搭配** — 拖拽式搭配实验室 + 随机搭配 + 保存/复用方案
- 👤 **用户系统** — 注册、登录、个人资料、头像上传
- 🔐 **管理员后台** — 用户管理、冻结/恢复、衣物与搭配的全面管理
- 🛡️ **软删除** — 误删可恢复，管理员支持彻底删除
- 🎀 **少女心 UI** — 淡粉色渐变主题，精致交互体验

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 2.7.18 + MyBatis Plus 3.5.5 |
| 前端 | Vue 3.4 + Vite 5 + Vue Router 4 + Axios |
| 数据库 | MySQL 5.7+ / 8.0+ |
| 构建 | Maven（后端）+ npm（前端） |

---

## 项目结构

```
PinkCloset/
├── sql/
│   └── init.sql                          ← 数据库初始化脚本
├── src/main/java/com/pinkcloset/
│   ├── PinkClosetApplication.java        ← 启动类
│   ├── common/
│   │   └── Result.java                   ← 统一响应格式
│   ├── config/
│   │   ├── MybatisPlusFillHandler.java   ← 自动填充 create_time
│   │   └── WebConfig.java                ← 跨域 + 静态资源映射
│   ├── controller/
│   │   ├── AdminController.java          ← 管理员接口
│   │   ├── ClothingController.java       ← 衣物接口
│   │   ├── OutfitController.java         ← 搭配方案接口
│   │   └── UserController.java           ← 用户接口
│   ├── dto/                              ← 请求体定义
│   │   ├── AdminLoginRequest.java
│   │   ├── LoginRequest.java
│   │   └── RegisterRequest.java
│   ├── entity/                           ← 数据库实体
│   │   ├── ClothingItem.java
│   │   ├── OutfitPlan.java
│   │   ├── User.java
│   │   └── UserRoot.java
│   └── mapper/                           ← MyBatis Plus Mapper
│       ├── ClothingItemMapper.java
│       ├── OutfitPlanMapper.java
│       ├── UserMapper.java
│       └── UserRootMapper.java
├── src/main/resources/
│   └── application.yml                   ← 配置文件
├── frontend/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js
│       ├── App.vue                       ← 根组件（路由 + 导航栏）
│       ├── api.js                        ← Axios 封装
│       └── components/
│           ├── Login.vue                 ← 登录页
│           ├── Register.vue              ← 注册页
│           ├── Wardrobe.vue              ← 我的衣橱
│           ├── OutfitLab.vue             ← 搭配实验室
│           ├── Profile.vue               ← 个人中心
│           ├── AdminLogin.vue            ← 管理员登录
│           └── AdminPanel.vue            ← 管理员面板
├── pom.xml
└── uploads/                              ← 图片存储（自动创建）
```

---

## 环境要求

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| **JDK** | 11+（推荐 17） | 运行后端 |
| **Maven** | 3.6+ | 构建后端 |
| **MySQL** | 5.7+ / 8.0+ | 数据库 |
| **Node.js** | 16+ | 构建前端 |
| **npm** | 7+ | 前端包管理 |
| **Git** | 任意 | 克隆代码（可选） |

---

## 快速开始（通用）

无论你使用哪个操作系统，整体流程都是 4 步：

1. **获取代码** — 克隆或下载项目
2. **初始化数据库** — 导入 SQL 脚本
3. **启动后端** — 配置并运行 Spring Boot
4. **启动前端** — 安装依赖并启动 Vite 开发服务器

下面按 Windows / macOS / Linux 分别给出具体步骤。

---

## 按系统部署运行

### Windows

#### 0. 安装必要工具（如尚未安装）

| 工具 | 推荐安装方式 |
|------|------------|
| JDK 17 | 下载 [Adoptium JDK 17](https://adoptium.net/)，安装后验证 `java -version` |
| Maven | 下载 [Maven 3.9](https://maven.apache.org/download.cgi)，配置 `PATH` 环境变量 |
| MySQL 8.0 | 下载 [MySQL Installer](https://dev.mysql.com/downloads/installer/)，安装时记住 root 密码 |
| Node.js | 下载 [Node.js LTS](https://nodejs.org/)，自带 npm |
| Git | 下载 [Git for Windows](https://git-scm.com/download/win) |

#### 1. 获取代码

```powershell
# 方式一：克隆仓库
git clone https://github.com/swhles2000/PinkCloset.git
cd PinkCloset

# 方式二：直接用 IDEA 打开项目目录
```

#### 2. 初始化数据库

打开 **MySQL 命令行**（开始菜单搜索 "MySQL 8.0 Command Line Client"）或 Navicat / DataGrip：

```sql
-- 方式一：命令行导入
source C:/path/to/PinkCloset/sql/init.sql

-- 方式二：在 Navicat/DataGrip 中打开 sql/init.sql 文件并执行
```

脚本会自动创建数据库 `pink_closet_db`、4 张表和默认管理员账号。

#### 3. 配置后端

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pink_closet_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true
    username: root
    password: 123456          # ← 改成你的 MySQL 密码

upload:
  path: C:/Users/你的用户名/PinkCloset/uploads/   # ← 改成你的实际路径（注意结尾斜杠）
```

#### 4. 启动后端

**方式一：IDEA 运行（推荐）**

1. 用 IntelliJ IDEA 打开项目根目录
2. 等待 Maven 自动下载依赖（首次约 2-3 分钟）
3. 找到 `PinkClosetApplication.java` → 右键 → **Run**

**方式二：命令行**

```powershell
cd C:\path\to\PinkCloset

# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/pink-closet-1.0.0.jar
```

验证后端：浏览器访问 http://localhost:8080/api/clothes ，返回 JSON 数据即成功。

#### 5. 启动前端

```powershell
cd C:\path\to\PinkCloset\frontend

# 安装依赖（首次）
npm install

# 启动开发服务器
npm run dev
```

浏览器打开 **http://localhost:5173** 即可使用 🌸

---

### macOS

#### 0. 安装必要工具（推荐使用 Homebrew）

```bash
# 安装 Homebrew（如尚未安装）
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装所有依赖
brew install openjdk@17
brew install maven
brew install mysql
brew install node
brew install git

# 配置 JDK 符号链接（使 java 命令可用）
sudo ln -sfn $(brew --prefix openjdk@17)/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

#### 1. 获取代码

```bash
git clone https://github.com/swhles2000/PinkCloset.git
cd PinkCloset
```

#### 2. 初始化数据库

```bash
# 启动 MySQL 服务
brew services start mysql

# 首次运行需设置 root 密码（按提示操作）
mysql_secure_installation

# 导入初始化脚本
mysql -u root -p < sql/init.sql
```

> 如果 MySQL 版本为 8.4+（Homebrew 默认），`mysql_secure_installation` 可能不可用，可跳过此步，直接用空密码连接。

#### 3. 配置后端

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pink_closet_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true
    username: root
    password: 123456          # ← 改成你的 MySQL 密码

upload:
  path: /Users/你的用户名/PinkCloset/uploads/   # ← macOS 路径格式（注意结尾斜杠）
```

#### 4. 启动后端

**方式一：使用 IDE**

用 IntelliJ IDEA（[下载](https://www.jetbrains.com/idea/download/)）打开项目，找到 `PinkClosetApplication.java` → Run。

**方式二：命令行**

```bash
cd ~/PinkCloset

# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/pink-closet-1.0.0.jar
```

验证：浏览器访问 http://localhost:8080/api/clothes 返回 JSON 数据即成功。

#### 5. 启动前端

```bash
cd ~/PinkCloset/frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

浏览器打开 **http://localhost:5173** 即可使用 🌸

---

### Linux（Ubuntu / Debian）

#### 0. 安装必要工具

```bash
# 更新包列表
sudo apt update && sudo apt upgrade -y

# 安装 JDK 17
sudo apt install -y openjdk-17-jdk

# 安装 Maven
sudo apt install -y maven

# 安装 MySQL 8.0
sudo apt install -y mysql-server

# 安装 Node.js 20.x（推荐通过 NodeSource）
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# 安装 Git
sudo apt install -y git
```

> **CentOS / RHEL / Fedora 用户**：将 `apt` 替换为 `dnf` / `yum`，Node.js 可使用 `dnf module install nodejs:20`。

#### 1. 获取代码

```bash
git clone https://github.com/swhles2000/PinkCloset.git
cd PinkCloset
```

#### 2. 初始化数据库

```bash
# 启动 MySQL 服务
sudo systemctl start mysql
sudo systemctl enable mysql    # 开机自启

# 安全初始化（设置 root 密码等）
sudo mysql_secure_installation

# 导入初始化脚本
mysql -u root -p < sql/init.sql
```

> 如果使用 MySQL 8.4+（Ubuntu 24.04+ 默认），root 默认使用 `auth_socket` 认证，需要先切换认证方式或创建新用户。可以执行：
> ```bash
> sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123456'; FLUSH PRIVILEGES;"
> ```

#### 3. 配置后端

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pink_closet_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true
    username: root
    password: 123456          # ← 改成你的 MySQL 密码

upload:
  path: /home/你的用户名/PinkCloset/uploads/   # ← Linux 路径格式（注意结尾斜杠）
```

> **重要**：确保 `upload.path` 对应的目录对运行后端的用户有写权限：
> ```bash
> mkdir -p /home/你的用户名/PinkCloset/uploads/
> chmod 755 /home/你的用户名/PinkCloset/uploads/
> ```

#### 4. 启动后端

**方式一：使用 IDE**

通过 SSH 远程开发或安装 IntelliJ IDEA（[下载](https://www.jetbrains.com/idea/download/#section=linux)），找到 `PinkClosetApplication.java` → Run。

**方式二：命令行（推荐）**

```bash
cd ~/PinkCloset

# 编译打包
mvn clean package -DskipTests

# 运行（前台运行，关闭终端即停止）
java -jar target/pink-closet-1.0.0.jar

# 或者后台运行
nohup java -jar target/pink-closet-1.0.0.jar > app.log 2>&1 &
```

验证：`curl http://localhost:8080/api/clothes` 返回 JSON 数据即成功。

#### 5. 启动前端

```bash
cd ~/PinkCloset/frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 后台运行
nohup npm run dev > frontend.log 2>&1 &
```

浏览器打开 **http://localhost:5173** 即可使用 🌸

---

## 配置说明

### application.yml 关键配置项

| 配置项 | 默认值 | 说明 |
|-------|--------|------|
| `server.port` | `8080` | 后端端口 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/pink_closet_db...` | 数据库连接地址 |
| `spring.datasource.username` | `root` | 数据库用户名 |
| `spring.datasource.password` | `123456` | 数据库密码 |
| `upload.path` | `D:/JavaProject/Clothes/uploads/` | 图片上传存储路径 |
| `spring.servlet.multipart.max-file-size` | `10MB` | 单文件上传大小限制 |
| `spring.servlet.multipart.max-request-size` | `20MB` | 单次请求总大小限制 |

> ⚠️ **跨平台注意事项**：`upload.path` 必须使用操作系统的路径格式（Windows 用 `D:/...`，macOS/Linux 用 `/...`），且路径末尾必须有斜杠。

### Vite 代理配置

`frontend/vite.config.js` 中配置了开发环境代理：

```javascript
proxy: {
  '/api':     { target: 'http://localhost:8080', changeOrigin: true },
  '/uploads': { target: 'http://localhost:8080', changeOrigin: true }
}
```

如果后端端口不是 8080，需同步修改此处。

---

## 使用说明

### 注册与登录

1. 访问 http://localhost:5173 ，进入登录页面
2. 点击「注册账号」创建新账号（手机号 + 密码）
3. 密码要求：至少 8 位，包含大写字母、小写字母、数字和特殊字符

### 管理员入口

- 管理员登录页：http://localhost:5173/admin/login
- 默认管理员账号：`root` / `Swh2000@..`

### 添加衣物

1. 登录后，点击顶部导航「🗂 我的衣橱」
2. 点击右上角「+ 添加衣物」
3. 上传图片（拖拽或点击），填写名称、分类、颜色和风格
4. 点击「上传衣物 🌸」

### 编辑衣物

1. 在衣橱页面，鼠标悬停在衣物卡片上
2. 点击「✏️ 编辑」按钮
3. 修改信息后点击「保存修改」

### 搭配实验室

1. 点击顶部导航「✨ 搭配实验室」
2. 点击下方衣物列表中的衣物 → 自动填入对应类别的槽位
3. 也可点击槽位本身，弹出该类别的快速选择弹窗
4. 点击「🎲 随机搭配」自动填满所有槽位
5. 搭配满意后点击「💾 保存此搭配」

---

## 接口文档

### 用户接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user/register` | 注册 |
| POST | `/api/user/login` | 登录 |
| GET | `/api/user/info` | 获取当前用户信息 |
| PUT | `/api/user/info` | 修改个人信息 |
| POST | `/api/user/avatar` | 上传头像 |

### 衣物接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/clothes` | 获取衣物列表（`?category=TOP` 筛选） |
| POST | `/api/clothes` | 上传衣物（multipart/form-data） |
| PUT | `/api/clothes/{id}` | 修改衣物信息 |
| DELETE | `/api/clothes/{id}` | 删除衣物（软删除） |

### 搭配方案接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/outfits` | 获取搭配方案列表 |
| POST | `/api/outfits` | 保存搭配方案（JSON） |
| DELETE | `/api/outfits/{id}` | 删除搭配方案（软删除） |

### 管理员接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/login` | 管理员登录 |
| GET | `/api/admin/users` | 获取所有用户 |
| PUT | `/api/admin/users/{id}/freeze` | 冻结用户 |
| PUT | `/api/admin/users/{id}/restore` | 恢复用户 |
| DELETE | `/api/admin/users/{id}` | 彻底删除用户 |

---

## 常见问题

### Q: 后端报 `Unknown database 'pink_closet_db'`
尚未执行 SQL 初始化脚本，请先完成数据库初始化步骤。

### Q: 后端报 `Access denied for user 'root'@'localhost'`
MySQL 密码不是 `123456`，请修改 `application.yml` 中的 `password` 字段。

### Q: 前端报 `Network Error` / 接口 404
确认后端已在 8080 端口正常运行。Vite 的代理会自动转发 `/api` 和 `/uploads` 请求。

### Q: 上传图片后图片不显示
1. 检查 `application.yml` 中的 `upload.path` 路径是否存在且有写权限
2. Windows 路径格式：`D:/path/to/uploads/`（用正斜杠，结尾有斜杠）
3. macOS/Linux 路径格式：`/home/user/PinkCloset/uploads/`

### Q: macOS/Linux 下启动后端报 `java: command not found`
JDK 未正确安装或未配置环境变量：
```bash
# 验证安装
java -version

# macOS (Homebrew)
echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Linux
sudo update-alternatives --config java
```

### Q: Linux 上 MySQL 8.4+ 无法用密码登录
MySQL 8.4+ 默认使用 `caching_sha2_password` 认证，需手动设置：
```bash
sudo mysql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '你的密码';
FLUSH PRIVILEGES;
EXIT;
```

### Q: 端口冲突
- 后端改端口：修改 `application.yml` 中 `server.port`
- 前端改端口：修改 `vite.config.js` 中 `server.port`，并同步更新 `proxy.target`

### Q: Maven 下载依赖很慢
配置国内镜像源，编辑 `~/.m2/settings.xml`（Windows 为 `C:\Users\用户名\.m2\settings.xml`）：
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

### Q: npm install 很慢或失败
配置国内镜像源：
```bash
npm config set registry https://registry.npmmirror.com
```

---

*Made with 💗 · [PinkCloset 粉色衣橱](https://github.com/swhles2000/PinkCloset)*
