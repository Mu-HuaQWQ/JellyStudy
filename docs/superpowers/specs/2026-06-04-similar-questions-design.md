# 相似问题推荐 — 设计文档

日期: 2026-06-04

## 目标

问题详情页展示相似问题及相似度，帮助用户发现相关内容。

## 方案

复用现有 `knowledgePointId` 和 `tags` 字段，纯数学计算相似度，不新增 AI 调用，不改数据结构。

## 后端

### 新接口

```
GET /api/questions/{id}/similar?limit=5
```

返回:
```json
{
  "code": 200,
  "data": [
    { "id": "...", "title": "...", "knowledgePointTitle": "...", "similarity": 0.85 },
    ...
  ]
}
```

### 相似度算法

```
总分 = 知识点分(权重0.6) + 标签分(权重0.4)

知识点分 = knowledgePointId 相同且非空 ? 0.6 : 0
标签分   = Jaccard系数 * 0.4
         = |tagsA ∩ tagsB| / max(|tagsA|, |tagsB|) * 0.4
```

- 候选集：knowledgePointId 相同 或 tags 有交集的未删除问题（排除自身）
- 应用层排序取 top N

### 改动文件

- `QuestionController.java` — 新增 endpoint
- `QuestionService.java` / `QuestionServiceImpl.java` — 新增 `findSimilar` 方法
- `QuestionRepository.java` — 可能需要新查询方法

## 前端

### 相似问题面板

- 在 `#questionDetail` 右侧加 sticky 面板
- 调用 `/questions/{id}/similar?limit=5`
- 每条显示：标题、知识点、相似度百分比进度条
- 点击跳转到对应问题

### 改动文件

- `qianduan/js/app.js` — `renderQuestionDetail` 内新增面板 + API 调用
- `qianduan/css/style.css` — 两栏布局 + 面板样式
