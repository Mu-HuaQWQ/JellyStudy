# 题库系统实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为每个知识点提供题库功能，支持选择题+判断题的 AI 生成和手动添加，提供浏览和闯关两种刷题模式

**Architecture:** 新增 QuestionBankItem 实体 + Repository + Service + Controller 后端四件套；前端在知识点卡片添加入口，新增独立题库页面，两种模式（浏览/闯关）切换

**Tech Stack:** Java 11, Spring Boot 2.7, MongoDB, DeepSeek API, 纯 HTML/CSS/JS

---

## File Structure

```
Backend (new):
  CREATE  entity/QuestionBankItem.java            — 题库题目实体
  CREATE  repository/QuestionBankItemRepository.java — MongoDB 仓库
  CREATE  service/QuestionBankService.java        — 业务接口
  CREATE  service/QuestionBankServiceImpl.java   — 业务实现（CRUD + AI生成 + 校验）
  CREATE  controller/QuestionBankController.java — REST 控制器

Frontend (modify):
  MODIFY  qianduan/index.html      — 题库页面 HTML + 知识点卡片改造
  MODIFY  qianduan/js/app.js       — 题库逻辑：浏览模式、闯关模式、AI生成对接
  MODIFY  qianduan/css/skins/ink.css   — 题库样式
  MODIFY  qianduan/css/skins/dark.css  — 题库暗黑样式
```

---

### Task 1: 创建 QuestionBankItem 实体

**Files:**
- Create: `JellyStudy/jelly-study/src/main/java/com/jellystudy/entity/QuestionBankItem.java`

- [ ] **Step 1: 创建实体文件**

```java
package com.jellystudy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "question_bank_items")
public class QuestionBankItem {

    @Id
    private String id;

    private String knowledgePointId;

    private String knowledgePointTitle;

    private String type;  // "CHOICE" 或 "TF"

    private String question;

    private List<String> options;

    private String correctAnswer;

    private String explanation;

    private String difficulty;  // "简单" / "中等" / "困难"

    private String authorId;    // "ai" 或用户ID

    @CreatedDate
    private LocalDateTime createTime;
}
```

- [ ] **Step 2: 编译验证**

```bash
cd E:/大三下/移动应用/11_2/JellyStudy && mvn compile -pl jelly-study -am -q
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add JellyStudy/jelly-study/src/main/java/com/jellystudy/entity/QuestionBankItem.java
git commit -m "feat: add QuestionBankItem entity for question bank system"
```

---

### Task 2: 创建 QuestionBankItemRepository

**Files:**
- Create: `JellyStudy/jelly-study/src/main/java/com/jellystudy/repository/QuestionBankItemRepository.java`

- [ ] **Step 1: 创建仓库**

```java
package com.jellystudy.repository;

import com.jellystudy.entity.QuestionBankItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionBankItemRepository extends MongoRepository<QuestionBankItem, String> {

    List<QuestionBankItem> findByKnowledgePointId(String knowledgePointId);

    long countByKnowledgePointId(String knowledgePointId);

    void deleteByKnowledgePointId(String knowledgePointId);
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile -pl jelly-study -am -q
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add JellyStudy/jelly-study/src/main/java/com/jellystudy/repository/QuestionBankItemRepository.java
git commit -m "feat: add QuestionBankItemRepository"
```

---

### Task 3: 创建 QuestionBankService 接口 + 实现

**Files:**
- Create: `JellyStudy/jelly-study/src/main/java/com/jellystudy/service/QuestionBankService.java`
- Create: `JellyStudy/jelly-study/src/main/java/com/jellystudy/service/QuestionBankServiceImpl.java`

- [ ] **Step 1: 创建接口**

```java
package com.jellystudy.service;

import com.jellystudy.entity.QuestionBankItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QuestionBankService {

    List<QuestionBankItem> findByKnowledgePointId(String knowledgePointId);

    long countByKnowledgePointId(String knowledgePointId);

    QuestionBankItem create(QuestionBankItem item);

    QuestionBankItem update(String id, QuestionBankItem item);

    void delete(String id);

    Optional<QuestionBankItem> findById(String id);

    /** AI 生成题目，返回生成的题目列表 */
    List<QuestionBankItem> generateByAI(String knowledgePointId, String knowledgePointTitle,
                                        String knowledgePointContent, int count);

    /** 校验单题答案，返回 {correct: bool, explanation: str} */
    Map<String, Object> checkAnswer(String questionId, String userAnswer);

    /** 闯关模式提交整组答案 */
    List<Map<String, Object>> submitAnswers(List<Map<String, String>> submissions);
}
```

- [ ] **Step 2: 创建实现类**

```java
package com.jellystudy.service;

import com.jellystudy.entity.QuestionBankItem;
import com.jellystudy.repository.QuestionBankItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class QuestionBankServiceImpl implements QuestionBankService {

    @Autowired
    private QuestionBankItemRepository repository;

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.base-url}")
    private String baseUrl;

    @Value("${ai.deepseek.model}")
    private String model;

    @Override
    public List<QuestionBankItem> findByKnowledgePointId(String knowledgePointId) {
        return repository.findByKnowledgePointId(knowledgePointId);
    }

    @Override
    public long countByKnowledgePointId(String knowledgePointId) {
        return repository.countByKnowledgePointId(knowledgePointId);
    }

    @Override
    public QuestionBankItem create(QuestionBankItem item) {
        return repository.save(item);
    }

    @Override
    public QuestionBankItem update(String id, QuestionBankItem item) {
        item.setId(id);
        return repository.save(item);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<QuestionBankItem> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<QuestionBankItem> generateByAI(String knowledgePointId, String knowledgePointTitle,
                                                String knowledgePointContent, int count) {
        if (count < 1) count = 5;
        if (count > 20) count = 20;

        String prompt = "请根据以下知识点生成" + count + "道题目，题型为选择题(CHOICE)和判断题(TF)混合，难度覆盖简单、中等、困难。\n\n" +
            "知识点标题：" + knowledgePointTitle + "\n" +
            "知识点内容：" + knowledgePointContent + "\n\n" +
            "请严格按照以下JSON数组格式返回，不要返回其他内容：\n" +
            "[\n" +
            "  {\n" +
            "    \"type\": \"CHOICE\",\n" +
            "    \"question\": \"题目内容\",\n" +
            "    \"options\": [\"A. 选项1\", \"B. 选项2\", \"C. 选项3\", \"D. 选项4\"],\n" +
            "    \"correctAnswer\": \"A. 选项1\",\n" +
            "    \"explanation\": \"解析为什么选这个\",\n" +
            "    \"difficulty\": \"简单\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"type\": \"TF\",\n" +
            "    \"question\": \"题目内容\",\n" +
            "    \"options\": [\"正确\", \"错误\"],\n" +
            "    \"correctAnswer\": \"正确\",\n" +
            "    \"explanation\": \"解析\",\n" +
            "    \"difficulty\": \"中等\"\n" +
            "  }\n" +
            "]\n" +
            "注意：判断题的options必须固定为[\"正确\", \"错误\"]，correctAnswer为\"正确\"或\"错误\"。选择题options必须是4个选项。";

        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", Arrays.asList(
                Map.of("role", "system", "content", "你是一个专业的教育题目生成器。只返回JSON数组，不返回其他内容。"),
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v1/chat/completions",
                HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String raw = (String) message.get("content");
                    return parseAndSave(raw, knowledgePointId, knowledgePointTitle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private List<QuestionBankItem> parseAndSave(String raw, String kpId, String kpTitle) {
        List<QuestionBankItem> items = new ArrayList<>();
        try {
            // 提取 JSON 数组
            int start = raw.indexOf('[');
            int end = raw.lastIndexOf(']');
            if (start != -1 && end != -1 && end > start) {
                String json = raw.substring(start, end + 1);
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                List<Map> list = mapper.readValue(json, List.class);
                for (Map m : list) {
                    QuestionBankItem item = QuestionBankItem.builder()
                        .knowledgePointId(kpId)
                        .knowledgePointTitle(kpTitle)
                        .type((String) m.get("type"))
                        .question((String) m.get("question"))
                        .options((List<String>) m.get("options"))
                        .correctAnswer((String) m.get("correctAnswer"))
                        .explanation((String) m.get("explanation"))
                        .difficulty((String) m.get("difficulty"))
                        .authorId("ai")
                        .build();
                    items.add(repository.save(item));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public Map<String, Object> checkAnswer(String questionId, String userAnswer) {
        Optional<QuestionBankItem> opt = repository.findById(questionId);
        if (opt.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("correct", false);
            result.put("explanation", "题目不存在");
            return result;
        }
        QuestionBankItem item = opt.get();
        boolean correct = item.getCorrectAnswer().equals(userAnswer);
        Map<String, Object> result = new HashMap<>();
        result.put("correct", correct);
        result.put("explanation", item.getExplanation());
        result.put("correctAnswer", item.getCorrectAnswer());
        return result;
    }

    @Override
    public List<Map<String, Object>> submitAnswers(List<Map<String, String>> submissions) {
        List<Map<String, Object>> results = new ArrayList<>();
        int correctCount = 0;
        for (Map<String, String> sub : submissions) {
            String id = sub.get("questionId");
            String userAnswer = sub.get("answer");
            Map<String, Object> r = checkAnswer(id, userAnswer);
            r.put("questionId", id);
            results.add(r);
            if (Boolean.TRUE.equals(r.get("correct"))) correctCount++;
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("total", submissions.size());
        summary.put("correct", correctCount);
        summary.put("score", correctCount * 100 / Math.max(1, submissions.size()));
        results.add(0, summary); // 第一项是汇总
        return results;
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
mvn compile -pl jelly-study -am -q
```

Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add JellyStudy/jelly-study/src/main/java/com/jellystudy/service/QuestionBankService.java
git add JellyStudy/jelly-study/src/main/java/com/jellystudy/service/QuestionBankServiceImpl.java
git commit -m "feat: add QuestionBankService with AI generation and answer checking"
```

---

### Task 4: 创建 QuestionBankController

**Files:**
- Create: `JellyStudy/jelly-study/src/main/java/com/jellystudy/controller/QuestionBankController.java`

- [ ] **Step 1: 创建控制器**

```java
package com.jellystudy.controller;

import com.jellystudy.entity.ApiResponse;
import com.jellystudy.entity.QuestionBankItem;
import com.jellystudy.service.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question-bank")
@CrossOrigin(origins = "*")
public class QuestionBankController {

    @Autowired
    private QuestionBankService questionBankService;

    /** 获取某知识点的所有题目 */
    @GetMapping("/knowledge-point/{kpId}")
    public ApiResponse<List<QuestionBankItem>> getByKnowledgePoint(@PathVariable String kpId) {
        return ApiResponse.success(questionBankService.findByKnowledgePointId(kpId));
    }

    /** 获取题目数量 */
    @GetMapping("/knowledge-point/{kpId}/count")
    public ApiResponse<Long> countByKnowledgePoint(@PathVariable String kpId) {
        return ApiResponse.success(questionBankService.countByKnowledgePointId(kpId));
    }

    /** AI 生成题目 */
    @PostMapping("/generate")
    public ApiResponse<List<QuestionBankItem>> generateByAI(@RequestBody Map<String, Object> req) {
        String kpId = (String) req.get("knowledgePointId");
        String kpTitle = (String) req.getOrDefault("knowledgePointTitle", "");
        String kpContent = (String) req.getOrDefault("knowledgePointContent", "");
        int count = req.containsKey("count") ? ((Number) req.get("count")).intValue() : 5;

        if (kpId == null || kpId.isBlank()) {
            return ApiResponse.error(400, "knowledgePointId 不能为空");
        }

        List<QuestionBankItem> items = questionBankService.generateByAI(kpId, kpTitle, kpContent, count);
        return ApiResponse.success(items);
    }

    /** 手动添加题目 */
    @PostMapping
    public ApiResponse<QuestionBankItem> create(@RequestBody QuestionBankItem item) {
        return ApiResponse.success(questionBankService.create(item));
    }

    /** 编辑题目 */
    @PutMapping("/{id}")
    public ApiResponse<QuestionBankItem> update(@PathVariable String id, @RequestBody QuestionBankItem item) {
        return ApiResponse.success(questionBankService.update(id, item));
    }

    /** 删除题目 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        questionBankService.delete(id);
        return ApiResponse.success();
    }

    /** 校验单题 */
    @PostMapping("/check")
    public ApiResponse<Map<String, Object>> checkAnswer(@RequestBody Map<String, String> req) {
        return ApiResponse.success(questionBankService.checkAnswer(
            req.get("questionId"), req.get("answer")));
    }

    /** 闯关提交 */
    @PostMapping("/submit")
    public ApiResponse<List<Map<String, Object>>> submitAnswers(@RequestBody List<Map<String, String>> submissions) {
        return ApiResponse.success(questionBankService.submitAnswers(submissions));
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile -pl jelly-study -am -q
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add JellyStudy/jelly-study/src/main/java/com/jellystudy/controller/QuestionBankController.java
git commit -m "feat: add QuestionBankController with AI generation and quiz endpoints"
```

---

### Task 5: 修改 index.html — 题库页面

**Files:**
- Modify: `qianduan/index.html`

需要在两处添加内容：

- [ ] **Step 1: 新增题库页面 section**

找到 `<section id="ai-page"` 之前的位置，插入：

```html
    <!-- =============================================
         Question Bank Page (题库)
         ============================================= -->
    <section id="questionbank-page" class="page">
        <div class="qb-container">
            <div class="qb-topbar">
                <button class="btn-back" onclick="goBackFromQuestionBank()">&larr; 返回知识点</button>
                <div class="qb-topbar-info">
                    <h2 id="qbKpTitle">知识点题库</h2>
                    <span class="qb-count" id="qbCount">0 道题目</span>
                </div>
                <div class="qb-topbar-actions">
                    <input type="number" id="qbGenCount" value="5" min="1" max="20" class="qb-gen-input" title="生成数量">
                    <button class="btn-primary" onclick="generateQuestions()" id="qbGenBtn">🤖 AI 生成</button>
                    <button class="btn-secondary" onclick="showAddQuestionForm()">+ 手动添加</button>
                </div>
            </div>

            <!-- Tab 切换 -->
            <div class="qb-tabs">
                <button class="qb-tab active" data-qbmode="browse" onclick="switchQBMode('browse')">📋 浏览模式</button>
                <button class="qb-tab" data-qbmode="challenge" onclick="switchQBMode('challenge')">⚡ 闯关模式</button>
            </div>

            <!-- 浏览模式 -->
            <div class="qb-mode" id="qbBrowseMode">
                <div id="qbQuestionList" class="qb-question-list"></div>
            </div>

            <!-- 闯关模式 -->
            <div class="qb-mode" id="qbChallengeMode" style="display:none;">
                <div class="qb-challenge-progress" id="qbChallengeProgress"></div>
                <div class="qb-challenge-card" id="qbChallengeCard">
                    <p class="qb-challenge-placeholder">点击"开始闯关"随机抽取题目</p>
                </div>
                <div class="qb-challenge-actions" id="qbChallengeActions">
                    <button class="btn-primary" onclick="startChallenge()" id="qbStartBtn">开始闯关</button>
                </div>
                <div class="qb-challenge-result" id="qbChallengeResult" style="display:none;"></div>
            </div>
        </div>
    </section>
```

- [ ] **Step 2: 修改知识点卡片（在 renderKnowledgePoints 函数中）**

这是 JS 生成的 HTML，所以在 app.js Task 中处理 — 在 `renderKnowledgePoints` 函数返回的 HTML 中，给每个卡片加题库入口。

在 app.js 中找到 `renderKnowledgePoints` 函数的 innerHTML 模板，在卡片底部添加题库按钮。

具体改动在 Task 6 中处理。

- [ ] **Step 3: 修改 app.js 中的 renderKnowledgePoints**

在知识点卡片的 HTML 模板中（约第312行），在 `</div>` 关闭之前、元信息之后，添加：

```js
            <button class="btn-small btn-qb" onclick="event.stopPropagation(); openQuestionBank('${kp.id}', '${escapeHtml(kp.title)}', '${escapeHtml(kp.content || '')}')">
                📝 题库
            </button>
```

- [ ] **Step 4: 更新 JS 版本号**

`v=14` → `v=15`

- [ ] **Step 5: 提交**

```bash
git add qianduan/index.html qianduan/js/app.js
git commit -m "feat: add question bank page HTML, knowledge card entry, JS version bump"
```

---

### Task 6: 修改 app.js — 题库交互逻辑

**Files:**
- Modify: `qianduan/js/app.js`

- [ ] **Step 1: 全局变量**

在文件顶部全局变量区添加：

```js
let qbCurrentKpId = null;
let qbCurrentKpTitle = '';
let qbCurrentKpContent = '';
let qbCurrentMode = 'browse'; // 'browse' | 'challenge'
let qbChallengeQuestions = [];
let qbChallengeIndex = 0;
let qbChallengeAnswers = [];
```

- [ ] **Step 2: 核心函数 — 在文件末尾、window exports 之前添加**

```js
// ══════════════════════════════════════════════════════════
// 题库系统
// ══════════════════════════════════════════════════════════

async function openQuestionBank(kpId, kpTitle, kpContent) {
    qbCurrentKpId = kpId;
    qbCurrentKpTitle = kpTitle;
    qbCurrentKpContent = kpContent;
    qbCurrentMode = 'browse';

    document.getElementById('qbKpTitle').textContent = kpTitle + ' — 题库';

    // 加载题目数量
    try {
        const countRes = await fetchApi('/question-bank/knowledge-point/' + kpId + '/count');
        if (countRes.code === 200) {
            document.getElementById('qbCount').textContent = countRes.data + ' 道题目';
        }
    } catch (e) {}

    switchQBMode('browse');
    loadQbQuestions();
    showPage('questionbank');
}

function goBackFromQuestionBank() {
    showPage('knowledge');
    loadKnowledgePoints();
}

function switchQBMode(mode) {
    qbCurrentMode = mode;
    document.querySelectorAll('.qb-tab').forEach(t => t.classList.remove('active'));
    document.querySelector('[data-qbmode="' + mode + '"]').classList.add('active');

    document.getElementById('qbBrowseMode').style.display = mode === 'browse' ? 'block' : 'none';
    document.getElementById('qbChallengeMode').style.display = mode === 'challenge' ? 'block' : 'none';

    if (mode === 'browse') {
        loadQbQuestions();
    }
}

async function loadQbQuestions() {
    if (!qbCurrentKpId) return;
    const container = document.getElementById('qbQuestionList');
    container.innerHTML = '<p class="loading-text">加载中...</p>';

    try {
        const res = await fetchApi('/question-bank/knowledge-point/' + qbCurrentKpId);
        if (res.code === 200 && res.data) {
            renderQbQuestions(res.data);
            document.getElementById('qbCount').textContent = res.data.length + ' 道题目';
        } else {
            container.innerHTML = '<p class="empty-state">暂无题目，点击"🤖 AI 生成"创建题目</p>';
        }
    } catch (e) {
        container.innerHTML = '<p class="error-state">加载失败</p>';
    }
}

function renderQbQuestions(questions) {
    const container = document.getElementById('qbQuestionList');
    container.innerHTML = questions.map((q, i) => `
        <div class="qb-card" id="qb-card-${i}">
            <div class="qb-card-header">
                <span class="qb-type-badge ${q.type === 'CHOICE' ? 'qb-choice' : 'qb-tf'}">${q.type === 'CHOICE' ? '选择题' : '判断题'}</span>
                <span class="qb-diff-badge">${q.difficulty || '中等'}</span>
                <span class="qb-card-num">#${i + 1}</span>
            </div>
            <div class="qb-card-question">${escapeHtml(q.question)}</div>
            <div class="qb-card-options" id="qb-options-${i}">
                ${(q.options || []).map((opt, oi) => `
                    <div class="qb-option" id="qb-opt-${i}-${oi}" onclick="selectQbOption(${i}, ${oi}, '${escapeHtml(q.correctAnswer || '')}', '${escapeHtml((q.options || [])[oi] || '')}')">
                        ${escapeHtml(opt)}
                    </div>
                `).join('')}
            </div>
            <div class="qb-card-actions">
                <button class="btn-small btn-secondary" onclick="toggleQbAnswer(${i})">查看答案</button>
            </div>
            <div class="qb-answer" id="qb-answer-${i}" style="display:none;">
                <div class="qb-answer-correct">✅ 正确答案：${escapeHtml(q.correctAnswer || '')}</div>
                <div class="qb-answer-explain">💡 解析：${escapeHtml(q.explanation || '暂无解析')}</div>
            </div>
        </div>
    `).join('');
}

function selectQbOption(cardIdx, optIdx, correctAnswer, optionText) {
    // 先清除该题所有选项的选中状态
    const optionsDiv = document.getElementById('qb-options-' + cardIdx);
    optionsDiv.querySelectorAll('.qb-option').forEach(o => o.classList.remove('qb-selected', 'qb-correct', 'qb-wrong'));

    const clicked = document.getElementById('qb-opt-' + cardIdx + '-' + optIdx);
    clicked.classList.add('qb-selected');

    // 自动判断对错并显示
    if (optionText === correctAnswer) {
        clicked.classList.add('qb-correct');
    } else {
        clicked.classList.add('qb-wrong');
        // 高亮正确答案
        const allOpts = optionsDiv.querySelectorAll('.qb-option');
        allOpts.forEach(o => { if (o.textContent.trim() === correctAnswer) o.classList.add('qb-correct'); });
    }
}

function toggleQbAnswer(idx) {
    const el = document.getElementById('qb-answer-' + idx);
    el.style.display = el.style.display === 'none' ? 'block' : 'none';
}

// ═══════════════════════════════════════
// AI 生成
// ═══════════════════════════════════════

async function generateQuestions() {
    const count = parseInt(document.getElementById('qbGenCount').value) || 5;
    const btn = document.getElementById('qbGenBtn');
    btn.textContent = '⏳ 生成中...';
    btn.disabled = true;

    try {
        const res = await fetchApi('/question-bank/generate', 'POST', {
            knowledgePointId: qbCurrentKpId,
            knowledgePointTitle: qbCurrentKpTitle,
            knowledgePointContent: qbCurrentKpContent,
            count: count
        });
        if (res.code === 200 && res.data) {
            alert('成功生成 ' + res.data.length + ' 道题目！');
            loadQbQuestions();
            // 同步更新题目数量
            const countRes = await fetchApi('/question-bank/knowledge-point/' + qbCurrentKpId + '/count');
            if (countRes.code === 200) {
                document.getElementById('qbCount').textContent = countRes.data + ' 道题目';
            }
        } else {
            alert('生成失败：' + (res.message || '请重试'));
        }
    } catch (e) {
        alert('请求失败：' + e.message);
    }
    btn.textContent = '🤖 AI 生成';
    btn.disabled = false;
}

// ═══════════════════════════════════════
// 闯关模式
// ═══════════════════════════════════════

async function startChallenge() {
    const res = await fetchApi('/question-bank/knowledge-point/' + qbCurrentKpId);
    if (res.code !== 200 || !res.data || res.data.length === 0) {
        alert('题库为空，请先生成题目');
        return;
    }
    // 随机抽取10题（或全部）
    let pool = res.data;
    shuffleArray(pool);
    qbChallengeQuestions = pool.slice(0, Math.min(10, pool.length));
    qbChallengeIndex = 0;
    qbChallengeAnswers = [];

    document.getElementById('qbStartBtn').style.display = 'none';
    document.getElementById('qbChallengeResult').style.display = 'none';
    renderChallengeQuestion();
}

function renderChallengeQuestion() {
    const idx = qbChallengeIndex;
    const total = qbChallengeQuestions.length;
    const q = qbChallengeQuestions[idx];

    document.getElementById('qbChallengeProgress').innerHTML = '第 <strong>' + (idx + 1) + '</strong> / ' + total + ' 题';

    const card = document.getElementById('qbChallengeCard');
    card.innerHTML = `
        <div class="qb-challenge-q-header">
            <span class="qb-type-badge ${q.type === 'CHOICE' ? 'qb-choice' : 'qb-tf'}">${q.type === 'CHOICE' ? '选择题' : '判断题'}</span>
            <span class="qb-diff-badge">${q.difficulty || '中等'}</span>
        </div>
        <div class="qb-challenge-question">${escapeHtml(q.question)}</div>
        <div class="qb-challenge-options">
            ${(q.options || []).map((opt, oi) => `
                <div class="qb-challenge-option" onclick="selectChallengeOption(${oi}, '${escapeHtml(opt)}')">
                    ${escapeHtml(opt)}
                </div>
            `).join('')}
        </div>
    `;

    document.getElementById('qbChallengeActions').innerHTML = `
        <button class="btn-primary" id="qbNextBtn" onclick="nextChallengeQuestion()" style="display:none;">
            ${idx + 1 < total ? '下一题 →' : '提交答案 ✓'}
        </button>
    `;
}

function selectChallengeOption(optIdx, optText) {
    const options = document.querySelectorAll('.qb-challenge-option');
    options.forEach(o => o.classList.remove('qb-challenge-selected'));
    options[optIdx].classList.add('qb-challenge-selected');
    qbChallengeAnswers[qbChallengeIndex] = optText;
    document.getElementById('qbNextBtn').style.display = 'inline-block';
}

async function nextChallengeQuestion() {
    qbChallengeIndex++;
    if (qbChallengeIndex < qbChallengeQuestions.length) {
        renderChallengeQuestion();
    } else {
        await submitChallenge();
    }
}

async function submitChallenge() {
    const submissions = qbChallengeQuestions.map((q, i) => ({
        questionId: q.id,
        answer: qbChallengeAnswers[i] || ''
    }));

    try {
        const res = await fetchApi('/question-bank/submit', 'POST', submissions);
        if (res.code === 200 && res.data) {
            renderChallengeResult(res.data);
        } else {
            alert('提交失败：' + res.message);
        }
    } catch (e) {
        alert('提交失败：' + e.message);
    }
}

function renderChallengeResult(results) {
    const summary = results[0]; // 第一项是汇总
    const details = results.slice(1);

    const resultDiv = document.getElementById('qbChallengeResult');
    resultDiv.style.display = 'block';
    resultDiv.innerHTML = `
        <div class="qb-result-summary">
            <h2>🎯 得分：${summary.score} 分</h2>
            <p>共 ${summary.total} 题，答对 ${summary.correct} 题</p>
        </div>
        <div class="qb-result-list">
            ${qbChallengeQuestions.map((q, i) => {
                const d = details[i] || {};
                const icon = d.correct ? '✅' : '❌';
                return `
                    <div class="qb-result-item ${d.correct ? 'qb-result-correct' : 'qb-result-wrong'}">
                        <div class="qb-result-q">${icon} ${escapeHtml(q.question)}</div>
                        <div class="qb-result-your">你的答案：${escapeHtml(qbChallengeAnswers[i] || '(未作答)')}</div>
                        ${!d.correct ? '<div class="qb-result-right">正确答案：' + escapeHtml(d.correctAnswer || '') + '</div>' : ''}
                        <div class="qb-result-explain">💡 ${escapeHtml(d.explanation || '')}</div>
                    </div>
                `;
            }).join('')}
        </div>
        <button class="btn-primary" onclick="startChallenge()" style="margin-top:1rem;">再来一组</button>
    `;

    document.getElementById('qbChallengeCard').innerHTML = '';
    document.getElementById('qbChallengeProgress').innerHTML = '';
    document.getElementById('qbChallengeActions').innerHTML = '';
}

function shuffleArray(arr) {
    for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [arr[i], arr[j]] = [arr[j], arr[i]];
    }
}

// 手动添加题目（简化版弹窗）
function showAddQuestionForm() {
    const type = prompt('题目类型？输入 1=选择题, 2=判断题', '1');
    const question = prompt('题目内容：');
    if (!question) return;
    let options, correctAnswer;

    if (type === '2') {
        options = ['正确', '错误'];
        correctAnswer = prompt('正确答案（正确/错误）：', '正确');
    } else {
        const opts = prompt('4个选项，用 | 分隔：', 'A. xxx | B. xxx | C. xxx | D. xxx');
        if (!opts) return;
        options = opts.split('|').map(s => s.trim());
        correctAnswer = prompt('正确答案（复制选项文本）：', options[0]);
    }

    const difficulty = prompt('难度（简单/中等/困难）：', '中等');

    fetchApi('/question-bank', 'POST', {
        knowledgePointId: qbCurrentKpId,
        knowledgePointTitle: qbCurrentKpTitle,
        type: type === '2' ? 'TF' : 'CHOICE',
        question: question,
        options: options,
        correctAnswer: correctAnswer,
        explanation: '',
        difficulty: difficulty,
        authorId: currentUserId
    }).then(r => {
        if (r.code === 200) { alert('添加成功'); loadQbQuestions(); }
        else alert('添加失败：' + r.message);
    }).catch(e => alert('请求失败：' + e.message));
}
```

- [ ] **Step 3: showPage 添加 questionbank 分支**

在 `showPage` 的 `switch` 块中添加（不需要 init 因为 openQuestionBank 已经做了加载）：

```js
            case 'questionbank':
                // openQuestionBank() 已经设置了所有数据，无需重复加载
                break;
```

- [ ] **Step 4: 注册 window 导出**

在文件末尾 window exports 区域添加：

```js
window.openQuestionBank = openQuestionBank;
window.goBackFromQuestionBank = goBackFromQuestionBank;
window.switchQBMode = switchQBMode;
window.generateQuestions = generateQuestions;
window.startChallenge = startChallenge;
window.selectChallengeOption = selectChallengeOption;
window.nextChallengeQuestion = nextChallengeQuestion;
window.toggleQbAnswer = toggleQbAnswer;
window.selectQbOption = selectQbOption;
window.showAddQuestionForm = showAddQuestionForm;
```

- [ ] **Step 5: 提交**

```bash
git add qianduan/js/app.js qianduan/index.html
git commit -m "feat: add question bank page logic — browse mode, challenge mode, AI generation"
```

---

### Task 7: 修改 ink.css — 题库样式

**Files:**
- Modify: `qianduan/css/skins/ink.css`

- [ ] **Step 1: 追加题库样式**

在 ink.css 末尾追加：

```css
/* ══════════════════════════════════════════════════════════
   Question Bank
   ══════════════════════════════════════════════════════════ */

.qb-container { max-width: 900px; margin: 0 auto; }

.qb-topbar {
    display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap;
    gap: 10px; padding: 12px 0; margin-bottom: 16px;
    border-bottom: 1px solid var(--border-light);
}
.qb-topbar-info h2 { margin: 0; font-size: 1.1rem; }
.qb-count { font-size: 0.85rem; color: var(--text-light); }
.qb-topbar-actions { display: flex; align-items: center; gap: 8px; }
.qb-gen-input { width: 50px; padding: 6px; border: 1px solid var(--border); border-radius: 4px; text-align: center; }

.qb-tabs { display: flex; gap: 8px; margin-bottom: 16px; }
.qb-tab {
    padding: 8px 20px; border: 1px solid var(--border); border-radius: var(--radius);
    background: var(--bg); color: var(--text); cursor: pointer; font-family: inherit; font-size: 0.9rem;
}
.qb-tab.active { background: var(--amber); color: #fff; border-color: var(--amber); }

/* ── Browse Mode ─────────────────────────────────── */
.qb-card {
    background: var(--bg-card); border: 1px solid var(--border-light);
    border-radius: var(--radius-lg); padding: 16px; margin-bottom: 12px;
}
.qb-card-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.qb-type-badge { font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; color: #fff; }
.qb-choice { background: #3b82f6; }
.qb-tf { background: #10b981; }
.qb-diff-badge { font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; background: var(--bg-hover); color: var(--text-light); }
.qb-card-num { margin-left: auto; font-size: 0.8rem; color: var(--text-muted); }
.qb-card-question { font-size: 0.95rem; margin-bottom: 10px; line-height: 1.6; }

.qb-option {
    padding: 8px 12px; border: 1px solid var(--border); border-radius: 6px;
    margin-bottom: 6px; cursor: pointer; font-size: 0.9rem; transition: all 0.15s;
}
.qb-option:hover { border-color: var(--amber); background: #fefce8; }
.qb-selected { border-color: var(--amber); }
.qb-correct { border-color: #10b981; background: #ecfdf5; color: #065f46; }
.qb-wrong { border-color: #ef4444; background: #fef2f2; color: #991b1b; }

.qb-card-actions { margin-top: 8px; }
.qb-answer { margin-top: 8px; padding: 10px; background: #f0fdf4; border-radius: 6px; border: 1px solid #bbf7d0; }
.qb-answer-correct { font-weight: 600; color: #065f46; margin-bottom: 4px; }
.qb-answer-explain { font-size: 0.88rem; color: var(--text-light); }

/* ── Challenge Mode ──────────────────────────────── */
.qb-challenge-progress { text-align: center; margin-bottom: 12px; font-size: 0.95rem; }
.qb-challenge-card {
    background: var(--bg-card); border: 1px solid var(--border-light);
    border-radius: var(--radius-lg); padding: 20px; min-height: 200px;
}
.qb-challenge-placeholder { text-align: center; color: var(--text-light); padding: 3rem 0; }
.qb-challenge-question { font-size: 1.05rem; margin: 12px 0 16px; line-height: 1.6; }
.qb-challenge-options { display: flex; flex-direction: column; gap: 8px; }
.qb-challenge-option {
    padding: 12px 16px; border: 2px solid var(--border); border-radius: 8px;
    cursor: pointer; font-size: 0.95rem; transition: all 0.15s;
}
.qb-challenge-option:hover { border-color: var(--amber); }
.qb-challenge-selected { border-color: var(--amber); background: #fefce8; }
.qb-challenge-actions { text-align: center; margin-top: 16px; }

/* ── Result ──────────────────────────────────────── */
.qb-result-summary { text-align: center; padding: 20px; background: #fefce8; border-radius: var(--radius-lg); margin-bottom: 16px; }
.qb-result-summary h2 { margin: 0 0 4px; color: var(--amber); }
.qb-result-item { padding: 12px; border-radius: var(--radius); margin-bottom: 8px; }
.qb-result-correct { background: #f0fdf4; border: 1px solid #bbf7d0; }
.qb-result-wrong { background: #fef2f2; border: 1px solid #fecaca; }
.qb-result-q { font-weight: 600; margin-bottom: 4px; }
.qb-result-your { font-size: 0.85rem; color: var(--text-light); }
.qb-result-right { font-size: 0.85rem; color: #059669; font-weight: 500; }
.qb-result-explain { font-size: 0.85rem; margin-top: 4px; color: var(--text-muted); }
```

- [ ] **Step 2: 提交**

```bash
git add qianduan/css/skins/ink.css
git commit -m "feat: add question bank styles to ink theme"
```

---

### Task 8: 修改 dark.css — 题库暗黑样式

**Files:**
- Modify: `qianduan/css/skins/dark.css`

- [ ] **Step 1: 追加暗黑覆盖样式**

```css
/* ══════════════════════════════════════════════════════════
   Question Bank (Dark Mode)
   ══════════════════════════════════════════════════════════ */

.qb-card { background: var(--surface); border-color: var(--border); }
.qb-option { background: var(--surface); border-color: var(--border); }
.qb-option:hover { background: #2d2615; }
.qb-correct { background: #064e3b; border-color: #059669; color: #6ee7b7; }
.qb-wrong { background: #450a0a; border-color: #dc2626; color: #fca5a5; }
.qb-answer { background: #064e3b; border-color: #059669; }
.qb-answer-correct { color: #6ee7b7; }
.qb-challenge-card { background: var(--surface); border-color: var(--border); }
.qb-challenge-option { background: var(--surface); border-color: var(--border); color: var(--text); }
.qb-challenge-option:hover { border-color: var(--amber); background: #2d2615; }
.qb-challenge-selected { border-color: var(--amber); background: #2d2615; }
.qb-result-summary { background: #2d2615; }
.qb-result-correct { background: #064e3b; border-color: #059669; }
.qb-result-wrong { background: #450a0a; border-color: #dc2626; }
.qb-result-right { color: #6ee7b7; }
```

- [ ] **Step 2: 提交**

```bash
git add qianduan/css/skins/dark.css
git commit -m "feat: add question bank dark mode styles"
```

---

### Task 9: 端到端验证 + 提交

- [ ] **Step 1: 编译后端**

```bash
cd E:/大三下/移动应用/11_2/JellyStudy && mvn compile -pl jelly-study -am -q
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 重启后端 + 重建前端**

```bash
# 杀旧进程
powershell -Command "$p = (Get-NetTCPConnection -LocalPort 8086 -ErrorAction SilentlyContinue).OwningProcess; Stop-Process -Id $p -Force"
# 重启后端
mvn spring-boot:run -pl jelly-study -q &
# 重建前端
docker-compose build qianduan && docker-compose up -d qianduan
```

- [ ] **Step 3: API 测试**

```bash
# 获取题目数量
curl -s http://localhost:8086/api/question-bank/knowledge-point/{某个kpId}/count

# AI 生成题目
curl -s -X POST http://localhost:8086/api/question-bank/generate \
  -H Content-Type:application/json \
  -d '{"knowledgePointId":"KpId","knowledgePointTitle":"测试","knowledgePointContent":"测试内容","count":3}'
```

- [ ] **Step 4: 最终提交**

```bash
git add -A
git commit -m "feat: complete question bank system with AI generation, browse & challenge modes"
```
