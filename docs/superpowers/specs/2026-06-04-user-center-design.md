# 用户中心设计文档

日期：2026-06-04
项目：JellyStudy 问答学习平台
范围：全栈实现（后端 MongoDB 数据模型 + REST 接口，前端完整页面对接真实数据）

## 目标

在现有平台基础上新增「用户中心 / 个人中心」，包含 6 个功能：

1. 上传头像
2. 显示最近浏览
3. 查看关注的人（含关注/取关、关注数/粉丝数、问题页关注按钮）
4. 管理收藏的帖子（仅收藏问题）
5. 查看当前拥有的贡献点（复用现有 reputation）
6. 管理称号头衔（按规则自动授予 + 用户选佩戴展示）

## 现状基础

- 后端 Spring Boot 2.7 + MongoDB，模块化（jelly-study 主服务 + jelly-study-evaluation）
- `User` 实体已有：`avatar`、`reputation`（贡献点）、`questionCount`、`answerCount`、`role`
- 登录为简单模式：用户名密码校验，前端 `currentUserId` 存 localStorage，无 JWT/session
- 前端单页 `index.html` 多 section 切换 + 侧边栏导航（`data-page` + `showPage()`）
- 前端 `API_BASE_URL = 'http://localhost:8086/api'`，后端在 8086
- 缺失：最近浏览、关注、收藏、称号 的数据模型与接口

## 1. 数据模型（MongoDB）

### 1.1 扩展 User（新增字段）

```
avatar          String        // 改为存路径，如 /uploads/avatars/user001.png（字段已存在）
followingCount  Integer       // 关注数（冗余存储）
followerCount   Integer       // 粉丝数（冗余存储）
ownedTitles     List<String>  // 已解锁的称号 code
displayTitle    String        // 当前佩戴展示的称号 code
// reputation（贡献点）已存在，复用
```

新增字段在创建用户时初始化：followingCount=0，followerCount=0，ownedTitles=["newbie"]，displayTitle="newbie"。

### 1.2 新建 collection

| collection | 字段 | 说明 |
|---|---|---|
| `follows` | id, followerId, followingId, createTime | 关注关系；(followerId, followingId) 唯一索引 |
| `favorites` | id, userId, questionId, questionTitle, createTime | 收藏的问题；标题冗余存便于列表展示 |
| `browse_history` | id, userId, questionId, questionTitle, viewTime | 最近浏览；同问题去重置顶，每人最多保留 20 条 |

### 1.3 称号规则（后端写死规则表，不建表）

称号按规则自动授予，每次查询 titles 时后端实时计算并同步到 `User.ownedTitles`。

| code | 名称 | 解锁条件 |
|---|---|---|
| newbie | 新人 | 默认人人有 |
| asker | 提问新秀 | questionCount ≥ 5 |
| asker_pro | 提问达人 | questionCount ≥ 20 |
| answerer | 解答之星 | answerCount ≥ 10 |
| scholar | 学者 | reputation ≥ 100 |
| master | 大师 | reputation ≥ 500 |

## 2. 后端接口

### 2.1 头像上传

- `POST /api/users/{id}/avatar`（multipart `file`）→ 存 `uploads/avatars/{userId}.{ext}`，更新 `user.avatar`，返回新路径
- `WebConfig` 增加静态资源映射：`/uploads/**` → 本地 `uploads/` 目录
- docker-compose 给 jelly-study 容器挂载 volume（Dockerfile 工作目录为 `/`，故挂载 `./uploads:/uploads`），避免重建镜像后头像丢失。两个主服务实例（instance1/instance2）需挂同一目录以共享头像文件

### 2.2 关注

- `POST /api/users/{id}/follow?targetId=xxx` 关注
- `DELETE /api/users/{id}/follow?targetId=xxx` 取关
- `GET /api/users/{id}/following` 我关注的人列表
- `GET /api/users/{id}/followers` 粉丝列表
- `GET /api/users/{id}/follow-status?targetId=xxx` 是否已关注（问题页按钮用）

关注/取关在同一 service 方法内同时维护 `follows` 表与双方 `followingCount/followerCount` 冗余字段，保证一致。不能关注自己；重复关注幂等。

### 2.3 收藏

- `POST /api/users/{id}/favorites?questionId=xxx`
- `DELETE /api/users/{id}/favorites?questionId=xxx`
- `GET /api/users/{id}/favorites`

重复收藏幂等（依赖 (userId, questionId)）。

### 2.4 最近浏览

- `POST /api/users/{id}/history?questionId=xxx`（进问题详情时调用，去重置顶 + 截断保留最近 20 条）
- `GET /api/users/{id}/history`

### 2.5 称号

- `GET /api/users/{id}/titles` 返回全部称号定义 + 已解锁标记 + 当前佩戴；后端实时按规则算并同步 `ownedTitles`
- `PUT /api/users/{id}/display-title?code=xxx` 选佩戴（必须是已解锁的，否则报错）

### 2.6 个人资料汇总（优化）

- `GET /api/users/{id}/profile` 一次返回贡献点 / 关注数 / 粉丝数 / 当前佩戴称号等，减少前端请求数

## 3. 前端

### 3.1 入口（两者都要）

- 侧边栏导航新增「个人中心」菜单项（`data-page="profile"`）
- 左下角当前用户区域点击进入 profile

### 3.2 个人中心页面（新增 `#profile-page` section）

```
顶部卡片：头像（带上传按钮）+ 昵称 + 佩戴的称号徽章 + 贡献点 + 关注数/粉丝数
Tab 切换：
  - 最近浏览  → 问题卡片列表
  - 我的收藏  → 问题卡片列表（带取消收藏）
  - 关注的人  → 用户列表（带取关）/ 粉丝列表
  - 我的称号  → 称号网格，已解锁高亮可点击佩戴；未解锁灰色 + 显示解锁条件
```

头像 URL 拼接：前端定义图片基址 = `API_BASE_URL` 去掉 `/api`（即 `http://localhost:8086`）+ `user.avatar` 路径。

### 3.3 改动现有页面

- 问题详情：进入时调用 history 接口；新增「收藏」按钮；作者名旁新增「关注」按钮（按 follow-status 显示已关注/关注）
- 侧边栏左下角头像：从首字母改为显示真实头像图（有图显示图，无图用首字母兜底）

## 4. 技术取舍

### 4.1 头像存储：服务器文件 + 静态访问

图片存后端挂载目录，DB 只存 URL 路径。必须在 docker-compose 给容器挂 volume，否则重建镜像后头像丢失。由于有两个主服务实例（8086/8087），需挂同一宿主目录共享文件。优于 base64 存 Mongo（避免文档膨胀、查询变慢）。

### 4.2 关注数维护：冗余字段

用 `followingCount/followerCount` 冗余字段，关注/取关时增减。列表与卡片展示快，代价是需保证与 `follows` 表一致——统一放在同一 service 方法内增减。

## 5. 范围与非目标

- 不引入 JWT/session，沿用现有 currentUserId 模式
- 称号仅自动授予，不做管理员后台分配
- 收藏仅针对问题，不收藏单条回答
- 最近浏览持久化到后端（非仅 localStorage）
