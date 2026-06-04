# 用户中心前端实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现个人中心页面前端（CSS + JS），并在问题详情页增加收藏/关注/浏览记录功能

**Architecture:** 前端单页应用，CSS 双皮肤系统（ink.css / dark.css 各自独立变量体系），JS 通过 `fetchApi()` 调用后端 REST API。HTML 骨架已存在于 `index.html` 的 `#profile-page` section。

**Tech Stack:** Vanilla JS + CSS（无框架），后端 Spring Boot + MongoDB，REST API 已在 `UserController` 中全部实现

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `qianduan/css/skins/ink.css` | 末尾插入 | 墨韵纸香皮肤的 profile 样式 |
| `qianduan/css/skins/dark.css` | 末尾插入 | 暗黑模式皮肤的 profile 样式 |
| `qianduan/js/app.js` | 多处修改+末尾插入 | profile JS 逻辑、问题详情页改动、侧边栏头像 |
| `qianduan/index.html` | 不改 | 骨架已完成，JS 和 CSS 直接引用现有 id/class |

> **CSS 变量体系不同：** ink.css 用 `--ink`/`--amber`/`--surface`/`--text`/`--border` 等；dark.css 用 `--bg-primary`/`--text-primary`/`--accent-primary`/`--border-subtle` 等。两个文件各自独立定义相同 class，用各自的变量。

---

### Task 1: ink.css — Profile 页面样式

**Files:**
- Modify: `qianduan/css/skins/ink.css` — 在末尾 `}`（最后一个 media query 闭合括号）之后插入

- [ ] **Step 1: 在 ink.css 末尾追加 profile 全部样式**

在 ink.css 文件最末尾（第 2205 行的 `}` 之后）追加：

```css
/* ═══════════════════════════════════════════════════════
   Profile / User Center
   ═══════════════════════════════════════════════════════ */

/* ── Profile Header Card ──────────────────────────── */
.profile-header {
    display: flex;
    align-items: center;
    gap: 1.5rem;
    background: var(--surface);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-lg);
    padding: 1.5rem 1.8rem;
    box-shadow: var(--shadow-sm);
    margin-bottom: 1.25rem;
}

.profile-avatar-wrap {
    position: relative;
    flex-shrink: 0;
}

.profile-avatar-img {
    width: 80px; height: 80px;
    border-radius: 50%;
    object-fit: cover;
    border: 3px solid var(--border);
}

.profile-avatar-fallback {
    width: 80px; height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--amber), var(--amber-glow));
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 2rem;
    font-weight: 700;
    font-family: 'Noto Serif SC', serif;
}

.profile-avatar-upload {
    position: absolute;
    bottom: 2px;
    right: 2px;
    width: 26px; height: 26px;
    border-radius: 50%;
    background: var(--sage);
    color: #fff;
    border: 2px solid #fff;
    font-size: 13px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
    padding: 0;
    transition: background 0.2s;
}
.profile-avatar-upload:hover { background: #4a7a4c; }

/* ── Profile Info ──────────────────────────────────── */
.profile-info {
    flex: 1;
    min-width: 0;
}

.profile-name-row {
    display: flex;
    align-items: center;
    gap: 0.6rem;
    margin-bottom: 0.6rem;
}

.profile-name-row h2 {
    font-family: 'Playfair Display', 'Noto Serif SC', serif;
    font-weight: 700;
    font-size: 1.35rem;
    color: var(--text);
    margin: 0;
}

.profile-title-badge {
    background: linear-gradient(135deg, var(--gold), var(--amber));
    color: #fff;
    padding: 2px 12px;
    border-radius: 20px;
    font-size: 0.78rem;
    font-weight: 600;
    white-space: nowrap;
}

.profile-stats {
    display: flex;
    gap: 1.5rem;
    flex-wrap: wrap;
}

.profile-stat {
    text-align: center;
    min-width: 50px;
}

.profile-stat-num {
    display: block;
    font-size: 1.15rem;
    font-weight: 700;
    color: var(--ink);
    font-family: 'JetBrains Mono', monospace;
}

.profile-stat-label {
    display: block;
    font-size: 0.75rem;
    color: var(--text-muted);
    margin-top: 2px;
}

/* ── Profile Tabs ──────────────────────────────────── */
.profile-tabs {
    display: flex;
    gap: 0;
    border-bottom: 2px solid var(--border-light);
    margin-bottom: 1rem;
    overflow-x: auto;
}

.profile-tab {
    padding: 0.65rem 1.1rem;
    background: none;
    border: none;
    border-bottom: 2px solid transparent;
    margin-bottom: -2px;
    color: var(--text-muted);
    font-family: inherit;
    font-size: 0.9rem;
    font-weight: 500;
    cursor: pointer;
    white-space: nowrap;
    transition: color 0.2s, border-color 0.2s;
}
.profile-tab:hover { color: var(--text); }
.profile-tab.active {
    color: var(--amber);
    border-bottom-color: var(--amber);
}

/* ── Profile Tab Content ───────────────────────────── */
.profile-tab-content {
    min-height: 200px;
}

/* ── History / Favorites / Follow List Items ────────── */
.profile-item-list {
    display: flex;
    flex-direction: column;
    gap: 0.6rem;
}

.profile-item-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: var(--surface);
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
    padding: 0.75rem 1rem;
    box-shadow: var(--shadow-sm);
    transition: box-shadow 0.2s;
}
.profile-item-card:hover { box-shadow: var(--shadow); }

.profile-item-card .item-title {
    flex: 1;
    min-width: 0;
    font-weight: 600;
    color: var(--ink);
    text-decoration: none;
    cursor: pointer;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.profile-item-card .item-title:hover { color: var(--amber); }

.profile-item-card .item-time {
    font-size: 0.78rem;
    color: var(--text-muted);
    margin-left: 1rem;
    white-space: nowrap;
}

.profile-item-card .item-action {
    margin-left: 0.75rem;
    padding: 0.3rem 0.7rem;
    border: 1px solid var(--border);
    border-radius: var(--radius);
    background: var(--surface);
    color: var(--text-muted);
    font-size: 0.78rem;
    cursor: pointer;
    transition: all 0.2s;
    font-family: inherit;
    white-space: nowrap;
}
.profile-item-card .item-action:hover {
    border-color: var(--rust);
    color: var(--rust);
}

/* ── Following / Followers User Item ────────────────── */
.profile-user-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    background: var(--surface);
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
    padding: 0.65rem 1rem;
    box-shadow: var(--shadow-sm);
}

.profile-user-avatar {
    width: 36px; height: 36px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
}

.profile-user-avatar-fb {
    width: 36px; height: 36px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--ink), var(--ink-light));
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 0.9rem;
    font-weight: 600;
    flex-shrink: 0;
}

.profile-user-name {
    flex: 1;
    font-weight: 600;
    color: var(--text);
}

.profile-user-action {
    padding: 0.3rem 0.7rem;
    border: 1px solid var(--border);
    border-radius: var(--radius);
    background: var(--surface);
    color: var(--text-muted);
    font-size: 0.78rem;
    cursor: pointer;
    transition: all 0.2s;
    font-family: inherit;
}
.profile-user-action:hover {
    border-color: var(--rust);
    color: var(--rust);
}

/* ── Titles List (方案 B：列表式) ──────────────────── */
.profile-titles-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.profile-title-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem 1rem;
    border-radius: var(--radius);
    border: 1.5px solid var(--border-light);
    background: var(--surface);
    transition: box-shadow 0.2s;
}

.profile-title-item.unlocked {
    border-color: var(--sage);
    background: var(--sage-light);
}

.profile-title-item.wearing {
    border-color: var(--gold);
    background: rgba(201,164,75,0.08);
}

.profile-title-item.locked {
    opacity: 0.55;
}

.profile-title-icon {
    font-size: 1.6rem;
    flex-shrink: 0;
    width: 36px;
    text-align: center;
}

.profile-title-info {
    flex: 1;
    min-width: 0;
}

.profile-title-name {
    font-weight: 700;
    color: var(--text);
    font-size: 0.95rem;
}

.profile-title-desc {
    font-size: 0.78rem;
    color: var(--text-muted);
    margin-top: 2px;
}

.profile-title-action {
    padding: 0.35rem 0.8rem;
    border-radius: 20px;
    border: none;
    font-family: inherit;
    font-size: 0.78rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;
}

.profile-title-action.wear {
    background: var(--amber);
    color: #fff;
}
.profile-title-action.wear:hover { background: var(--amber-glow); }

.profile-title-action.wearing-badge {
    background: var(--gold);
    color: #fff;
    cursor: default;
}

.profile-title-action.locked-badge {
    background: transparent;
    color: var(--text-light);
    border: 1px solid var(--border);
    cursor: default;
}

/* ── Empty State ────────────────────────────────────── */
.profile-empty {
    text-align: center;
    padding: 3rem 1rem;
    color: var(--text-muted);
    font-size: 0.95rem;
}

/* ── Profile Responsive ──────────────────────────────── */
@media (max-width: 768px) {
    .profile-header {
        flex-direction: column;
        text-align: center;
        padding: 1.25rem;
    }
    .profile-stats {
        justify-content: center;
        gap: 1rem;
    }
    .profile-name-row {
        justify-content: center;
    }
    .profile-tabs {
        gap: 0;
    }
    .profile-tab {
        padding: 0.55rem 0.7rem;
        font-size: 0.82rem;
    }
    .profile-item-card {
        flex-wrap: wrap;
        gap: 0.5rem;
    }
    .profile-item-card .item-time {
        margin-left: 0;
    }
}
```

- [ ] **Step 2: 验证 CSS 文件语法正确**

```bash
# 检查 CSS 大括号是否配对——如果 wc 没有报错就说明格式正常
grep -c '{' qianduan/css/skins/ink.css && grep -c '}' qianduan/css/skins/ink.css
```
期望：两个数字相等

- [ ] **Step 3: Commit**

```bash
git add qianduan/css/skins/ink.css
git commit -m "feat: add profile page styles to ink skin"
```

---

### Task 2: dark.css — Profile 页面样式（暗黑版）

**Files:**
- Modify: `qianduan/css/skins/dark.css` — 在末尾 `}` 之后插入

- [ ] **Step 1: 在 dark.css 末尾追加 profile 全部样式**

在 dark.css 文件最末尾（第 1551 行的 `}` 之后）追加。注意 dark.css 使用 `--bg-primary`/`--text-primary`/`--accent-*`/`--border-subtle` 等变量，不使用 ink.css 的 `--ink`/`--amber` 等：

```css
/* ═══════════════════════════════════════════════════════
   Profile / User Center
   ═══════════════════════════════════════════════════════ */

/* ── Profile Header Card ──────────────────────────── */
.profile-header {
    display: flex;
    align-items: center;
    gap: 1.5rem;
    background: var(--bg-secondary);
    border: 1px solid var(--border-subtle);
    border-radius: var(--radius-lg);
    padding: 1.5rem 1.8rem;
    box-shadow: var(--shadow-sm);
    margin-bottom: 1.25rem;
}

.profile-avatar-wrap {
    position: relative;
    flex-shrink: 0;
}

.profile-avatar-img {
    width: 80px; height: 80px;
    border-radius: 50%;
    object-fit: cover;
    border: 3px solid var(--border-medium);
}

.profile-avatar-fallback {
    width: 80px; height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 2rem;
    font-weight: 700;
    font-family: 'Inter', sans-serif;
}

.profile-avatar-upload {
    position: absolute;
    bottom: 2px;
    right: 2px;
    width: 26px; height: 26px;
    border-radius: 50%;
    background: var(--accent-success);
    color: #fff;
    border: 2px solid var(--bg-secondary);
    font-size: 13px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
    padding: 0;
    transition: background 0.2s;
}
.profile-avatar-upload:hover { background: #0d9668; }

/* ── Profile Info ──────────────────────────────────── */
.profile-info {
    flex: 1;
    min-width: 0;
}

.profile-name-row {
    display: flex;
    align-items: center;
    gap: 0.6rem;
    margin-bottom: 0.6rem;
}

.profile-name-row h2 {
    font-weight: 700;
    font-size: 1.35rem;
    color: var(--text-primary);
    margin: 0;
}

.profile-title-badge {
    background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
    color: #fff;
    padding: 2px 12px;
    border-radius: 20px;
    font-size: 0.78rem;
    font-weight: 600;
    white-space: nowrap;
}

.profile-stats {
    display: flex;
    gap: 1.5rem;
    flex-wrap: wrap;
}

.profile-stat {
    text-align: center;
    min-width: 50px;
}

.profile-stat-num {
    display: block;
    font-size: 1.15rem;
    font-weight: 700;
    color: var(--text-primary);
    font-family: 'JetBrains Mono', monospace;
}

.profile-stat-label {
    display: block;
    font-size: 0.75rem;
    color: var(--text-secondary);
    margin-top: 2px;
}

/* ── Profile Tabs ──────────────────────────────────── */
.profile-tabs {
    display: flex;
    gap: 0;
    border-bottom: 2px solid var(--border-subtle);
    margin-bottom: 1rem;
    overflow-x: auto;
}

.profile-tab {
    padding: 0.65rem 1.1rem;
    background: none;
    border: none;
    border-bottom: 2px solid transparent;
    margin-bottom: -2px;
    color: var(--text-secondary);
    font-family: inherit;
    font-size: 0.9rem;
    font-weight: 500;
    cursor: pointer;
    white-space: nowrap;
    transition: color 0.2s, border-color 0.2s;
}
.profile-tab:hover { color: var(--text-primary); }
.profile-tab.active {
    color: var(--accent-primary);
    border-bottom-color: var(--accent-primary);
}

/* ── Profile Tab Content ───────────────────────────── */
.profile-tab-content {
    min-height: 200px;
}

/* ── History / Favorites / Follow List Items ────────── */
.profile-item-list {
    display: flex;
    flex-direction: column;
    gap: 0.6rem;
}

.profile-item-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: var(--bg-secondary);
    border: 1px solid var(--border-subtle);
    border-radius: var(--radius-md);
    padding: 0.75rem 1rem;
    box-shadow: var(--shadow-sm);
    transition: box-shadow 0.2s;
}
.profile-item-card:hover { box-shadow: var(--shadow-md); }

.profile-item-card .item-title {
    flex: 1;
    min-width: 0;
    font-weight: 600;
    color: var(--text-primary);
    text-decoration: none;
    cursor: pointer;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.profile-item-card .item-title:hover { color: var(--accent-primary); }

.profile-item-card .item-time {
    font-size: 0.78rem;
    color: var(--text-muted);
    margin-left: 1rem;
    white-space: nowrap;
}

.profile-item-card .item-action {
    margin-left: 0.75rem;
    padding: 0.3rem 0.7rem;
    border: 1px solid var(--border-medium);
    border-radius: var(--radius-sm);
    background: var(--bg-tertiary);
    color: var(--text-secondary);
    font-size: 0.78rem;
    cursor: pointer;
    transition: all 0.2s;
    font-family: inherit;
    white-space: nowrap;
}
.profile-item-card .item-action:hover {
    border-color: var(--accent-danger);
    color: var(--accent-danger);
}

/* ── Following / Followers User Item ────────────────── */
.profile-user-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    background: var(--bg-secondary);
    border: 1px solid var(--border-subtle);
    border-radius: var(--radius-md);
    padding: 0.65rem 1rem;
    box-shadow: var(--shadow-sm);
}

.profile-user-avatar {
    width: 36px; height: 36px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
}

.profile-user-avatar-fb {
    width: 36px; height: 36px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 0.9rem;
    font-weight: 600;
    flex-shrink: 0;
}

.profile-user-name {
    flex: 1;
    font-weight: 600;
    color: var(--text-primary);
}

.profile-user-action {
    padding: 0.3rem 0.7rem;
    border: 1px solid var(--border-medium);
    border-radius: var(--radius-sm);
    background: var(--bg-tertiary);
    color: var(--text-secondary);
    font-size: 0.78rem;
    cursor: pointer;
    transition: all 0.2s;
    font-family: inherit;
}
.profile-user-action:hover {
    border-color: var(--accent-danger);
    color: var(--accent-danger);
}

/* ── Titles List ────────────────────────────────────── */
.profile-titles-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.profile-title-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem 1rem;
    border-radius: var(--radius-md);
    border: 1.5px solid var(--border-subtle);
    background: var(--bg-secondary);
    transition: box-shadow 0.2s;
}

.profile-title-item.unlocked {
    border-color: var(--accent-success);
    background: rgba(16,185,129,0.08);
}

.profile-title-item.wearing {
    border-color: var(--accent-primary);
    background: rgba(99,102,241,0.1);
}

.profile-title-item.locked {
    opacity: 0.45;
}

.profile-title-icon {
    font-size: 1.6rem;
    flex-shrink: 0;
    width: 36px;
    text-align: center;
}

.profile-title-info {
    flex: 1;
    min-width: 0;
}

.profile-title-name {
    font-weight: 700;
    color: var(--text-primary);
    font-size: 0.95rem;
}

.profile-title-desc {
    font-size: 0.78rem;
    color: var(--text-secondary);
    margin-top: 2px;
}

.profile-title-action {
    padding: 0.35rem 0.8rem;
    border-radius: 20px;
    border: none;
    font-family: inherit;
    font-size: 0.78rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;
}

.profile-title-action.wear {
    background: var(--accent-primary);
    color: #fff;
}
.profile-title-action.wear:hover { background: var(--accent-secondary); }

.profile-title-action.wearing-badge {
    background: var(--accent-primary);
    color: #fff;
    cursor: default;
    opacity: 0.8;
}

.profile-title-action.locked-badge {
    background: transparent;
    color: var(--text-disabled);
    border: 1px solid var(--border-medium);
    cursor: default;
}

/* ── Empty State ────────────────────────────────────── */
.profile-empty {
    text-align: center;
    padding: 3rem 1rem;
    color: var(--text-muted);
    font-size: 0.95rem;
}

/* ── Profile Responsive ──────────────────────────────── */
@media (max-width: 768px) {
    .profile-header {
        flex-direction: column;
        text-align: center;
        padding: 1.25rem;
    }
    .profile-stats {
        justify-content: center;
        gap: 1rem;
    }
    .profile-name-row {
        justify-content: center;
    }
    .profile-tabs {
        gap: 0;
    }
    .profile-tab {
        padding: 0.55rem 0.7rem;
        font-size: 0.82rem;
    }
    .profile-item-card {
        flex-wrap: wrap;
        gap: 0.5rem;
    }
    .profile-item-card .item-time {
        margin-left: 0;
    }
}
```

- [ ] **Step 2: 验证 CSS 文件语法正确**

```bash
grep -c '{' qianduan/css/skins/dark.css && grep -c '}' qianduan/css/skins/dark.css
```
期望：两个数字相等

- [ ] **Step 3: Commit**

```bash
git add qianduan/css/skins/dark.css
git commit -m "feat: add profile page styles to dark skin"
```

---

### Task 3: app.js — 图片基址常量 + showPage 集成 + 侧边栏头像改造

**Files:**
- Modify: `qianduan/js/app.js` — 开头添加常量、修改 `updateUserDisplay`、`showPage` 添加 profile 分支

- [ ] **Step 1: 在 app.js 开头添加图片基址常量**

在第 1 行 `const API_BASE_URL = ...` 之后添加：

```javascript
const IMAGE_BASE_URL = 'http://localhost:8086';
```

完整上下文（第 1-3 行变为）：

```javascript
const API_BASE_URL = 'http://localhost:8086/api';
const IMAGE_BASE_URL = 'http://localhost:8086';
let currentUserId = localStorage.getItem('userId') || 'user001';
```

- [ ] **Step 2: 修改 updateUserDisplay，侧边栏头像支持真实图片**

找到 `updateUserDisplay` 函数（第 30-33 行）：

```javascript
function updateUserDisplay() {
    const el = document.getElementById('sidebarUserName');
    if (el) el.textContent = currentUserName;
}
```

替换为：

```javascript
async function updateUserDisplay() {
    const nameEl = document.getElementById('sidebarUserName');
    if (nameEl) nameEl.textContent = currentUserName;

    const avatarEl = document.getElementById('sidebarUserAvatar');
    if (!avatarEl) return;

    // 清除旧内容
    avatarEl.innerHTML = '';
    avatarEl.style.backgroundImage = '';

    try {
        const res = await fetchApi(`/users/${currentUserId}`);
        if (res.code === 200 && res.data && res.data.avatar) {
            // 有头像：显示图片
            const img = document.createElement('img');
            img.src = IMAGE_BASE_URL + res.data.avatar;
            img.alt = currentUserName;
            img.style.width = '100%';
            img.style.height = '100%';
            img.style.borderRadius = '50%';
            img.style.objectFit = 'cover';
            img.onerror = () => {
                // 图片加载失败，显示首字母兜底
                img.style.display = 'none';
                avatarEl.textContent = (currentUserName || 'U')[0].toUpperCase();
                avatarEl.style.display = 'flex';
                avatarEl.style.alignItems = 'center';
                avatarEl.style.justifyContent = 'center';
                avatarEl.style.color = '#fff';
                avatarEl.style.fontWeight = '600';
                avatarEl.style.fontSize = '0.85rem';
            };
            avatarEl.appendChild(img);
            return;
        }
    } catch (e) {
        // 请求失败，使用首字母兜底
    }

    // 无头像：显示首字母
    avatarEl.textContent = (currentUserName || 'U')[0].toUpperCase();
    avatarEl.style.display = 'flex';
    avatarEl.style.alignItems = 'center';
    avatarEl.style.justifyContent = 'center';
    avatarEl.style.color = '#fff';
    avatarEl.style.fontWeight = '600';
    avatarEl.style.fontSize = '0.85rem';
}
```

- [ ] **Step 3: 在 showPage 的 switch 中添加 profile 分支**

在 `showPage` 函数（第 87-127 行）的 switch 块中（第 125 行 `}` 之前）添加：

```javascript
        case 'profile':
            loadProfile();
            break;
```

插入位置：在 `case 'messages':` 的 `break;` 之后，switch 的 `}` 之前。

- [ ] **Step 4: 修改 switchUser，调用新的 updateUserDisplay**

`switchUser` 函数中（约第 896 行）已经有 `updateUserDisplay()` 调用，但它现在是 async 的。由于 `switchUser` 没有 await，需要加上 await 或者改为 `updateUserDisplay().catch(() => {})`。

找到第 896 行：
```javascript
        updateUserDisplay();
```

替换为：
```javascript
        updateUserDisplay().catch(() => {});
```

- [ ] **Step 5: 修改 initApp 中的 updateUserDisplay 调用**

第 25 行：
```javascript
    updateUserDisplay();
```

替换为：
```javascript
    updateUserDisplay().catch(() => {});
```

- [ ] **Step 6: Commit**

```bash
git add qianduan/js/app.js
git commit -m "feat: add IMAGE_BASE_URL, sidebar avatar with real image, profile page entry in showPage"
```

---

### Task 4: app.js — Profile 核心加载 + Tab 切换 + 头像上传

**Files:**
- Modify: `qianduan/js/app.js` — 在文件末尾（`initApp` 重写之前，约第 1687 行）插入

- [ ] **Step 1: 添加 loadProfile 函数**

在 `// 格式化时间（HH:mm）` 函数之前（约第 1680 行之前）插入：

```javascript
// ═══════════════════════════════════════════════════════
// Profile / User Center
// ═══════════════════════════════════════════════════════

let currentProfileTab = 'history';

async function loadProfile() {
    try {
        const res = await fetchApi(`/users/${currentUserId}/profile`);
        if (res.code !== 200) {
            alert('加载个人资料失败: ' + res.message);
            return;
        }
        const p = res.data;

        // 头像
        const avatarImg = document.getElementById('profileAvatarImg');
        const avatarFallback = document.getElementById('profileAvatarFallback');
        if (p.avatar) {
            avatarImg.src = IMAGE_BASE_URL + p.avatar;
            avatarImg.style.display = 'block';
            avatarFallback.style.display = 'none';
        } else {
            avatarImg.style.display = 'none';
            avatarFallback.style.display = 'flex';
            avatarFallback.textContent = (p.nickname || p.username || 'U')[0].toUpperCase();
        }

        // 基本信息
        document.getElementById('profileNickname').textContent = p.nickname || p.username;
        document.getElementById('profileTitleBadge').textContent = p.displayTitleName || '新人';

        // 统计数据
        document.getElementById('profileReputation').textContent = p.reputation || 0;
        document.getElementById('profileFollowing').textContent = p.followingCount || 0;
        document.getElementById('profileFollowers').textContent = p.followerCount || 0;
        document.getElementById('profileQuestions').textContent = p.questionCount || 0;
        document.getElementById('profileAnswers').textContent = p.answerCount || 0;

        // 加载当前激活的 tab
        switchProfileTab(currentProfileTab);
    } catch (e) {
        alert('请求失败: ' + e.message);
    }
}
```

- [ ] **Step 2: 添加 switchProfileTab 函数**

紧接着 `loadProfile` 之后插入：

```javascript
function switchProfileTab(tab) {
    currentProfileTab = tab;

    // 更新 tab 激活状态
    document.querySelectorAll('.profile-tab').forEach(t => {
        t.classList.toggle('active', t.dataset.tab === tab);
    });

    // 路由到对应加载函数
    switch (tab) {
        case 'history':
            loadProfileHistory();
            break;
        case 'favorites':
            loadProfileFavorites();
            break;
        case 'following':
            loadProfileFollowing();
            break;
        case 'followers':
            loadProfileFollowers();
            break;
        case 'titles':
            loadProfileTitles();
            break;
    }
}
```

- [ ] **Step 3: 添加 uploadAvatar 函数**

```javascript
async function uploadAvatar(event) {
    const file = event.target.files[0];
    if (!file) return;

    // 文件大小限制 2MB
    if (file.size > 2 * 1024 * 1024) {
        alert('头像文件不能超过 2MB');
        event.target.value = '';
        return;
    }

    // 仅允许图片类型
    if (!file.type.startsWith('image/')) {
        alert('请选择图片文件');
        event.target.value = '';
        return;
    }

    try {
        const formData = new FormData();
        formData.append('file', file);

        const res = await fetch(API_BASE_URL + `/users/${currentUserId}/avatar`, {
            method: 'POST',
            body: formData
        });
        const data = await res.json();
        if (data.code === 200) {
            // 刷新个人中心头像
            const avatarImg = document.getElementById('profileAvatarImg');
            const avatarFallback = document.getElementById('profileAvatarFallback');
            avatarImg.src = IMAGE_BASE_URL + data.data.avatar + '?t=' + Date.now();
            avatarImg.style.display = 'block';
            avatarFallback.style.display = 'none';

            // 刷新侧边栏头像
            updateUserDisplay().catch(() => {});
        } else {
            alert('头像上传失败: ' + data.message);
        }
    } catch (e) {
        alert('上传失败: ' + e.message);
    }
    event.target.value = '';
}
```

- [ ] **Step 4: Commit**

```bash
git add qianduan/js/app.js
git commit -m "feat: add profile loading, tab switching, and avatar upload"
```

---

### Task 5: app.js — Tab 内容渲染函数（history, favorites, following, followers, titles）

**Files:**
- Modify: `qianduan/js/app.js` — 在 profile 代码区（Task 4 插入的代码之后）继续插入

- [ ] **Step 1: 添加 loadProfileHistory 函数**

```javascript
async function loadProfileHistory() {
    const container = document.getElementById('profileTabContent');
    try {
        const res = await fetchApi(`/users/${currentUserId}/history`);
        if (res.code !== 200) {
            container.innerHTML = '<p class="profile-empty">加载失败</p>';
            return;
        }
        const list = res.data || [];
        if (list.length === 0) {
            container.innerHTML = '<p class="profile-empty">暂无浏览记录</p>';
            return;
        }
        container.innerHTML = `
            <div class="profile-item-list">
                ${list.map(h => `
                    <div class="profile-item-card">
                        <span class="item-title" onclick="viewQuestionDetail('${h.questionId}')" title="${escapeHtml(h.questionTitle || '')}">
                            ${escapeHtml(h.questionTitle || '(已删除的问题)')}
                        </span>
                        <span class="item-time">${formatDate(h.viewTime)}</span>
                    </div>
                `).join('')}
            </div>`;
    } catch (e) {
        container.innerHTML = '<p class="profile-empty">加载失败: ' + e.message + '</p>';
    }
}
```

- [ ] **Step 2: 添加 loadProfileFavorites 函数**

```javascript
async function loadProfileFavorites() {
    const container = document.getElementById('profileTabContent');
    try {
        const res = await fetchApi(`/users/${currentUserId}/favorites`);
        if (res.code !== 200) {
            container.innerHTML = '<p class="profile-empty">加载失败</p>';
            return;
        }
        const list = res.data || [];
        if (list.length === 0) {
            container.innerHTML = '<p class="profile-empty">暂无收藏</p>';
            return;
        }
        container.innerHTML = `
            <div class="profile-item-list">
                ${list.map(f => `
                    <div class="profile-item-card">
                        <span class="item-title" onclick="viewQuestionDetail('${f.questionId}')" title="${escapeHtml(f.questionTitle || '')}">
                            ${escapeHtml(f.questionTitle || '(已删除的问题)')}
                        </span>
                        <span class="item-time">${formatDate(f.createTime)}</span>
                        <button class="item-action" onclick="removeProfileFavorite('${f.questionId}')">取消收藏</button>
                    </div>
                `).join('')}
            </div>`;
    } catch (e) {
        container.innerHTML = '<p class="profile-empty">加载失败: ' + e.message + '</p>';
    }
}

async function removeProfileFavorite(questionId) {
    try {
        const res = await fetchApi(`/users/${currentUserId}/favorites?questionId=${questionId}`, 'DELETE');
        if (res.code === 200) {
            loadProfileFavorites();
        } else {
            alert('取消收藏失败: ' + res.message);
        }
    } catch (e) {
        alert('请求失败: ' + e.message);
    }
}
```

- [ ] **Step 3: 添加 loadProfileFollowing 和 loadProfileFollowers 函数**

```javascript
async function loadProfileFollowing() {
    const container = document.getElementById('profileTabContent');
    try {
        const res = await fetchApi(`/users/${currentUserId}/following`);
        if (res.code !== 200) {
            container.innerHTML = '<p class="profile-empty">加载失败</p>';
            return;
        }
        const list = res.data || [];
        if (list.length === 0) {
            container.innerHTML = '<p class="profile-empty">还没有关注任何人</p>';
            return;
        }
        container.innerHTML = `
            <div class="profile-item-list">
                ${list.map(u => `
                    <div class="profile-user-item">
                        ${renderProfileUserAvatar(u)}
                        <span class="profile-user-name">${escapeHtml(u.nickname || u.username)}</span>
                        <button class="profile-user-action" onclick="unfollowUser('${u.id}')">取消关注</button>
                    </div>
                `).join('')}
            </div>`;
    } catch (e) {
        container.innerHTML = '<p class="profile-empty">加载失败: ' + e.message + '</p>';
    }
}

async function loadProfileFollowers() {
    const container = document.getElementById('profileTabContent');
    try {
        const res = await fetchApi(`/users/${currentUserId}/followers`);
        if (res.code !== 200) {
            container.innerHTML = '<p class="profile-empty">加载失败</p>';
            return;
        }
        const list = res.data || [];
        if (list.length === 0) {
            container.innerHTML = '<p class="profile-empty">还没有粉丝</p>';
            return;
        }
        container.innerHTML = `
            <div class="profile-item-list">
                ${list.map(u => `
                    <div class="profile-user-item">
                        ${renderProfileUserAvatar(u)}
                        <span class="profile-user-name">${escapeHtml(u.nickname || u.username)}</span>
                        <span style="font-size:0.78rem;color:var(--text-muted);">${u.reputation || 0} 贡献点</span>
                    </div>
                `).join('')}
            </div>`;
    } catch (e) {
        container.innerHTML = '<p class="profile-empty">加载失败: ' + e.message + '</p>';
    }
}

function renderProfileUserAvatar(u) {
    if (u.avatar) {
        return `<img class="profile-user-avatar" src="${IMAGE_BASE_URL}${u.avatar}" alt="${u.username}" onerror="this.style.display='none';this.nextElementSibling.style.display='flex';" onload="this.nextElementSibling.style.display='none';"><div class="profile-user-avatar-fb" style="display:none;">${(u.nickname || u.username || 'U')[0].toUpperCase()}</div>`;
    }
    return `<div class="profile-user-avatar-fb">${(u.nickname || u.username || 'U')[0].toUpperCase()}</div>`;
}

async function unfollowUser(targetId) {
    if (!confirm('确定取消关注？')) return;
    try {
        const res = await fetchApi(`/users/${currentUserId}/follow?targetId=${targetId}`, 'DELETE');
        if (res.code === 200) {
            loadProfileFollowing();
            // 刷新 header 统计数据
            loadProfileHeaderStats();
        } else {
            alert('取消关注失败: ' + res.message);
        }
    } catch (e) {
        alert('请求失败: ' + e.message);
    }
}
```

- [ ] **Step 4: 添加 loadProfileHeaderStats 辅助函数**

在 `unfollowUser` 之后插入（关注/取关后刷新顶部统计数字）：

```javascript
async function loadProfileHeaderStats() {
    try {
        const res = await fetchApi(`/users/${currentUserId}/profile`);
        if (res.code === 200) {
            const p = res.data;
            document.getElementById('profileFollowing').textContent = p.followingCount || 0;
            document.getElementById('profileFollowers').textContent = p.followerCount || 0;
            document.getElementById('profileReputation').textContent = p.reputation || 0;
        }
    } catch (e) { /* 静默失败 */ }
}
```

- [ ] **Step 5: 添加 loadProfileTitles 函数**

```javascript
async function loadProfileTitles() {
    const container = document.getElementById('profileTabContent');
    try {
        const res = await fetchApi(`/users/${currentUserId}/titles`);
        if (res.code !== 200) {
            container.innerHTML = '<p class="profile-empty">加载失败</p>';
            return;
        }
        const titles = res.data || [];
        if (titles.length === 0) {
            container.innerHTML = '<p class="profile-empty">暂无称号数据</p>';
            return;
        }

        // 图标映射
        const iconMap = {
            'newbie': '🌟',
            'asker': '❓',
            'asker_pro': '💡',
            'answerer': '⭐',
            'scholar': '🎓',
            'master': '👑'
        };

        container.innerHTML = `
            <div class="profile-titles-list">
                ${titles.map(t => {
                    let cls = 'profile-title-item';
                    let actionHtml = '';
                    const icon = iconMap[t.code] || '🏅';
                    if (t.wearing) {
                        cls += ' wearing';
                        actionHtml = '<span class="profile-title-action wearing-badge">佩戴中</span>';
                    } else if (t.unlocked) {
                        cls += ' unlocked';
                        actionHtml = `<button class="profile-title-action wear" onclick="wearTitle('${t.code}')">佩戴</button>`;
                    } else {
                        cls += ' locked';
                        actionHtml = '<span class="profile-title-action locked-badge">未解锁</span>';
                    }
                    return `
                        <div class="${cls}">
                            <span class="profile-title-icon">${icon}</span>
                            <div class="profile-title-info">
                                <div class="profile-title-name">${escapeHtml(t.name)}</div>
                                <div class="profile-title-desc">${escapeHtml(t.requirement)}</div>
                            </div>
                            ${actionHtml}
                        </div>`;
                }).join('')}
            </div>`;
    } catch (e) {
        container.innerHTML = '<p class="profile-empty">加载失败: ' + e.message + '</p>';
    }
}

async function wearTitle(code) {
    try {
        const res = await fetchApi(`/users/${currentUserId}/display-title?code=${encodeURIComponent(code)}`, 'PUT');
        if (res.code === 200) {
            // 刷新称号列表
            loadProfileTitles();
            // 刷新 header 中的佩戴称号
            document.getElementById('profileTitleBadge').textContent =
                res.data.displayTitle ? (res.data.displayTitle === 'newbie' ? '新人' :
                 res.data.displayTitle === 'asker' ? '提问新秀' :
                 res.data.displayTitle === 'asker_pro' ? '提问达人' :
                 res.data.displayTitle === 'answerer' ? '解答之星' :
                 res.data.displayTitle === 'scholar' ? '学者' :
                 res.data.displayTitle === 'master' ? '大师' : res.data.displayTitle) : '';
        } else {
            alert('设置失败: ' + res.message);
        }
    } catch (e) {
        alert('请求失败: ' + e.message);
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add qianduan/js/app.js
git commit -m "feat: add profile tab content renderers (history, favorites, follow, titles)"
```

---

### Task 6: app.js — 问题详情页改动（收藏按钮、关注按钮、浏览记录）

**Files:**
- Modify: `qianduan/js/app.js` — 修改 `renderQuestionDetail` 函数、`viewQuestionDetail` 函数

- [ ] **Step 1: 修改 viewQuestionDetail，进入详情时记录浏览**

找到 `viewQuestionDetail` 函数（第 270-282 行），在 `renderQuestionDetail(res.data);` 之后添加浏览记录调用：

```javascript
async function viewQuestionDetail(questionId) {
    try {
        const res = await fetchApi(`/questions/${questionId}/detail`);
        if (res.code === 200) {
            renderQuestionDetail(res.data);
            showPage('detail');
            // 记录浏览历史（异步，不阻塞页面渲染）
            fetchApi(`/users/${currentUserId}/history?questionId=${questionId}`, 'POST').catch(() => {});
        } else {
            alert('加载问题详情失败: ' + res.message);
        }
    } catch (error) {
        alert('请求失败: ' + error.message);
    }
}
```

- [ ] **Step 2: 修改 renderQuestionDetail，添加收藏按钮和关注按钮**

找到 `renderQuestionDetail` 函数（第 284 行起），在 `detail-actions` 的 div 中（第 331-342 行），在"评论"按钮之后、`isAuthor` 删除按钮之前，添加收藏按钮：

原代码（第 331-342 行）：
```javascript
                <div class="detail-actions">
                    <button class="action-btn ${isLiked(q) ? 'liked' : ''}" onclick="toggleLikeQuestion('${q.id}')">
                        <span class="act-icon act-like"></span> ${q.likeCount || 0}
                    </button>
                    <button class="action-btn" onclick="openAnswerModal('${q.id}')">
                        <span class="act-icon act-answer"></span> 回答
                    </button>
                    <button class="action-btn" onclick="openCommentModal('${q.id}', 'QUESTION')">
                        <span class="act-icon act-comment"></span> 评论
                    </button>
                    ${isAuthor ? '<button class="action-btn" onclick="deleteQuestion(\'' + q.id + '\')"><span class="act-icon act-delete"></span> 删除</button>' : ''}
                </div>
```

替换为：
```javascript
                <div class="detail-actions">
                    <button class="action-btn ${isLiked(q) ? 'liked' : ''}" onclick="toggleLikeQuestion('${q.id}')">
                        <span class="act-icon act-like"></span> ${q.likeCount || 0}
                    </button>
                    <button class="action-btn" onclick="openAnswerModal('${q.id}')">
                        <span class="act-icon act-answer"></span> 回答
                    </button>
                    <button class="action-btn" onclick="openCommentModal('${q.id}', 'QUESTION')">
                        <span class="act-icon act-comment"></span> 评论
                    </button>
                    <button class="action-btn" id="detail-fav-btn-${q.id}" onclick="toggleFavoriteQuestion('${q.id}')">
                        <span class="act-icon">⭐</span> 收藏
                    </button>
                    ${currentUserId && currentUserId !== q.authorId ? `<button class="action-btn" id="detail-follow-btn-${q.authorId}" onclick="toggleFollowUser('${q.authorId}')">
                        <span class="act-icon">👤</span> 关注作者
                    </button>` : ''}
                    ${isAuthor ? '<button class="action-btn" onclick="deleteQuestion(\'' + q.id + '\')"><span class="act-icon act-delete"></span> 删除</button>' : ''}
                </div>
```

然后在 `renderQuestionDetail` 函数的末尾（`loadSimilarQuestions(q.id);` 之后、`}` 闭合之前），添加异步查询收藏/关注状态并更新按钮：

在 `loadSimilarQuestions(q.id);` 之后（第 368 行）添加：

```javascript
    // 异步查询收藏状态
    fetchApi(`/users/${currentUserId}/favorites/status?questionId=${q.id}`)
        .then(r => { if (r.code === 200 && r.data) {
            const btn = document.getElementById('detail-fav-btn-' + q.id);
            if (btn) { btn.classList.add('liked'); btn.innerHTML = '<span class="act-icon">⭐</span> 已收藏'; }
        }}).catch(() => {});
    // 异步查询关注状态
    if (currentUserId && currentUserId !== q.authorId) {
        fetchApi(`/users/${currentUserId}/follow-status?targetId=${q.authorId}`)
            .then(r => { if (r.code === 200 && r.data) {
                const btn = document.getElementById('detail-follow-btn-' + q.authorId);
                if (btn) { btn.classList.add('liked'); btn.innerHTML = '<span class="act-icon">👤</span> 已关注'; }
            }}).catch(() => {});
    }
```

- [ ] **Step 3: 添加 toggleFavoriteQuestion 和 toggleFollowUser 全局函数**

在 profile 代码区（之前插入的 profile 函数之后）添加：

```javascript
// ═══════════════════════════════════════════════════════
// Question Detail: Favorite & Follow
// ═══════════════════════════════════════════════════════

async function toggleFavoriteQuestion(questionId) {
    const btn = document.getElementById('detail-fav-btn-' + questionId);
    const isFavorited = btn && btn.classList.contains('liked');
    try {
        if (isFavorited) {
            const res = await fetchApi(`/users/${currentUserId}/favorites?questionId=${questionId}`, 'DELETE');
            if (res.code === 200) {
                btn.classList.remove('liked');
                btn.innerHTML = '<span class="act-icon">⭐</span> 收藏';
            }
        } else {
            const res = await fetchApi(`/users/${currentUserId}/favorites?questionId=${questionId}`, 'POST');
            if (res.code === 200) {
                btn.classList.add('liked');
                btn.innerHTML = '<span class="act-icon">⭐</span> 已收藏';
            }
        }
    } catch (e) {
        alert('操作失败: ' + e.message);
    }
}

async function toggleFollowUser(targetId) {
    const btn = document.getElementById('detail-follow-btn-' + targetId);
    const isFollowing = btn && btn.classList.contains('liked');
    try {
        if (isFollowing) {
            const res = await fetchApi(`/users/${currentUserId}/follow?targetId=${targetId}`, 'DELETE');
            if (res.code === 200) {
                btn.classList.remove('liked');
                btn.innerHTML = '<span class="act-icon">👤</span> 关注作者';
            }
        } else {
            const res = await fetchApi(`/users/${currentUserId}/follow?targetId=${targetId}`, 'POST');
            if (res.code === 200) {
                btn.classList.add('liked');
                btn.innerHTML = '<span class="act-icon">👤</span> 已关注';
            }
        }
    } catch (e) {
        alert('操作失败: ' + e.message);
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add qianduan/js/app.js
git commit -m "feat: add favorite/follow buttons on question detail, record browse history"
```

---

### Task 7: 验证与收尾

**Files:**
- 无新建

- [ ] **Step 1: 检查 JS 语法完整性**

```bash
# 用 Node.js 语法检查（如果没有 node 则跳过）
node --check qianduan/js/app.js 2>&1 || echo "Node not available, skipping syntax check"
```

- [ ] **Step 2: 确保所有函数全局可见**

`index.html` 中使用了 `onclick="switchProfileTab(...)"`、`onclick="uploadAvatar(event)"` 等内联事件。确认这些函数在 app.js 中都以 `function` 声明定义（非 `const` 箭头函数），确保它们在全局作用域可访问。

检查列表：
- `switchProfileTab(tab)` — ✅ function 声明
- `uploadAvatar(event)` — ✅ async function 声明（内联 onclick 中 async 也能正常调用，返回值被忽略）
- `removeProfileFavorite(questionId)` — ✅ async function 声明
- `unfollowUser(targetId)` — ✅ async function 声明
- `wearTitle(code)` — ✅ async function 声明
- `toggleFavoriteQuestion(questionId)` — ✅ async function 声明
- `toggleFollowUser(targetId)` — ✅ async function 声明

- [ ] **Step 3: Commit**

```bash
git add qianduan/js/app.js
git commit -m "chore: final verification of profile frontend code"
```

---

## 实现顺序

按 Task 1 → 7 顺序执行。每个 Task 内部 Step 按编号顺序执行。

## 依赖关系

```
Task 1 (ink.css) ──┐
                   ├── Task 3 (app.js 基础改造) ── Task 4 (profile 核心) ── Task 5 (tab 渲染)
Task 2 (dark.css) ─┘                                                         Task 6 (问题详情改动)
                                                                              │
                                                                              └── Task 7 (验证)
```

Task 1 和 Task 2 无依赖，可并行。
Task 3 依赖无。
Task 4 依赖 Task 3。
Task 5 依赖 Task 4。
Task 6 与 Task 4/5 无依赖，可并行。
Task 7 依赖所有。
