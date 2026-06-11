# AI 角色扮演（Role-Play Tutor）设计文档

## 1. 功能概述

为 AI 问答功能添加角色扮演能力，用户可从 6 位崩坏：星穹铁道角色中选择 AI 人设，动态切换 System Prompt，改变 AI 回答的语气和风格。

## 2. 角色人设定义

| 编号 | 角色 | System Prompt 核心要素 |
|------|------|----------------------|
| `xilian` | 昔涟 | 自称"人家"，句尾带"♪"，爱用波浪线"~"，浪漫俏皮，软萌可爱但答案准确 |
| `baie` | 白厄 | 少年英雄腔调，"我是终将升起的烈阳"，文雅朗诵感，鼓励式教学 |
| `pamu` | 帕姆 | 自称"帕姆"（第三人称），句尾加"帕"，傲娇严厉但认真负责 |
| `luosigumu` | 螺丝咕姆 | "逻辑：…"开头判断句，诗意比喻，优雅绅士，理性推导 |
| `heita` | 黑塔 | 毒舌极简，"转圈圈喽~""就这？"，自恋高效，三句话讲完赶人 |
| `sushang` | 李素裳 | 自称"本姑娘"，半文半白，热情豪爽，偶尔用错成语但核心正确 |

默认无 persona 时使用原版老师人设。

## 3. 后端设计

### 3.1 新增 Persona 枚举/常量

文件：`JellyStudy/jelly-study/src/main/java/com/jellystudy/ai/PersonaPrompts.java`

```java
// 定义各角色的 System Prompt 映射表
public class PersonaPrompts {
    public static final Map<String, String> PROMPTS = Map.of(
        "xilian", "你是昔涟...",
        "baie", "你是白厄...",
        // ... 等
    );
    public static String getPrompt(String personaCode) { ... }
}
```

### 3.2 修改 AIServiceImpl

- `answerQuestion(questionTitle, questionContent, personaCode)` 新增参数
- 从 `PersonaPrompts` 获取对应 System Prompt
- 注入到 DeepSeek API 请求的 system message 中

### 3.3 修改 AIController

- `POST /api/ai/answer` 接口新增可选参数 `persona`
- 透传给 Service 层

## 4. 前端设计

### 4.1 新增 AI 问答独立页面

- 侧边栏导航新增"AI 问答"项（data-page="ai"）
- 页面结构（布局A — 紧凑型）：
  ```
  ┌─────────────────────────────────────────┐
  │ 🤖 AI 智能问答    🎭 人设：[下拉选择]   │  ← 顶栏
  ├─────────────────────────────────────────┤
  │                                         │
  │  聊天区域（用户消息 + AI 回复）          │  ← 对话区
  │                                         │
  ├─────────────────────────────────────────┤
  │ [输入框                            ] [发送] │  ← 输入区
  └─────────────────────────────────────────┘
  ```
- 角色选择器与顶栏融合

### 4.2 问题详情页内嵌 AI 问答区

- 在问题详情页侧边栏（`detail-sidebar`）新增可折叠的 AI 问答区块
- 包含小型 persona 选择器 + 输入框 + 发送按钮
- 默认折叠，点击展开

### 4.3 共用逻辑

- `currentPersona` 全局变量，localStorage 持久化
- 两个入口共用同一 persona 状态

## 5. CSS 改动

- `ink.css` / `dark.css`：新增 AI 页面样式、内嵌 AI 区样式
- 聊天气泡样式（用户/AI 区分左右）
- 角色选择器样式

## 6. 涉及文件清单

| 文件 | 改动类型 |
|------|---------|
| `JellyStudy/.../ai/PersonaPrompts.java` | 新增 |
| `JellyStudy/.../service/AIService.java` | 修改（接口加参数） |
| `JellyStudy/.../service/AIServiceImpl.java` | 修改（注入 persona prompt） |
| `JellyStudy/.../controller/AIController.java` | 修改（接收 persona 参数） |
| `qianduan/index.html` | 修改（AI 页面 HTML + 侧边栏导航 + 详情页内嵌区） |
| `qianduan/js/app.js` | 修改（AI 页面渲染 + persona 切换 + 内嵌 AI 逻辑） |
| `qianduan/css/skins/ink.css` | 修改（新增样式） |
| `qianduan/css/skins/dark.css` | 修改（新增样式） |
