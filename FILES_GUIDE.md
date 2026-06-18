# JellyStudy 文件说明文档

## 一、后端 — jelly-study 主服务

### 入口

| 文件 | 作用 |
|------|------|
| `JellyStudyApplication.java` | Spring Boot 启动入口 |

### Controller 层（API 接口）

| 文件 | 作用 |
|------|------|
| `UserController.java` | 用户注册/登录、个人资料、头像上传、关注/取关/粉丝/收藏/浏览历史 |
| `QuestionController.java` | 问题发布/查询/编辑/删除、搜索、点赞、相似问题推荐 |
| `AnswerController.java` | 回答提交/查询/编辑/删除、点赞、采纳 |
| `AIController.java` | AI 问答接口（支持 persona 角色参数） |
| `CreditController.java` | 信用点查询/赚取、抽卡、碎片合成、装饰佩戴/卸下 |
| `QuestionBankController.java` | 题库 CRUD、AI 生成题目、答题校验、闯关提交 |
| `KnowledgePointController.java` | 知识点树 CRUD、层级管理 |
| `NotificationController.java` | 系统通知列表、标记已读 |
| `MessageController.java` | 私信发送/接收、联系人列表、会话已读 |
| `CommentController.java` | 评论提交/查询/删除 |
| `NacosConfigController.java` | 配置中心接口（当前未启用） |
| `RedisController.java` | Redis 缓存操作测试 |

### Service 层（业务逻辑）

| 文件 | 作用 |
|------|------|
| `UserService.java` / `Impl` | 用户注册登录、密码验证 |
| `UserCenterService.java` | 用户中心聚合：关注/收藏/浏览/称号、个人资料汇总 |
| `QuestionService.java` / `Impl` | 问题 CRUD、搜索、点赞、标签管理 |
| `AnswerService.java` / `Impl` | 回答 CRUD、点赞、AI 评分记录 |
| `AIService.java` / `Impl` | 调用 DeepSeek API，注入 persona System Prompt，解析返回 |
| `CreditService.java` / `Impl` | 信用点经济：收支流水、抽卡概率计算、保底机制、碎片合成 |
| `QuestionBankService.java` / `Impl` | 题库 CRUD、AI 调用生成题目（JSON 解析入库） |
| `KnowledgePointService.java` / `Impl` | 知识点树管理 |
| `CommentService.java` / `Impl` | 评论 CRUD |
| `NotificationService.java` / `Impl` | 通知创建/查询/标记已读 |
| `MessageService.java` / `Impl` | 私信收发、会话管理 |
| `NotificationIntegrationService.java` | 通知与 RabbitMQ 消息队列的桥接 |
| `RedisService.java` | Redis 缓存读写封装 |
| `TitleCatalog.java` | 内置称号规则表（根据 questionCount/answerCount/reputation 自动授予） |
| `AvatarStorageService.java` | 头像文件存储到 uploads 目录 |

### Repository 层（MongoDB 数据访问）

| 文件 | 操作的集合 |
|------|-----------|
| `UserRepository.java` | users |
| `QuestionRepository.java` | questions |
| `AnswerRepository.java` | answers |
| `CommentRepository.java` | comments |
| `KnowledgePointRepository.java` | knowledge_points |
| `FollowRepository.java` | follows |
| `FavoriteRepository.java` | favorites |
| `BrowseHistoryRepository.java` | browse_history |
| `GachaItemRepository.java` | gacha_items |
| `UserFragmentRepository.java` | user_fragments |
| `UserDecorationRepository.java` | user_decorations |
| `QuestionBankItemRepository.java` | question_bank_items |
| `NotificationRepository.java` | notifications |
| `MessageRepository.java` | messages |

### Entity 层（数据模型）

| 文件 | MongoDB 集合 | 说明 |
|------|-------------|------|
| `User.java` | users | 账号、积分、称号、问题/回答计数 |
| `Question.java` | questions | 标题、内容、标签、知识点关联、状态 |
| `Answer.java` | answers | 内容、AI评分、点赞用户列表 |
| `Comment.java` | comments | 评论内容、关联回答ID |
| `KnowledgePoint.java` | knowledge_points | 知识点名称、父节点、层级 |
| `Follow.java` | follows | 关注者ID + 被关注者ID |
| `Favorite.java` | favorites | 用户ID + 问题ID |
| `BrowseHistory.java` | browse_history | 用户ID + 问题ID + 浏览时间 |
| `GachaItem.java` | gacha_items | 物品名、类型、稀有度、合成碎片数 |
| `UserFragment.java` | user_fragments | 用户ID + 物品ID + 碎片数量 |
| `UserDecoration.java` | user_decorations | 合成后的装饰品、佩戴状态 |
| `QuestionBankItem.java` | question_bank_items | 题目、选项、答案、解析、难度 |
| `Notification.java` | notifications | 通知类型、内容、已读状态 |
| `Message.java` | messages | 发送者/接收者、内容、已读状态 |
| `ApiResponse.java` | — | 通用 JSON 响应包装 `{code, message, data}` |
| `*Request.java` | — | 请求体 DTO |

### AI 模块

| 文件 | 作用 |
|------|------|
| `PersonaPrompts.java` | 6 个人设 + 默认老师角色的 System Prompt 定义 |

### Config 配置

| 文件 | 作用 |
|------|------|
| `MongoConfig.java` | MongoDB 连接 + Repository 扫描 |
| `RedisConfig.java` | Redis 连接配置 |
| `RabbitMQConfig.java` | 消息队列、交换机、绑定声明 |
| `WebConfig.java` | CORS 跨域、静态资源映射（uploads/） |
| `NacosConfig.java` | Nacos 配置中心（当前未启用） |
| `GlobalExceptionHandler.java` | 全局异常处理 → 统一 JSON 错误响应 |
| `GachaItemSeeder.java` | **启动时初始化奖池** — 17 件物品的固定列表，幂等写入 |

### 其他

| 文件 | 作用 |
|------|------|
| `NotificationConsumer.java` | RabbitMQ 消费者：接收异步消息后创建通知 |
| `EvaluationClient.java` | 调用 evaluation 服务进行 AI 评价 |
| `AnswerDubboServiceImpl.java` | Dubbo 回答服务实现（当前未启用） |
| `QuestionDubboServiceImpl.java` | Dubbo 问题服务实现（当前未启用） |
| `api/AnswerEvaluationResult.java` | 回答评价结果 DTO |
| `api/QuestionEvaluationResult.java` | 问题评价结果 DTO |
| `api/EvaluationDubboService.java` | 评价 Dubbo 接口定义 |

---

## 二、后端 — jelly-study-api 共享模块

| 文件 | 作用 |
|------|------|
| `QuestionDTO.java` | 问题数据传输对象 |
| `AnswerDTO.java` | 回答数据传输对象 |
| `QuestionDubboService.java` | 问题 Dubbo 服务接口定义 |
| `AnswerDubboService.java` | 回答 Dubbo 服务接口定义 |

---

## 三、后端 — jelly-study-evaluation 评估服务

独立微服务（端口 8089），负责 AI 评价问答质量。

| 文件 | 作用 |
|------|------|
| `EvaluationApplication.java` | 评估服务启动入口 |
| `EvaluationController.java` | 评价 API 接口 |
| `EvaluationService.java` | 评价逻辑（调用 AI 打分） |
| `QuestionEvaluation.java` | 问题评价实体 |
| `AnswerEvaluation.java` | 回答评价实体 |

---

## 四、前端 qianduan

| 文件 | 作用 |
|------|------|
| `index.html` | **单页应用入口** — 所有页面 Section（首页/详情/个人中心/AI/题库/抽卡）、侧边栏、模态框 |
| `js/app.js` | **全部前端逻辑** — API 调用、页面渲染、事件处理、抽卡动画、摆件物理引擎、皮肤切换 |
| `js/skin.js` | **皮肤管理器** — 亮色/暗色 CSS 动态切换，localStorage 持久化 |

### CSS

| 文件 | 作用 |
|------|------|
| `css/base.css` | **基础样式 + 背景装饰主题** — CSS 变量定义、全局 reset、7 个 bg-* 主题（纯白/星空/竹林/银河/极光/像素/手绘）的完整配色与纹理覆盖 |
| `css/skins/ink.css` | **亮色皮肤（墨韵纸香）** — 侧边栏、卡片、气泡、按钮、表单等全部 UI 样式 |
| `css/skins/dark.css` | **暗色皮肤** — 独立的暗黑配色体系 + ink 变量名兼容别名 |

### 部署

| 文件 | 作用 |
|------|------|
| `qianduan/Dockerfile` | Nginx 静态文件服务镜像 |

---

## 五、根目录

| 文件 | 作用 |
|------|------|
| `docker-compose.yml` | 7 个容器的编排定义（redis/rabbitmq/study×2/evaluation/qianduan） |
| `.gitignore` | 排除编译产物、IDE 配置、敏感文档 |
| `PROJECT_STRUCTURE.md` | 项目架构文档 |
| `docs/gacha-pool.md` | 抽卡奖池说明 |
| `docs/superpowers/specs/*.md` | 功能设计文档 |
