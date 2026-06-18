# JellyStudy 项目结构文档

## 一、项目概览

JellyStudy 是一个**知识问答 + AI 辅助 + 游戏化激励**的学习平台，
采用前后端分离架构，Docker Compose 容器化部署。

```
11_2/
├── JellyStudy/                 ← 后端（Spring Boot 多模块）
│   ├── jelly-study/            ← 主服务模块
│   ├── jelly-study-api/        ← 共享 API 接口
│   ├── jelly-study-evaluation/ ← 评估服务
│   └── docker-compose.yml      ← 容器编排
├── qianduan/                   ← 前端（原生 HTML/CSS/JS SPA）
└── docs/                       ← 设计文档
```

---

## 二、后端架构（JellyStudy）

### 2.1 三层分层

```
Controller  →  Service  →  Repository  →  MongoDB
   (API)        (业务)       (数据访问)      (数据库)
```

每个功能模块都遵循 `Controller → Service → ServiceImpl → Repository` 结构。

### 2.2 模块清单

| 模块 | 职责 |
|------|------|
| **用户模块** | 注册登录、个人资料、头像上传 |
| **问答模块** | 提问、回答、点赞、采纳、评论 |
| **知识点模块** | 知识树 CRUD、按知识点筛选问题 |
| **AI 模块** | DeepSeek 大模型问答、角色扮演、题库生成 |
| **用户中心** | 关注/粉丝、收藏、浏览历史、称号系统 |
| **信用 & 抽卡** | 信用点经济、抽卡奖池、碎片合成、装饰佩戴 |
| **题库模块** | AI 生成题目、浏览/闯关刷题 |
| **消息模块** | 私信、系统通知、异步推送 |
| **评估模块** | 问答质量 AI 评价（独立服务） |

### 2.3 Controller 一览

| Controller | 路径前缀 | 功能 |
|------------|----------|------|
| `UserController` | `/api/users` | 用户 CRUD、个人资料、头像、关注/取关 |
| `QuestionController` | `/api/questions` | 问题 CRUD、搜索、点赞、相似推荐 |
| `AnswerController` | `/api/answers` | 回答 CRUD、点赞、采纳 |
| `AIController` | `/api/ai` | AI 问答（支持 persona 角色扮演） |
| `CreditController` | `/api/credits` | 信用点查询/赚取、抽卡、碎片、装饰 |
| `QuestionBankController` | `/api/question-bank` | 题库 CRUD、AI 生成题目、闯关/浏览 |
| `KnowledgePointController` | `/api/knowledge-points` | 知识点树 CRUD |
| `NotificationController` | `/api/notifications` | 系统通知 |
| `MessageController` | `/api/messages` | 私信 |
| `CommentController` | `/api/comments` | 评论 |
| `NacosConfigController` | `/api/config` | 配置管理（当前未启用） |
| `RedisController` | `/api/redis` | 缓存测试 |

---

## 三、数据存储 — MongoDB 集合

数据库名：`jelly_study`（配置在 `application.yml`，默认 localhost:27017）

| 集合 | 对应实体 | 存放内容 |
|------|----------|----------|
| `users` | `User` | 用户账号、积分、称号 |
| `questions` | `Question` | 问题标题、内容、标签、状态 |
| `answers` | `Answer` | 回答内容、AI 评分、点赞数 |
| `comments` | `Comment` | 评论 |
| `knowledge_points` | `KnowledgePoint` | 知识点树 |
| `follows` | `Follow` | 关注关系 |
| `favorites` | `Favorite` | 收藏的问题 |
| `browse_history` | `BrowseHistory` | 最近浏览（每人最多 20 条） |
| `gacha_items` | `GachaItem` | 抽卡奖池物品定义 |
| `user_fragments` | `UserFragment` | 用户拥有的碎片 |
| `user_decorations` | `UserDecoration` | 用户已合成的装饰品 |
| `question_bank_items` | `QuestionBankItem` | 题库题目（AI 生成 + 手动） |
| `notifications` | `Notification` | 系统通知 |
| `messages` | `Message` | 私信记录 |

---

## 四、基础设施服务

| 服务 | 端口 | 用途 |
|------|------|------|
| **MongoDB** | 27017（宿主机） | 主数据库，存所有业务数据 |
| **Redis** | 6379（容器） | 缓存热门问题、知识点树、称号状态 |
| **RabbitMQ** | 5672 / 15672（容器） | 异步消息：通知推送、信用点计算 |
| **DeepSeek API** | 外部 HTTPS | AI 问答、题库生成、角色扮演 |

> ⚠️ MongoDB 跑在宿主机（通过 `host.docker.internal` 访问），其余中间件在 Docker 容器中。

---

## 五、数据流转

### 5.1 典型请求链路

```
浏览器 (localhost:8088)
   │
   │  fetch('/api/questions?page=1')
   ▼
Nginx 前端容器 (8088) ──静态文件──▶ index.html / app.js / *.css
   │
   │  API 请求直连后端（API_BASE_URL = http://localhost:8086）
   ▼
Spring Boot (8086 或 8087)
   │
   ├── Controller  接收请求、校验参数
   ├── Service     执行业务逻辑
   ├── Repository  操作 MongoDB
   └── 返回 JSON   { code: 200, data: {...} }
   │
   ▼
前端渲染 / 更新 DOM
```

### 5.2 AI 问答流程

```
用户输入问题
   │
   ▼
AIController.answer()
   │
   ├── 从 PersonaPrompts 获取对应人设 System Prompt
   ├── 拼接 user prompt（问题标题 + 内容）
   ├── POST 请求 → DeepSeek API
   ├── 解析 JSON 响应 → 提取 answer 文本
   └── 返回给前端渲染气泡
```

### 5.3 抽卡流程

```
用户点击"单抽"
   │
   ▼
CreditController.gachaPull(userId, times=1)
   │
   ├── 扣 160 信用点
   ├── 按概率随机稀有度（30%/20%/20%/20%/10%）
   ├── 保底检查（10 抽保底精良+，20 抽保底史诗+）
   ├── 从奖池该稀有度中随机选一件
   ├── 1% 概率直接出本体 → 写入 user_decorations
   └── 99% 概率出碎片 → 写入 user_fragments（叠加数量）
```

### 5.4 背景装饰佩戴流程

```
用户点击"佩戴"背景
   │
   ▼
CreditController.toggleEquip(userId, decorationId, equip=true)
   │
   ├── 同类型先卸下（equipped = false）
   ├── 该装饰 equipped = true
   └── 更新 user_decorations
   │
   ▼
前端 loadEquippedStyles()
   │
   ├── GET /credits/decorations/{userId}
   ├── 筛选 equipped = true 的 BACKGROUND 项
   ├── document.body.classList.add('bg-像素世界')
   ├── base.css 中 body.bg-像素世界 { ... } 覆盖 CSS 变量
   └── 全局配色、侧边栏、卡片全部切换
```

---

## 六、前端架构

### 6.1 文件结构

```
qianduan/
├── index.html          ← 单页应用，所有页面 Section 在此
├── js/
│   ├── app.js          ← 全部业务逻辑（~1950 行）
│   └── skin.js         ← 皮肤管理器（亮/暗切换）
├── css/
│   ├── base.css        ← 基础变量 + 背景装饰主题（bg-*）
│   └── skins/
│       ├── ink.css     ← 亮色主题（墨韵纸香）
│       └── dark.css    ← 暗黑主题
└── Dockerfile          ← Nginx 静态文件服务
```

### 6.2 页面导航

所有页面在 `index.html` 中以 `<section id="xxx-page" class="page">` 形式存在，
通过 `showPage('xxx')` 切换显示。

| 页面 ID | 功能 |
|---------|------|
| `home-page` | 首页：知识点树 + 问题列表 |
| `question-detail-page` | 问题详情 + 回答 + AI 辅助 |
| `profile-page` | 个人中心（头像、称号、关注、收藏、历史、装饰） |
| `ai-page` | AI 独立问答页 + 角色选择 |
| `question-bank-page` | 题库：浏览 / 闯关模式 |
| `gacha-page` | 跃迁抽卡 |

### 6.3 皮肤系统

```
SkinManager (skin.js)
   │
   ├── 切换 <link id="skin-stylesheet"> 的 href
   ├── ink.css  ← 墨韵纸香（默认）
   └── dark.css ← 暗黑模式
   │
背景装饰 bg-*（base.css）
   │
   └── 覆盖 CSS 变量，实现 6 种主题配色
      纯白 / 星空 / 竹林 / 银河 / 极光 / 像素世界 / 手绘
```

---

## 七、部署架构

```
                    localhost
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
   jelly-qianduan  jelly-study-1  jelly-study-2
    (8088:80)      (8086:8086)    (8087:8086)
   Nginx 静态       Spring Boot    Spring Boot
        │               │               │
        │               └───────┬───────┘
        │                       │
        ▼                       ▼
   jelly-redis            jelly-rabbitmq
    (6379)                (5672 / 15672)
        │                       │
        ▼                       ▼
   jelly-network (bridge) ─────────────────────
        │
        ▼
   MongoDB (宿主机 27017)
   DeepSeek API (外部 HTTPS)
```

---

## 八、关键配置

| 配置项 | 位置 | 值 |
|--------|------|-----|
| MongoDB 连接 | docker-compose 环境变量 | `host.docker.internal:27017` |
| Redis 连接 | docker-compose 环境变量 | `redis:6379` |
| DeepSeek API Key | `application.yml` | `ai.deepseek.api-key` |
| 头像存储 | Volume 挂载 | `./uploads:/uploads` |
| 前端 API 基址 | `app.js` | `http://localhost:8086/api` |

---

## 九、开发常用命令

```bash
# 后端编译
cd JellyStudy && mvn package -pl jelly-study -am -DskipTests -q

# 单服务重建
docker compose build --no-cache jelly-study-instance1
docker compose up -d jelly-study-instance1

# 前端重建
docker compose build --no-cache qianduan
docker compose up -d qianduan

# 全量重建
docker compose build --no-cache && docker compose up -d
```
