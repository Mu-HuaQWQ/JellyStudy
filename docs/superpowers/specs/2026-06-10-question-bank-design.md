# 题库系统设计文档

## 1. 功能概述

为每个知识点提供题库功能。支持 AI 自动生成选择题和判断题，也支持手动添加/编辑。提供浏览模式和闯关模式两种刷题方式。

## 2. 实体模型

**QuestionBankItem**（MongoDB 集合 `question_bank_items`）

```java
@Document(collection = "question_bank_items")
public class QuestionBankItem {
    @Id private String id;
    private String knowledgePointId;      // 关联知识点
    private String knowledgePointTitle;   // 冗余：知识点标题
    private QuestionType type;            // CHOICE / TF
    private String question;              // 题目内容
    private List<String> options;         // 选项列表
    private String correctAnswer;         // 正确答案
    private String explanation;           // 解析
    private String difficulty;            // 简单/中等/困难
    private String authorId;              // 创建者（AI生成则为 "ai"）
    private LocalDateTime createTime;
}

enum QuestionType { CHOICE, TF }
```

- CHOICE：options 有4个选项，correctAnswer 为其中一个选项的文本
- TF：options 固定为 ["正确", "错误"]，correctAnswer 为 "正确" 或 "错误"

## 3. API 设计

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/question-bank/knowledge-point/{kpId}` | 获取某知识点的所有题目 |
| POST | `/api/question-bank/generate` | AI 生成题目 `{knowledgePointId, count}` |
| POST | `/api/question-bank` | 手动添加题目 |
| PUT | `/api/question-bank/{id}` | 编辑题目 |
| DELETE | `/api/question-bank/{id}` | 删除题目 |
| POST | `/api/question-bank/check` | 提交单题答案，返回对错+解析 |
| POST | `/api/question-bank/submit` | 闯关模式提交整组答案，返回总分+逐题解析 |

## 4. AI 生成逻辑

调用 DeepSeek API，System Prompt 要求输出 JSON 数组：

```json
[
  {
    "type": "CHOICE",
    "question": "题目内容",
    "options": ["A. xxx", "B. xxx", "C. xxx", "D. xxx"],
    "correctAnswer": "A. xxx",
    "explanation": "解析",
    "difficulty": "简单"
  }
]
```

生成后自动存入 question_bank_items 集合，authorId = "ai"。

## 5. 前端交互

### 5.1 知识点卡片改造
- 每个知识点卡片新增"📝 题库 (N题)"入口
- 显示该知识点已有题目数量

### 5.2 题库页面
- 顶部：知识点标题 + "🤖 AI 生成"按钮 + 输入生成数量
- Tab 切换：浏览模式 / 闯关模式

### 5.3 浏览模式
- 题目列表，每道题显示类型标签（选择/判断）
- 点击展开：显示选项 + "查看答案"按钮
- 点击后显示正确答案（绿色高亮）+ 解析

### 5.4 闯关模式
- 默认加载10题（或当前题库全部，取少者）
- 逐题作答，不可回退
- 全部完成统一提交，显示：
  - 总分（x/10）
  - 每道题正确/错误标记 + 解析
  - "再来一组"按钮

## 6. 涉及文件

| 文件 | 改动 |
|------|------|
| `entity/QuestionBankItem.java` | 新增实体 |
| `repository/QuestionBankItemRepository.java` | 新增仓库 |
| `service/QuestionBankService.java` | 新增服务（CRUD + AI生成 + 校验） |
| `controller/QuestionBankController.java` | 新增控制器 |
| `qianduan/index.html` | 题库页面 HTML |
| `qianduan/js/app.js` | 题库渲染 + 浏览/闯关逻辑 |
| `qianduan/css/skins/ink.css` | 题库样式 |
| `qianduan/css/skins/dark.css` | 题库暗黑样式 |
