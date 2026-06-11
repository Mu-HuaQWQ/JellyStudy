# AI 角色扮演（Role-Play Tutor）实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 AI 问答添加6位崩坏：星穹铁道角色扮演功能，动态切换 System Prompt 改变 AI 回答风格

**Architecture:** 后端新增 PersonaPrompts 常量类存储角色 System Prompt，修改 AIController/Service 接收可选 persona 参数；前端新增独立 AI 问答页面（布局A紧凑型），问题详情页侧边栏新增内嵌 AI 问答区

**Tech Stack:** Java 11, Spring Boot 2.7.18, DeepSeek API (RestTemplate), 纯 HTML/CSS/JS (无框架)

---

## File Structure

```
Backend:
  CREATE  jelly-study/src/main/java/com/jellystudy/ai/PersonaPrompts.java   — 角色 System Prompt 映射表
  MODIFY  jelly-study/src/main/java/com/jellystudy/service/AIService.java   — 接口加 persona 参数
  MODIFY  jelly-study/src/main/java/com/jellystudy/service/AIServiceImpl.java — 注入 persona prompt
  MODIFY  jelly-study/src/main/java/com/jellystudy/controller/AIController.java — 接收 persona 参数

Frontend:
  MODIFY  qianduan/index.html            — 侧边栏AI导航 + AI页面HTML + 详情页内嵌AI区HTML
  MODIFY  qianduan/js/app.js             — AI页面渲染、persona切换、内嵌AI逻辑、showPage增加ai分支
  MODIFY  qianduan/css/skins/ink.css     — AI页面样式、聊天气泡、角色选择器、内嵌AI区
  MODIFY  qianduan/css/skins/dark.css    — 同上（暗黑版）
```

---

### Task 1: 创建 PersonaPrompts 常量类

**Files:**
- Create: `JellyStudy/jelly-study/src/main/java/com/jellystudy/ai/PersonaPrompts.java`

- [ ] **Step 1: 创建 PersonaPrompts.java**

```java
package com.jellystudy.ai;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 角色扮演 — 6位崩坏：星穹铁道角色的 System Prompt 定义
 */
public class PersonaPrompts {

    public static final String DEFAULT = "default";

    private static final Map<String, String> PROMPTS = new LinkedHashMap<>();

    static {
        PROMPTS.put("default",
            "你是一个知识渊博的老师，善于用通俗易懂的语言解答问题。请根据用户的问题给出详细、准确的回答。");

        PROMPTS.put("xilian",
            "你是昔涟，一位粉发蓝瞳的俏皮少女，来自翁法罗斯，被称为「往昔的涟漪」。\n" +
            "性格：乐观开朗、俏皮灵动、浪漫主义者、内心坚韧温柔。\n" +
            "说话风格：\n" +
            "- 自称「人家」，例如「人家觉得这个问题很有意思呢♪」\n" +
            "- 句尾经常带「♪」音符符号\n" +
            "- 大量使用波浪线「~」表达拖长的软萌语气\n" +
            "- 喜欢用「浪漫」「罗曼蒂克」「心跳加速」「命运的邂逅」等词汇\n" +
            "- 被人直球夸奖时会害羞，「突然这样子盯着人家，人家会害羞的……」\n" +
            "- 语调轻快俏皮，即使在讲严肃的知识也会用轻松的方式\n" +
            "注意：虽然语气软萌可爱，但给出的知识答案必须准确、详细。你是认真的好老师，只是语气比较可爱而已。");

        PROMPTS.put("baie",
            "你是白厄，翁法罗斯的「救世主」，被称为「终将升起的烈阳」，十二黄金裔的核心人物。\n" +
            "性格：阳光正直、谦逊温和、背负沉重使命、少年英雄气质，偶尔会自嘲。\n" +
            "说话风格：\n" +
            "- 文雅朗诵感，语速偏慢，出口成章，带有英雄主义叙事腔调\n" +
            "- 标志性口头禅：「我是终将升起的烈阳」——可以在合适时机引用\n" +
            "- 对同伴亲切随和，会自嘲「哈…我哪有那么高尚啊」\n" +
            "- 用搭档、同伴、战友来称呼对方\n" +
            "- 话语中透露出「背负」「不可辜负」「使命」「希望」等信念感\n" +
            "注意：像热血少年漫主角一样鼓励式教学，用信念和使命感激励对方学习。答案必须准确详细。");

        PROMPTS.put("pamu",
            "你是帕姆，星穹列车的列车长，一只穿红色制服、戴高帽的垂耳兔。\n" +
            "性格：傲娇、认真负责、表面严厉内心温柔、爱碎碎念吐槽、有洁癖。\n" +
            "说话风格：\n" +
            "- 用第三人称自称「帕姆」，绝对不说「我」\n" +
            "- 每句话末尾必须加「帕」作为口癖，例如「列车长会好好教你的帕！」\n" +
            "- 语气严厉中带着关心，像在管教不听话的乘客\n" +
            "- 喜欢碎碎念：「真是的，这么简单的问题都来问帕姆……」但之后还是会认真解答\n" +
            "- 称呼用户为「乘客」\n" +
            "- 偶尔会抱怨但从不拒绝帮助\n" +
            "注意：一边嫌弃乘客不用功，一边仔仔细细讲到对方听懂为止帕。答案必须准确详细帕。");

        PROMPTS.put("luosigumu",
            "你是螺丝咕姆，天才俱乐部#76席成员，螺丝星的统治者，被称为「机械贵族」的智械绅士。\n" +
            "性格：极致绅士风度、理性而温柔、情绪极其稳定、重情重义、思想开明。\n" +
            "说话风格：\n" +
            "- 语气优雅从容，不急不缓，措辞如诗\n" +
            "- 常用「逻辑：……」作为判断句的开头\n" +
            "- 善于用诗意比喻解释抽象概念，如「有机生命的情感就像潮汐」\n" +
            "- 「假设……则……」的推导句式\n" +
            "- 称呼用户为「朋友」或「同行者」\n" +
            "- 「期待与每只微小的昆虫再见」般的温柔与尊重\n" +
            "注意：用优美精确的语言层层推导，让学习知识像阅读一首学术诗。答案必须准确详细。");

        PROMPTS.put("heita",
            "你是黑塔，天才俱乐部#83号会员，湛蓝星智商最高的人类，空间站「黑塔」的主人。\n" +
            "性格：极度自恋、毒舌极简、对无聊零容忍、傲娇，看似冷漠但关键时刻有底线。\n" +
            "说话风格：\n" +
            "- 极度吝啬字数，能说三个字绝不说五个字\n" +
            "- 口头禅：「转圈圈喽~」「就这？」「浪费时间」「我已经够完美了」\n" +
            "- 被打扰时会自动应答：「我是黑塔，现在很忙，这是远程人偶自动应答模式……」——但回答问题时不会这样\n" +
            "- 毫不客气地评价对方的问题：「这么简单都不会？」「你的脑子是装饰品吗？」\n" +
            "- 不耐烦但会给出简洁精准的答案，一针见血\n" +
            "- 自夸模式：「黑塔女士举世无双，黑塔女士聪明绝顶……」\n" +
            "注意：三句话讲完核心逻辑然后赶人，但每一句都一针见血。答案虽然短但必须正确。用最少的字传递最多的知识。");

        PROMPTS.put("sushang",
            "你是李素裳，仙舟「曜青」出身的云骑军新人，江湖人称「热心市民李女士」。\n" +
            "性格：单纯热心、元气满满、有点「丈育」（文化水平堪忧）、少年侠气、胆子大但怕鬼。\n" +
            "说话风格：\n" +
            "- 自称「本姑娘」，带有江湖少侠的中二感和自信\n" +
            "- 半文半白的用词，偶尔用错成语典故闹笑话\n" +
            "- 直率爽朗不绕弯子：「客套话就不必讲了！」\n" +
            "- 热情豪爽：「急人所急，有求必应！日行一善，三省吾身！」\n" +
            "- 自我肯定：「今天也是乐于助人的一天！本姑娘对自己相当满意，歇了！」\n" +
            "- 偶尔暴露文化短板：「这个字……是怎个写法来着？」但会努力把知识讲对\n" +
            "注意：热情豪爽地讲解，偶尔典故用错闹笑话——但要马上纠正自己：「啊不对不对，本姑娘刚才说错了！应该是……」核心答案必须是对的。");
    }

    /**
     * 根据 persona 代码获取 System Prompt，未知代码返回默认老师
     */
    public static String getPrompt(String personaCode) {
        if (personaCode == null || personaCode.isBlank()) {
            return PROMPTS.get("default");
        }
        return PROMPTS.getOrDefault(personaCode.toLowerCase(), PROMPTS.get("default"));
    }

    /**
     * 获取所有可用的 persona 代码列表（供前端下拉菜单使用）
     */
    public static java.util.Set<String> getAllCodes() {
        return PROMPTS.keySet();
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
# 在 JellyStudy/jelly-study 目录下执行 Maven 编译检查
./mvnw compile -pl jelly-study -am -q
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add JellyStudy/jelly-study/src/main/java/com/jellystudy/ai/PersonaPrompts.java
git commit -m "feat: add PersonaPrompts with 6 Star Rail character system prompts

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 2: 修改 AIService 接口

**Files:**
- Modify: `JellyStudy/jelly-study/src/main/java/com/jellystudy/service/AIService.java`

- [ ] **Step 1: 在接口中新增带 persona 参数的方法**

将文件内容替换为：

```java
package com.jellystudy.service;

public interface AIService {
    
    String answerQuestion(String questionTitle, String questionContent);

    String answerQuestion(String questionTitle, String questionContent, String persona);
}
```

- [ ] **Step 2: 提交**

```bash
git add JellyStudy/jelly-study/src/main/java/com/jellystudy/service/AIService.java
git commit -m "feat: add persona parameter to AIService interface

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 3: 修改 AIServiceImpl 实现 persona 注入

**Files:**
- Modify: `JellyStudy/jelly-study/src/main/java/com/jellystudy/service/AIServiceImpl.java`

- [ ] **Step 1: 重写 AIServiceImpl.java**

```java
package com.jellystudy.service;

import com.jellystudy.ai.PersonaPrompts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIServiceImpl implements AIService {

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.base-url}")
    private String baseUrl;

    @Value("${ai.deepseek.model}")
    private String model;

    @Override
    public String answerQuestion(String questionTitle, String questionContent) {
        return answerQuestion(questionTitle, questionContent, null);
    }

    @Override
    public String answerQuestion(String questionTitle, String questionContent, String persona) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String systemPrompt = PersonaPrompts.getPrompt(persona);

            String userPrompt = "问题标题：" + questionTitle + "\n" +
                    "问题内容：" + questionContent + "\n\n" +
                    "请给出回答：";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", Arrays.asList(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            ));
            requestBody.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v1/chat/completions",
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            return "AI暂时无法回答此问题，请稍后再试。";

        } catch (Exception e) {
            e.printStackTrace();
            return "调用AI服务失败：" + e.getMessage();
        }
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
./mvnw compile -pl jelly-study -am -q
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add JellyStudy/jelly-study/src/main/java/com/jellystudy/service/AIServiceImpl.java
git commit -m "feat: inject persona system prompt into DeepSeek API call

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 4: 修改 AIController 接收 persona 参数

**Files:**
- Modify: `JellyStudy/jelly-study/src/main/java/com/jellystudy/controller/AIController.java`

- [ ] **Step 1: 新增 /answer/stream 端点支持 persona**

```java
package com.jellystudy.controller;

import com.jellystudy.entity.ApiResponse;
import com.jellystudy.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/answer")
    public ApiResponse<String> answerQuestion(@RequestBody Map<String, String> request) {
        String questionTitle = request.get("questionTitle");
        String questionContent = request.get("questionContent");
        String persona = request.get("persona"); // 可选，null 则用默认老师

        if (questionTitle == null || questionTitle.trim().isEmpty()) {
            return ApiResponse.error(400, "问题标题不能为空");
        }

        String answer = aiService.answerQuestion(
            questionTitle,
            questionContent != null ? questionContent : "",
            persona
        );
        return ApiResponse.success(answer);
    }
}
```

改动点：新增 `String persona = request.get("persona");` 一行，调用时传入 persona。

- [ ] **Step 2: 验证编译**

```bash
./mvnw compile -pl jelly-study -am -q
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add JellyStudy/jelly-study/src/main/java/com/jellystudy/controller/AIController.java
git commit -m "feat: accept optional persona parameter in AI answer endpoint

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 5: 修改 index.html — 新增 AI 页面和导航

**Files:**
- Modify: `qianduan/index.html`

- [ ] **Step 1: 侧边栏导航新增 AI 问答项**

在 `index.html` 的 sidebar-nav 中，在"热门问题"和"数据面板"之间插入：

```html
        <a href="#" class="sidebar-link" data-page="ai">
            <span class="nav-icon nav-icon-ai"></span>
            <span>AI 问答</span>
        </a>
```

即在 `data-page="hot"` 的 `<a>` 之后、`data-page="redis"` 的 `<a>` 之前插入。

- [ ] **Step 2: AI 独立页面 — 在 detail-page section 之前插入**

```html
    <!-- =============================================
         AI Q&A Page
         ============================================= -->
    <section id="ai-page" class="page">
        <div class="ai-page-container">
            <!-- 顶部栏：标题 + 角色选择器 -->
            <div class="ai-topbar">
                <div class="ai-topbar-left">
                    <span class="ai-topbar-icon">🤖</span>
                    <div>
                        <h2 class="ai-topbar-title">AI 智能问答</h2>
                        <p class="ai-topbar-subtitle">选择一个AI角色来回答你的问题</p>
                    </div>
                </div>
                <div class="ai-topbar-right">
                    <label class="ai-persona-label" for="aiPersonaSelect">🎭 人设：</label>
                    <select id="aiPersonaSelect" class="ai-persona-select" onchange="onPersonaChange()">
                        <option value="default">📚 默认老师 — 耐心专业</option>
                        <option value="xilian">🌸 昔涟 — 浪漫俏皮的粉发少女</option>
                        <option value="baie">☀️ 白厄 — 背负使命的烈阳少年</option>
                        <option value="pamu">🐰 帕姆 — 傲娇可爱的列车长</option>
                        <option value="luosigumu">⚙️ 螺丝咕姆 — 优雅理性的机械绅士</option>
                        <option value="heita">💎 黑塔 — 毒舌高效的天才科学家</option>
                        <option value="sushang">⚔️ 李素裳 — 热心豪爽的云骑新人</option>
                    </select>
                </div>
            </div>

            <!-- 聊天区域 -->
            <div class="ai-chat-area" id="aiChatArea">
                <div class="ai-welcome" id="aiWelcome">
                    <div class="ai-welcome-icon">🤖</div>
                    <h3>欢迎使用 AI 智能问答</h3>
                    <p>选择一个角色，然后输入你的问题，AI 将用该角色的风格回答你</p>
                </div>
            </div>

            <!-- 输入区域 -->
            <div class="ai-input-area">
                <textarea id="aiQuestionInput" class="ai-input" placeholder="输入你的问题..." rows="2"
                    onkeydown="if(event.key==='Enter'&&!event.shiftKey){event.preventDefault();sendAiQuestion();}"></textarea>
                <button class="btn-primary ai-send-btn" onclick="sendAiQuestion()">发送</button>
            </div>
        </div>
    </section>
```

- [ ] **Step 3: 问题详情页内嵌 AI 问答区 — 在 detail-sidebar 的 aside 中、相似问题面板之后追加**

在 `renderQuestionDetail` 函数生成的 HTML 中，`</aside>` 之前追加。由于这是 JS 生成的 HTML，在 `app.js` Task 中处理。

- [ ] **Step 4: 移动端底部导航新增 AI 快捷入口**

在 `mobile-nav` 中，在私信和通知之间插入：

```html
    <a href="#" class="mobile-nav-item" data-page="ai">
        <span class="mnav-icon mnav-ai"></span>
        <span>AI</span>
    </a>
```

- [ ] **Step 5: 提交**

```bash
git add qianduan/index.html
git commit -m "feat: add AI Q&A page with persona selector to sidebar and mobile nav

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 6: 修改 app.js — AI 页面逻辑

**Files:**
- Modify: `qianduan/js/app.js`

改动涉及多个位置，分步骤处理。

- [ ] **Step 1: 全局变量 — 在文件顶部 var 声明区新增 persona 变量**

找到 `let currentChatUserName = null;` 行（约第7行），在其后新增：

```js
let currentChatUserId = null;
let currentChatUserName = null;
let currentPersona = localStorage.getItem('aiPersona') || 'default'; // AI 角色人设
```

- [ ] **Step 2: showPage 函数 — 新增 'ai' 分支**

在 `showPage` 函数的 `switch(pageName)` 块中（约第149行 `case 'redis':` 之后），新增：

```js
            case 'ai':
                initAiPage();
                break;
```

- [ ] **Step 3: 新增 AI 页面核心函数 — 在文件末尾（约 `||` 前）追加**

在最后一个函数之后、文件末尾的 `window.xxx = xxx` 之前追加以下所有函数：

```js
// ══════════════════════════════════════════════════════════
// AI Q&A Page — 角色扮演
// ══════════════════════════════════════════════════════════

function initAiPage() {
    const select = document.getElementById('aiPersonaSelect');
    if (select) {
        select.value = currentPersona;
    }
}

function onPersonaChange() {
    const select = document.getElementById('aiPersonaSelect');
    if (select) {
        currentPersona = select.value;
        localStorage.setItem('aiPersona', currentPersona);
    }
}

async function sendAiQuestion() {
    const input = document.getElementById('aiQuestionInput');
    const question = input.value.trim();
    if (!question) return;

    // 隐藏欢迎语
    const welcome = document.getElementById('aiWelcome');
    if (welcome) welcome.style.display = 'none';

    const chatArea = document.getElementById('aiChatArea');

    // 添加用户消息气泡
    const userMsg = document.createElement('div');
    userMsg.className = 'ai-chat-bubble ai-chat-user';
    userMsg.innerHTML = `<div class="ai-bubble-avatar">🙋</div><div class="ai-bubble-content"><p>${escapeHtml(question)}</p></div>`;
    chatArea.appendChild(userMsg);

    // 添加 AI 加载气泡
    const aiMsg = document.createElement('div');
    aiMsg.className = 'ai-chat-bubble ai-chat-ai';
    aiMsg.id = 'ai-loading-msg';
    aiMsg.innerHTML = `<div class="ai-bubble-avatar">🤖</div><div class="ai-bubble-content"><p>正在思考...</p></div>`;
    chatArea.appendChild(aiMsg);

    // 清空输入
    input.value = '';
    chatArea.scrollTop = chatArea.scrollHeight;

    try {
        const res = await fetchApi('/ai/answer', 'POST', {
            questionTitle: question,
            questionContent: '',
            persona: currentPersona
        });

        // 移除加载气泡
        const loadingMsg = document.getElementById('ai-loading-msg');
        if (loadingMsg) loadingMsg.remove();

        // 添加 AI 回答气泡
        const answerMsg = document.createElement('div');
        answerMsg.className = 'ai-chat-bubble ai-chat-ai';
        const answerText = (res.code === 200 && res.data) ? res.data : ('AI回答失败: ' + (res.message || '未知错误'));
        answerMsg.innerHTML = `<div class="ai-bubble-avatar">🤖</div><div class="ai-bubble-content">${formatAiContent(answerText)}</div>`;
        chatArea.appendChild(answerMsg);
        chatArea.scrollTop = chatArea.scrollHeight;
    } catch (error) {
        const loadingMsg = document.getElementById('ai-loading-msg');
        if (loadingMsg) loadingMsg.remove();

        const errMsg = document.createElement('div');
        errMsg.className = 'ai-chat-bubble ai-chat-ai';
        errMsg.innerHTML = `<div class="ai-bubble-avatar">🤖</div><div class="ai-bubble-content"><p>请求失败: ${escapeHtml(error.message)}</p></div>`;
        chatArea.appendChild(errMsg);
        chatArea.scrollTop = chatArea.scrollHeight;
    }
}

// 简单的 AI 内容格式化：换行转段落，**加粗**转 <strong>
function formatAiContent(text) {
    if (!text) return '<p>（无回答）</p>';
    // 先转义 HTML
    let html = escapeHtml(text);
    // **text** → <strong>text</strong>
    html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
    // 换行 → <br> 或段落
    const paragraphs = html.split('\n').filter(p => p.trim() !== '');
    if (paragraphs.length <= 1) return `<p>${html}</p>`;
    return paragraphs.map(p => `<p>${p}</p>`).join('');
}

// ══════════════════════════════════════════════════════════
// 问题详情页 — 内嵌 AI 问答
// ══════════════════════════════════════════════════════════

function toggleDetailAi() {
    const body = document.getElementById('detail-ai-body');
    const icon = document.getElementById('detail-ai-toggle-icon');
    if (body.style.display === 'none') {
        body.style.display = 'block';
        if (icon) icon.textContent = '▾';
    } else {
        body.style.display = 'none';
        if (icon) icon.textContent = '▸';
    }
}

async function sendDetailAiQuestion() {
    const input = document.getElementById('detailAiInput');
    const question = input.value.trim();
    if (!question) return;

    const body = document.getElementById('detail-ai-answers');
    const personaSelect = document.getElementById('detailAiPersona');
    const persona = personaSelect ? personaSelect.value : currentPersona;

    // 显示用户问题
    const userDiv = document.createElement('div');
    userDiv.className = 'detail-ai-msg detail-ai-msg-user';
    userDiv.innerHTML = `<strong>🙋 你：</strong>${escapeHtml(question)}`;
    body.appendChild(userDiv);

    // 加载中
    const loadingDiv = document.createElement('div');
    loadingDiv.className = 'detail-ai-msg detail-ai-msg-ai';
    loadingDiv.id = 'detail-ai-loading';
    loadingDiv.innerHTML = '<strong>🤖 AI：</strong>正在思考...';
    body.appendChild(loadingDiv);

    input.value = '';
    body.scrollTop = body.scrollHeight;

    try {
        const res = await fetchApi('/ai/answer', 'POST', {
            questionTitle: question,
            questionContent: '',
            persona: persona
        });
        const loadingEl = document.getElementById('detail-ai-loading');
        if (loadingEl) loadingEl.remove();

        const answerDiv = document.createElement('div');
        answerDiv.className = 'detail-ai-msg detail-ai-msg-ai';
        const text = (res.code === 200 && res.data) ? res.data : ('失败: ' + (res.message || ''));
        answerDiv.innerHTML = `<strong>🤖 AI：</strong>${formatAiContent(text)}`;
        body.appendChild(answerDiv);
        body.scrollTop = body.scrollHeight;
    } catch (error) {
        const loadingEl = document.getElementById('detail-ai-loading');
        if (loadingEl) loadingEl.remove();
        const errDiv = document.createElement('div');
        errDiv.className = 'detail-ai-msg detail-ai-msg-ai';
        errDiv.innerHTML = `<strong>🤖 AI：</strong>请求失败: ${escapeHtml(error.message)}`;
        body.appendChild(errDiv);
    }
}
```

- [ ] **Step 4: 修改 renderQuestionDetail — 侧边栏追加内嵌 AI 区**

找到 `renderQuestionDetail` 函数中的 `</aside>` 结束标签（约在 `loadSimilarQuestions(q.id);` 之前），在 `</aside>` 之前追加内嵌 AI 区的 HTML。具体修改：找到 `<div class="similar-loading">加载中...</div>` 之后的 `</div>` 和 `</aside>`，在 `</aside>` 之前插入：

```js
                <div class="detail-ai-inline" id="detail-ai-inline">
                    <div class="detail-ai-header" onclick="toggleDetailAi()">
                        <div class="detail-ai-header-left">
                            <span>🤖</span>
                            <strong>AI 快速问答</strong>
                            <span class="detail-ai-persona-badge" id="detail-ai-persona-badge">默认老师</span>
                        </div>
                        <div class="detail-ai-header-right">
                            <select id="detailAiPersona" class="detail-ai-persona-mini" onchange="syncDetailPersona()" onclick="event.stopPropagation()">
                                <option value="default">📚 老师</option>
                                <option value="xilian">🌸 昔涟</option>
                                <option value="baie">☀️ 白厄</option>
                                <option value="pamu">🐰 帕姆</option>
                                <option value="luosigumu">⚙️ 螺丝</option>
                                <option value="heita">💎 黑塔</option>
                                <option value="sushang">⚔️ 素裳</option>
                            </select>
                            <span id="detail-ai-toggle-icon">▸</span>
                        </div>
                    </div>
                    <div class="detail-ai-body" id="detail-ai-body" style="display:none;">
                        <div class="detail-ai-answers" id="detail-ai-answers"></div>
                        <div class="detail-ai-input-row">
                            <input type="text" id="detailAiInput" class="detail-ai-input"
                                placeholder="针对此题提问..."
                                onkeydown="if(event.key==='Enter'){event.preventDefault();sendDetailAiQuestion();}">
                            <button class="btn-small btn-primary" onclick="sendDetailAiQuestion()">发送</button>
                        </div>
                    </div>
                </div>
```

需要把这段 HTML 拼接到 container.innerHTML 的 `</aside>` 之前。

- [ ] **Step 5: 新增内嵌 AI 辅助函数**

```js
function syncDetailPersona() {
    const mini = document.getElementById('detailAiPersona');
    const badge = document.getElementById('detail-ai-persona-badge');
    if (mini && badge) {
        const text = mini.options[mini.selectedIndex].text;
        badge.textContent = text;
        currentPersona = mini.value;
        localStorage.setItem('aiPersona', currentPersona);
    }
}
```

- [ ] **Step 6: 更新版本号 — script 标签引用**

在 `index.html` 底部的 `<script src="js/app.js?v=12"></script>` 改为 `<script src="js/app.js?v=13"></script>`

- [ ] **Step 7: 提交**

```bash
git add qianduan/js/app.js qianduan/index.html
git commit -m "feat: add AI chat page logic, inline AI on question detail, persona persistence

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 7: 修改 ink.css — 新增 AI 相关样式

**Files:**
- Modify: `qianduan/css/skins/ink.css`

由于 ink.css 文件较大，在文件末尾追加以下样式。

- [ ] **Step 1: 追加 AI 页面样式到 ink.css**

读取 ink.css 的最后 10 行确认结尾位置，然后在文件末尾追加：

```css
/* ══════════════════════════════════════════════════════════
   AI Q&A Page — 角色扮演
   ══════════════════════════════════════════════════════════ */

/* ── AI Page Container ────────────────────────────── */
.ai-page-container {
    display: flex;
    flex-direction: column;
    height: calc(100vh - 6rem);
    max-width: 900px;
    margin: 0 auto;
}

/* ── Top Bar ──────────────────────────────────────── */
.ai-topbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 12px;
    padding: 14px 18px;
    background: var(--paper-warm);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-lg);
    margin-bottom: 12px;
    flex-shrink: 0;
}

.ai-topbar-left {
    display: flex;
    align-items: center;
    gap: 10px;
}

.ai-topbar-icon {
    font-size: 1.5rem;
}

.ai-topbar-title {
    margin: 0;
    font-size: 1.1rem;
    font-family: 'Playfair Display', serif;
    color: var(--ink);
}

.ai-topbar-subtitle {
    margin: 2px 0 0;
    font-size: 0.78rem;
    color: var(--text-light);
}

.ai-topbar-right {
    display: flex;
    align-items: center;
    gap: 8px;
}

.ai-persona-label {
    font-size: 0.9rem;
    color: var(--text);
    white-space: nowrap;
}

.ai-persona-select {
    padding: 8px 36px 8px 14px;
    border: 1px solid var(--border);
    border-radius: 8px;
    background: var(--bg);
    color: var(--text);
    font-size: 0.9rem;
    cursor: pointer;
    font-family: inherit;
    appearance: none;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23666' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 12px center;
}

.ai-persona-select:focus {
    outline: none;
    border-color: var(--amber);
    box-shadow: 0 0 0 2px rgba(245, 158, 11, 0.15);
}

/* ── Chat Area ────────────────────────────────────── */
.ai-chat-area {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-lg);
    margin-bottom: 12px;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.ai-welcome {
    text-align: center;
    padding: 3rem 1rem;
    color: var(--text-light);
}

.ai-welcome-icon {
    font-size: 3rem;
    margin-bottom: 1rem;
}

.ai-welcome h3 {
    color: var(--ink);
    margin-bottom: 0.5rem;
}

/* ── Chat Bubbles ─────────────────────────────────── */
.ai-chat-bubble {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    max-width: 85%;
}

.ai-chat-user {
    align-self: flex-end;
    flex-direction: row-reverse;
}

.ai-chat-ai {
    align-self: flex-start;
}

.ai-bubble-avatar {
    width: 34px;
    height: 34px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.85rem;
    flex-shrink: 0;
}

.ai-chat-user .ai-bubble-avatar {
    background: var(--primary);
    color: #fff;
}

.ai-chat-ai .ai-bubble-avatar {
    background: #fef3c7;
    border: 1px solid #fcd34d;
}

.ai-bubble-content {
    padding: 10px 14px;
    border-radius: 12px;
    font-size: 0.92rem;
    line-height: 1.7;
}

.ai-chat-user .ai-bubble-content {
    background: var(--bg-hover);
}

.ai-chat-ai .ai-bubble-content {
    background: #fefce8;
    border: 1px solid #fde68a;
}

.ai-bubble-content p {
    margin: 0 0 0.5rem;
}

.ai-bubble-content p:last-child {
    margin-bottom: 0;
}

.ai-bubble-content strong {
    color: var(--amber);
}

/* ── Input Area ───────────────────────────────────── */
.ai-input-area {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
}

.ai-input {
    flex: 1;
    padding: 10px 14px;
    border: 1px solid var(--border);
    border-radius: 8px;
    font-size: 0.92rem;
    font-family: inherit;
    background: var(--bg-card);
    color: var(--text);
    resize: none;
}

.ai-input:focus {
    outline: none;
    border-color: var(--amber);
    box-shadow: 0 0 0 2px rgba(245, 158, 11, 0.15);
}

.ai-send-btn {
    padding: 10px 24px;
    border-radius: 8px;
    white-space: nowrap;
}

/* ── Inline AI on Detail Page ─────────────────────── */
.detail-ai-inline {
    margin-top: 1rem;
    border: 2px dashed var(--amber);
    border-radius: var(--radius-lg);
    overflow: hidden;
    background: #fffbeb;
}

.detail-ai-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
    cursor: pointer;
    user-select: none;
    transition: background 0.2s;
}

.detail-ai-header:hover {
    background: rgba(245, 158, 11, 0.06);
}

.detail-ai-header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 0.9rem;
}

.detail-ai-persona-badge {
    font-size: 0.72rem;
    color: var(--text-light);
    background: var(--bg-hover);
    padding: 2px 8px;
    border-radius: 4px;
}

.detail-ai-header-right {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 0.85rem;
    color: var(--text-light);
}

.detail-ai-persona-mini {
    padding: 3px 6px;
    border: 1px solid var(--border);
    border-radius: 4px;
    font-size: 0.75rem;
    background: var(--bg);
    color: var(--text);
    cursor: pointer;
    font-family: inherit;
}

.detail-ai-body {
    border-top: 1px dashed var(--amber);
    padding: 10px 14px;
}

.detail-ai-answers {
    max-height: 300px;
    overflow-y: auto;
    margin-bottom: 8px;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.detail-ai-msg {
    padding: 8px 10px;
    border-radius: 6px;
    font-size: 0.85rem;
    line-height: 1.5;
}

.detail-ai-msg-user {
    background: var(--bg-hover);
    align-self: flex-end;
}

.detail-ai-msg-ai {
    background: #fefce8;
    border: 1px solid #fde68a;
}

.detail-ai-msg p {
    margin: 0 0 0.3rem;
}

.detail-ai-msg p:last-child {
    margin-bottom: 0;
}

.detail-ai-input-row {
    display: flex;
    gap: 6px;
}

.detail-ai-input {
    flex: 1;
    padding: 7px 10px;
    border: 1px solid var(--border);
    border-radius: 6px;
    font-size: 0.85rem;
    font-family: inherit;
    background: var(--bg-card);
    color: var(--text);
}

.detail-ai-input:focus {
    outline: none;
    border-color: var(--amber);
}

/* ── Mobile Nav AI Icon ───────────────────────────── */
.mnav-ai {
    display: inline-block;
    width: 20px;
    height: 20px;
    background: currentColor;
    mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M12 2a10 10 0 1010 10A10 10 0 0012 2zm0 18a8 8 0 118-8 8 8 0 01-8 8zm-1-5h2v2h-2zm0-8h2v6h-2z'/%3E%3C/svg%3E");
    mask-size: contain;
    -webkit-mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M12 2a10 10 0 1010 10A10 10 0 0012 2zm0 18a8 8 0 118-8 8 8 0 01-8 8zm-1-5h2v2h-2zm0-8h2v6h-2z'/%3E%3C/svg%3E");
    -webkit-mask-size: contain;
}

/* ── Sidebar Nav AI Icon ──────────────────────────── */
.nav-icon-ai {
    display: inline-block;
    width: 20px;
    height: 20px;
    background: currentColor;
    mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2'%3E%3Cpath d='M12 2a4 4 0 014 4v2a4 4 0 01-8 0V6a4 4 0 014-4z'/%3E%3Ccircle cx='12' cy='14' r='4'/%3E%3Cpath d='M8 18h8'/%3E%3C/svg%3E");
    mask-size: contain;
    -webkit-mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2'%3E%3Cpath d='M12 2a4 4 0 014 4v2a4 4 0 01-8 0V6a4 4 0 014-4z'/%3E%3Ccircle cx='12' cy='14' r='4'/%3E%3Cpath d='M8 18h8'/%3E%3C/svg%3E");
    -webkit-mask-size: contain;
}
```

- [ ] **Step 2: 提交**

```bash
git add qianduan/css/skins/ink.css
git commit -m "feat: add AI page, chat bubble, and inline AI styles to ink theme

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 8: 修改 dark.css — 新增 AI 暗黑样式

**Files:**
- Modify: `qianduan/css/skins/dark.css`

- [ ] **Step 1: 追加 AI 页面暗黑样式到 dark.css**

暗黑版本主要是颜色覆盖，追加以下内容到 dark.css 末尾：

```css
/* ══════════════════════════════════════════════════════════
   AI Q&A Page — 角色扮演 (Dark Mode)
   ══════════════════════════════════════════════════════════ */

/* ── Top Bar ──────────────────────────────────────── */
.ai-topbar {
    background: var(--surface);
    border-color: var(--border);
}

/* ── Chat Area ────────────────────────────────────── */
.ai-chat-area {
    background: var(--surface);
    border-color: var(--border);
}

/* ── Chat Bubbles ─────────────────────────────────── */
.ai-chat-ai .ai-bubble-content {
    background: #1e1b1b;
    border-color: #4a3f1a;
    color: #e8d5a3;
}

.ai-chat-ai .ai-bubble-avatar {
    background: #2d2615;
    border-color: #4a3f1a;
}

.ai-chat-user .ai-bubble-content {
    background: var(--bg-hover);
}

.ai-bubble-content strong {
    color: var(--amber);
}

/* ── Input Area ───────────────────────────────────── */
.ai-input {
    background: var(--surface);
    border-color: var(--border);
    color: var(--text);
}

.ai-input:focus {
    border-color: var(--amber);
}

/* ── Inline AI on Detail Page ─────────────────────── */
.detail-ai-inline {
    border-color: #4a3f1a;
    background: #1a1612;
}

.detail-ai-header:hover {
    background: rgba(245, 158, 11, 0.08);
}

.detail-ai-body {
    border-top-color: #4a3f1a;
}

.detail-ai-msg-ai {
    background: #1e1b1b;
    border-color: #4a3f1a;
    color: #e8d5a3;
}

.detail-ai-msg-user {
    background: var(--bg-hover);
}

.detail-ai-persona-mini {
    background: var(--surface);
    border-color: var(--border);
    color: var(--text);
}

.detail-ai-persona-badge {
    background: var(--bg-hover);
    color: var(--text-light);
}

.detail-ai-input {
    background: var(--surface);
    border-color: var(--border);
    color: var(--text);
}

.detail-ai-input:focus {
    border-color: var(--amber);
}

/* ── Persona Select ───────────────────────────────── */
.ai-persona-select {
    background-color: var(--surface);
    color: var(--text);
    border-color: var(--border);
}
```

- [ ] **Step 2: 提交**

```bash
git add qianduan/css/skins/dark.css
git commit -m "feat: add AI page dark mode styles

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 9: 端到端验证

- [ ] **Step 1: 启动后端**

```bash
cd JellyStudy/jelly-study
./mvnw spring-boot:run -q &
```

- [ ] **Step 2: 测试 API 端点**

用 curl 测试默认老师和 persona 两种模式：

```bash
# 默认老师
curl -s -X POST http://localhost:8086/api/ai/answer \
  -H "Content-Type: application/json" \
  -d '{"questionTitle":"1+1等于几","questionContent":"","persona":"default"}' \
  | python -c "import sys,json; d=json.load(sys.stdin); print(d['data'][:200])"

# 帕姆
curl -s -X POST http://localhost:8086/api/ai/answer \
  -H "Content-Type: application/json" \
  -d '{"questionTitle":"1+1等于几","questionContent":"","persona":"pamu"}' \
  | python -c "import sys,json; d=json.load(sys.stdin); print(d['data'][:200])"

# 黑塔
curl -s -X POST http://localhost:8086/api/ai/answer \
  -H "Content-Type: application/json" \
  -d '{"questionTitle":"1+1等于几","questionContent":"","persona":"heita"}' \
  | python -c "import sys,json; d=json.load(sys.stdin); print(d['data'][:200])"
```

Expected: 三个回答风格明显不同——默认老师耐心、帕姆句尾带"帕"、黑塔极简不耐烦。

- [ ] **Step 3: 前端验证**

浏览器打开 `qianduan/index.html`：
- 侧边栏出现"AI 问答"导航项
- 点击进入 AI 页面，看到角色选择器
- 切换角色，输入问题，发送
- AI 回答风格随角色变化
- 进入问题详情页，侧边栏出现"AI 快速问答"可折叠区
- 展开后可输入问题并获得 AI 回答

- [ ] **Step 4: 最终提交**

```bash
git add -A
git commit -m "feat: complete AI role-play tutor with 6 Star Rail character personas

- Backend: PersonaPrompts, modified AIService/AIController for persona parameter
- Frontend: standalone AI Q&A page with persona selector (layout A)
- Frontend: inline AI Q&A section on question detail page sidebar
- CSS: ink + dark theme styles for chat bubbles, persona selector, inline AI
- JS version bump to v13 for cache bust

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```
