const API_BASE_URL = 'http://localhost:8086/api';
let currentUserId = localStorage.getItem('userId') || 'user001';
let currentUserName = localStorage.getItem('userName') || 'user001';
// 当前聊天状态（提前声明，避免页面加载时 switchUser 等函数访问到 TDZ）
let currentChatUserId = null;
let currentChatUserName = null;

let currentPage = 0;
const pageSize = 10;

// 通知分页（提前声明，避免 showPage('notifications') 触发 TDZ）
let notificationPage = 0;
const notificationPageSize = 10;

// 用户缓存：id -> { nickname, username }，供活跃用户等展示使用
const usersCache = {};

document.addEventListener('DOMContentLoaded', function() {
    initApp();
});

function initApp() {
    setupNavigation();
    setupForms();
    updateUserDisplay();
    loadHomeData();
    loadUsers();
}

function updateUserDisplay() {
    const el = document.getElementById('sidebarUserName');
    if (el) el.textContent = currentUserName;
}

function setupNavigation() {
    // Sidebar navigation
    document.querySelectorAll('.sidebar-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const page = this.dataset.page;
            showPage(page);
        });
    });

    // Mobile bottom nav
    document.querySelectorAll('.mobile-nav-item').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const page = this.dataset.page;
            showPage(page);
        });
    });

    // Hamburger toggle
    const hamburger = document.getElementById('hamburgerBtn');
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    if (hamburger && sidebar && overlay) {
        hamburger.addEventListener('click', () => {
            sidebar.classList.toggle('open');
            overlay.classList.toggle('open');
        });
        overlay.addEventListener('click', () => {
            sidebar.classList.remove('open');
            overlay.classList.remove('open');
        });
    }
}

function updateNavActive(pageName) {
    document.querySelectorAll('.sidebar-link').forEach(l => {
        l.classList.toggle('active', l.dataset.page === pageName);
    });
    document.querySelectorAll('.mobile-nav-item').forEach(l => {
        l.classList.toggle('active', l.dataset.page === pageName);
    });
}

function setupForms() {
    document.getElementById('askForm').addEventListener('submit', handleAskSubmit);
    document.getElementById('answerForm').addEventListener('submit', handleAnswerSubmit);
    document.getElementById('commentForm').addEventListener('submit', handleCommentSubmit);
    document.getElementById('knowledgeForm').addEventListener('submit', handleKnowledgeSubmit);
    document.getElementById('createUserForm').addEventListener('submit', handleCreateUserSubmit);
}

function showPage(pageName) {
    const pages = document.querySelectorAll('.page');
    pages.forEach(page => page.classList.remove('active'));

    const targetPage = document.getElementById(`${pageName}-page`);
    if (targetPage) {
        targetPage.classList.add('active');
    }

    // Update active states on sidebar and mobile nav
    updateNavActive(pageName);

    // Close mobile sidebar after navigation
    document.getElementById('sidebar').classList.remove('open');
    document.getElementById('sidebarOverlay').classList.remove('open');

    switch(pageName) {
        case 'home':
            loadHomeData();
            break;
        case 'questions':
            loadAllQuestions();
            break;
        case 'knowledge':
            loadKnowledgePoints();
            break;
        case 'hot':
            loadHotQuestions();
            break;
        case 'redis':
            loadRedisData();
            break;
        case 'notifications':
            notificationPage = 0;
            loadNotifications();
            break;
        case 'messages':
            loadContacts();
            break;
    }
}

// Hero search
function searchFromHero() {
    const q = document.getElementById('heroSearchInput').value.trim();
    if (q) {
        document.getElementById('searchInput').value = q;
        showPage('questions');
        searchQuestions();
    }
}

// Filter notifications by tab
let currentNotificationFilter = 'all';
function filterNotifications(filter) {
    currentNotificationFilter = filter;
    document.querySelectorAll('.notif-tab').forEach(tab => {
        tab.classList.toggle('active', tab.textContent.trim() ===
            (filter === 'all' ? '全部' : filter === 'unread' ? '未读' : '已读'));
    });
    notificationPage = 0;
    loadNotifications();
}

async function loadHomeData() {
    try {
        const [countRes, hotRes, recommendRes, knowledgeRes] = await Promise.all([
            fetchApi('/questions/count'),
            fetchApi('/questions/hot?limit=5'),
            fetchApi('/questions/recommend?limit=5'),
            fetchApi('/knowledge-points/count')
        ]);
        
        if (countRes.code === 200) {
            document.getElementById('questionCount').textContent = countRes.data;
        }
        if (hotRes.code === 200) {
            document.getElementById('hotQuestionCount').textContent = hotRes.data.length;
        }
        if (recommendRes.code === 200) {
            renderQuestions(recommendRes.data, 'recommendQuestions');
        }
        if (knowledgeRes.code === 200) {
            document.getElementById('knowledgeCount').textContent = knowledgeRes.data;
        }
    } catch (error) {
        console.error('Failed to load home data:', error);
    }
}

async function searchQuestions() {
    const keyword = (document.getElementById('searchInput')?.value || '').trim();
    if (!keyword) {
        loadAllQuestions();
        return;
    }
    try {
        const res = await fetchApi(`/questions/search?keyword=${encodeURIComponent(keyword)}`);
        if (res.code === 200) {
            renderQuestions(res.data, 'allQuestions');
        }
    } catch (error) {
        console.error('Failed to search questions:', error);
    }
}

async function loadAllQuestions() {
    try {
        const res = await fetchApi(`/questions?page=${currentPage}&size=${pageSize}`);
        if (res.code === 200) {
            renderQuestions(res.data.content, 'allQuestions');
        }
    } catch (error) {
        console.error('Failed to load questions:', error);
    }
}

async function loadHotQuestions() {
    try {
        const res = await fetchApi('/questions/hot?limit=20');
        if (res.code === 200) {
            renderQuestions(res.data, 'hotQuestions');
        }
    } catch (error) {
        console.error('Failed to load hot questions:', error);
    }
}

async function loadKnowledgePoints() {
    try {
        const res = await fetchApi('/knowledge-points');
        if (res.code === 200) {
            renderKnowledgePoints(res.data);
        }
    } catch (error) {
        console.error('Failed to load knowledge points:', error);
    }
}

function renderQuestions(questions, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    
    if (!questions || questions.length === 0) {
        container.innerHTML = '<p>暂无问题</p>';
        return;
    }
    
    container.innerHTML = questions.map(q => `
        <div class="question-card" onclick="viewQuestionDetail('${q.id}')">
            <h3>${escapeHtml(q.title)}</h3>
            <div class="question-meta">
                <span>${q.authorName}</span>
                <span>${q.knowledgePointTitle || '待AI分析'}</span>
                <span>${q.viewCount} 次浏览</span>
                <span>${q.answerCount || 0} 个回答</span>
                <span>${q.likeCount || 0} 赞</span>
            </div>
        </div>
    `).join('');
}

function renderKnowledgePoints(knowledgePoints) {
    const container = document.getElementById('knowledgeList');
    if (!container) return;
    
    if (!knowledgePoints || knowledgePoints.length === 0) {
        container.innerHTML = '<p>暂无知识点</p>';
        return;
    }
    
    container.innerHTML = knowledgePoints.map(kp => `
        <div class="knowledge-card" onclick="loadQuestionsByKnowledgePoint('${kp.id}'); event.stopPropagation();">
            <h3>${escapeHtml(kp.title)}</h3>
            <p>${escapeHtml(kp.content || '')}</p>
            <div class="knowledge-meta">
                <span>难度: ${kp.difficulty || '未评估'}</span>
                <span>问题数: ${kp.questionCount || 0}</span>
            </div>
        </div>
    `).join('');
}

async function viewQuestionDetail(questionId) {
    try {
        const res = await fetchApi(`/questions/${questionId}/detail`);
        if (res.code === 200) {
            renderQuestionDetail(res.data);
            showPage('detail');
        } else {
            alert('加载问题详情失败: ' + res.message);
        }
    } catch (error) {
        alert('请求失败: ' + error.message);
    }
}

function renderQuestionDetail(data) {
    const q = data.question;
    const answers = data.answers || [];
    const acceptedAnswer = data.acceptedAnswer;
    const questionComments = data.questionComments || [];
    const answerCommentsMap = data.answerCommentsMap || {};
    const isAuthor = currentUserId && q.authorId === currentUserId;
    
    const sortedAnswers = [...answers].sort((a, b) => {
        if (acceptedAnswer && a.id === acceptedAnswer.id) return -1;
        if (acceptedAnswer && b.id === acceptedAnswer.id) return 1;
        return 0;
    });
    
    const container = document.getElementById('questionDetail');

    container.innerHTML = `
        <div class="detail-layout">
            <div class="detail-main">
                <div class="detail-header">
                    <h1>${escapeHtml(q.title)}</h1>
                    <div class="detail-meta">
                        <span>${q.authorName}</span>
                        <span>${q.knowledgePointTitle || '待AI分析'}</span>
                        <span>${q.viewCount} 次浏览</span>
                        <span>${answers.length} 个回答</span>
                        <span>${q.likeCount || 0} 赞</span>
                        <span>${formatDate(q.createTime)}</span>
                    </div>
                </div>

                <div class="ai-evaluation-section" id="ai-evaluation-${q.id}">
                    <div class="ai-eval-header">
                        <h3>AI 智能评估</h3>
                        <button class="btn-small btn-secondary" onclick="loadQuestionEvaluation('${q.id}')">刷新</button>
                    </div>
                    <div class="ai-eval-body" id="evaluation-content-${q.id}">
                        <p>正在加载AI评估结果...</p>
                    </div>
                </div>

                <div class="detail-content">${escapeHtml(q.content)}</div>

                <div class="question-tags">
                    ${(q.tags || []).map(tag => `<span class="tag">${escapeHtml(tag)}</span>`).join('')}
                </div>

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

                <div class="answers-section">
                    <h2>${answers.length} 个回答</h2>
                    ${sortedAnswers.map(a => renderAnswerCard(a, acceptedAnswer, answerCommentsMap, isAuthor, q)).join('')}
                </div>

                <div class="comments-section">
                    <h3>${questionComments.length} 条评论</h3>
                    ${questionComments.map(c => renderCommentCard(c)).join('')}
                    <button class="btn-secondary" onclick="openCommentModal('${q.id}', 'QUESTION')" style="margin-top:1rem;">
                        添加评论
                    </button>
                </div>
            </div>

            <aside class="detail-sidebar">
                <div class="similar-questions-panel" id="similar-questions-panel">
                    <h3>相似问题</h3>
                    <div class="similar-loading">加载中...</div>
                </div>
            </aside>
        </div>
    `;

    loadQuestionEvaluation(q.id);
    loadSimilarQuestions(q.id);
}

async function loadQuestionEvaluation(questionId) {
    const contentDiv = document.getElementById(`evaluation-content-${questionId}`);
    if (!contentDiv) return;
    
    contentDiv.innerHTML = '<p style="margin:0;color:var(--text-light);">正在加载AI评估结果...</p>';
    
    try {
        const res = await fetchApi(`/questions/${questionId}/evaluation`);
        if (res.code === 200 && res.data && res.data.success) {
            const evalData = res.data.data;
            contentDiv.innerHTML = `
                <div class="eval-grid">
                    <div class="eval-item">
                        <strong>知识点:</strong>
                        <p>${(evalData.extractedKnowledgePoints || []).join(', ') || '待分析'}</p>
                    </div>
                    <div class="eval-item">
                        <strong>难度:</strong>
                        <p>${evalData.difficultyLevel || '待评估'}</p>
                    </div>
                    <div class="eval-item">
                        <strong>难度说明:</strong>
                        <p>${evalData.difficultyReason || '待分析'}</p>
                    </div>
                </div>
            `;
        } else {
            contentDiv.innerHTML = '<p style="margin:0;color:var(--text-light);">暂无评估结果，AI正在分析中...</p>';
        }
    } catch (error) {
        contentDiv.innerHTML = '<p style="margin:0;color:var(--text-light);">暂无评估结果，AI正在分析中...</p>';
    }
}

async function loadSimilarQuestions(questionId) {
    const panel = document.getElementById('similar-questions-panel');
    if (!panel) return;

    try {
        const res = await fetchApi(`/questions/${questionId}/similar?limit=5`);
        if (res.code === 200 && res.data && res.data.length > 0) {
            panel.innerHTML = `
                <h3>相似问题</h3>
                ${res.data.map(item => `
                    <div class="similar-item" onclick="viewQuestionDetail('${item.id}')">
                        <div class="similar-title">${escapeHtml(item.title)}</div>
                        ${item.knowledgePointTitle ? `<div class="similar-kp">${escapeHtml(item.knowledgePointTitle)}</div>` : ''}
                        <div class="similar-score">
                            <div class="similar-bar">
                                <div class="similar-bar-fill" style="width:${Math.round(item.similarity * 100)}%"></div>
                            </div>
                            <span class="similar-percent">${Math.round(item.similarity * 100)}%</span>
                        </div>
                    </div>
                `).join('')}
            `;
        } else {
            panel.innerHTML = `
                <h3>相似问题</h3>
                <p class="similar-empty">暂无相似问题</p>
            `;
        }
    } catch (error) {
        panel.innerHTML = `
            <h3>相似问题</h3>
            <p class="similar-empty">加载失败</p>
        `;
    }
}

function renderAnswerCard(answer, acceptedAnswer, answerCommentsMap, isAuthor, question) {
    const isAccepted = acceptedAnswer && acceptedAnswer.id === answer.id;
    const comments = answerCommentsMap[answer.id] || [];
    const isAnswerAuthor = currentUserId && answer.authorId === currentUserId;

    return `
        <div class="answer-card ${isAccepted ? 'accepted' : ''}" id="answer-${answer.id}">
            <div class="answer-header">
                <span class="answer-author">${answer.authorName}</span>
                ${isAccepted ? '<span class="accepted-badge">已采纳</span>' : ''}
            </div>
            <div class="answer-content">${escapeHtml(answer.content)}</div>
            <div class="answer-eval" id="answer-eval-${answer.id}">
                <button class="btn-small btn-secondary" onclick="loadAnswerEvaluation('${answer.id}')">查看AI评分</button>
                <div id="answer-eval-content-${answer.id}"></div>
            </div>
            <div class="answer-actions">
                <button class="action-btn ${isAnswerLiked(answer) ? 'liked' : ''}" onclick="toggleLikeAnswer('${answer.id}')">
                    <span class="act-icon act-like"></span> ${answer.likeCount || 0}
                </button>
                <button class="action-btn" onclick="openCommentModal('${answer.id}', 'ANSWER')">
                    <span class="act-icon act-comment"></span> 评论 (${comments.length})
                </button>
                ${!isAccepted && isAuthor ? '<button class="action-btn" onclick="acceptAnswer(\'' + answer.id + '\')"><span class="act-icon act-accept"></span> 采纳</button>' : ''}
                ${isAnswerAuthor ? '<button class="action-btn" onclick="deleteAnswer(\'' + answer.id + '\')"><span class="act-icon act-delete"></span> 删除</button>' : ''}
            </div>
        </div>
    `;
}

async function loadAnswerEvaluation(answerId) {
    console.log('loadAnswerEvaluation called with answerId:', answerId);
    const contentDiv = document.getElementById(`answer-eval-content-${answerId}`);
    console.log('contentDiv:', contentDiv);
    if (!contentDiv) return;
    
    contentDiv.innerHTML = '<p style="margin:0;color:var(--text-light);">正在加载AI评分...</p>';
    
    try {
        const res = await fetchApi(`/answers/${answerId}/evaluation`);
        console.log('Evaluation response:', res);
        console.log('res.data:', res.data);
        console.log('res.data.success:', res.data?.success);
        console.log('res.data.data:', res.data?.data);
        
        let evalData = null;
        if (res.data && res.data.success && res.data.data) {
            evalData = res.data.data;
            console.log('evalData:', evalData);
            console.log('evalData.score:', evalData.score);
            console.log('evalData.evaluationReason:', evalData.evaluationReason);
        } else {
            console.log('No evaluation data! res.data:', res.data);
        }
        
        if (evalData && evalData.score !== undefined) {
            contentDiv.innerHTML = `
                <div class="eval-score-card">
                    <div class="eval-score-row">
                        <strong>AI评分:</strong>
                        <span class="eval-score-num">${evalData.score || '--'}</span>
                        <span>/ 100</span>
                    </div>
                    <div class="eval-score-reason">
                        <strong>评价:</strong> ${evalData.evaluationReason || '待分析'}
                    </div>
                    <button class="btn-small btn-secondary" onclick="reevaluateAnswer('${answerId}')">重新评定</button>
                </div>
            `;
        } else {
            contentDiv.innerHTML = `
                <div class="eval-score-card">
                    <p>暂无评分结果</p>
                    <button class="btn-small btn-secondary" onclick="reevaluateAnswer('${answerId}')">开始评定</button>
                </div>
            `;
        }
    } catch (error) {
        contentDiv.innerHTML = `
            <div class="eval-score-card">
                <p>暂无评分结果</p>
                <button class="btn-small btn-secondary" onclick="reevaluateAnswer('${answerId}')">开始评定</button>
            </div>
        `;
    }
}

async function reevaluateAnswer(answerId) {
    const contentDiv = document.getElementById(`answer-eval-content-${answerId}`);
    if (!contentDiv) return;

    contentDiv.innerHTML = '<p style="margin:0;color:var(--text-light);">正在评估，请稍候...</p>';

    try {
        const answerRes = await fetchApi(`/answers/${answerId}`);
        if (answerRes.code !== 200 || !answerRes.data) {
            throw new Error('获取回答失败');
        }
        const answer = answerRes.data;

        const questionRes = await fetchApi(`/questions/${answer.questionId}`);
        if (questionRes.code !== 200 || !questionRes.data) {
            throw new Error('获取问题失败');
        }
        const question = questionRes.data;

        const reevalRes = await fetchApi(`/answers/${answerId}/re-evaluate`, 'POST', {
            questionId: question.id,
            questionContent: question.content,
            answerContent: answer.content
        });

        // re-evaluate 是同步接口，响应里已包含评估结果，直接渲染
        const evalData = reevalRes?.data?.data;
        if (reevalRes?.code === 200 && reevalRes?.data?.success && evalData && evalData.score != null) {
            contentDiv.innerHTML = `
                <div class="eval-score-card">
                    <div class="eval-score-row">
                        <strong>AI评分:</strong>
                        <span class="eval-score-num">${evalData.score}</span>
                        <span>/ 100</span>
                    </div>
                    <div class="eval-score-reason">
                        <strong>评价:</strong> ${evalData.evaluationReason || evalData.feedback || '待分析'}
                    </div>
                    <button class="btn-small btn-secondary" onclick="reevaluateAnswer('${answerId}')">重新评定</button>
                </div>
            `;
        } else {
            const errMsg = evalData?.errorMessage || reevalRes?.data?.errorMessage || 'AI评估失败，请重试';
            contentDiv.innerHTML = `
                <div class="eval-score-card">
                    <p style="color:var(--text-light);">${errMsg}</p>
                    <button class="btn-small btn-secondary" onclick="reevaluateAnswer('${answerId}')">重新评定</button>
                </div>
            `;
        }
    } catch (error) {
        contentDiv.innerHTML = `
            <div class="eval-score-card">
                <p style="color:var(--text-light);">评估失败：${error.message}</p>
                <button class="btn-small btn-secondary" onclick="reevaluateAnswer('${answerId}')">重新评定</button>
            </div>
        `;
    }
}

function renderCommentCard(comment) {
    return `
        <div class="comment-card">
            <div class="comment-header">
                <span class="comment-author-name">${comment.authorName}</span>
                <span>${formatDate(comment.createTime)}</span>
            </div>
            <div class="comment-content">${escapeHtml(comment.content)}</div>
        </div>
    `;
}

function isLiked(question) {
    return question.likedByUsers && question.likedByUsers.includes(currentUserId);
}

function isAnswerLiked(answer) {
    return answer.likedByUsers && answer.likedByUsers.includes(currentUserId);
}

async function toggleLikeQuestion(questionId) {
    try {
        const liked = isLiked(await getQuestion(questionId));
        const url = liked ? `/questions/${questionId}/unlike` : `/questions/${questionId}/like`;
        const res = await fetchApi(url, 'POST', null, { userId: currentUserId });
        
        if (res.code === 200) {
            viewQuestionDetail(questionId);
        }
    } catch (error) {
        alert('操作失败: ' + error.message);
    }
}

async function deleteQuestion(questionId) {
    if (!confirm('确定要删除这个问题吗？')) return;
    try {
        const res = await fetchApi(`/questions/${questionId}?userId=${currentUserId}`, 'DELETE');
        if (res.code === 200) {
            showPage('home');
            loadHomeData();
        } else {
            alert('删除失败: ' + res.message);
        }
    } catch (error) {
        alert('操作失败: ' + error.message);
    }
}

async function toggleLikeAnswer(answerId) {
    try {
        const answer = await getAnswer(answerId);
        const liked = isAnswerLiked(answer);
        const url = liked ? `/answers/${answerId}/unlike` : `/answers/${answerId}/like`;
        const res = await fetchApi(url, 'POST', null, { userId: currentUserId });
        
        if (res.code === 200) {
            const detailRes = await fetchApi(`/questions/${answer.questionId}/detail`);
            if (detailRes.code === 200) {
                renderQuestionDetail(detailRes.data);
            }
        }
    } catch (error) {
        alert('操作失败: ' + error.message);
    }
}

async function getQuestion(questionId) {
    const res = await fetchApi(`/questions/${questionId}`);
    return res.data;
}

async function getAnswer(answerId) {
    const res = await fetchApi(`/answers/${answerId}`);
    return res.data;
}

async function acceptAnswer(answerId) {
    try {
        const res = await fetchApi(`/answers/${answerId}/accept`, 'POST', null, { userId: currentUserId });
        if (res.code === 200) {
            const answer = res.data;
            viewQuestionDetail(answer.questionId);
        } else {
            alert('采纳失败: ' + res.message);
        }
    } catch (error) {
        alert('操作失败: ' + error.message);
    }
}

async function deleteAnswer(answerId) {
    if (!confirm('确定要删除这个回答吗？')) return;
    try {
        const res = await fetchApi(`/answers/${answerId}?userId=${currentUserId}`, 'DELETE');
        if (res.code === 200) {
            const answerRes = await fetchApi(`/answers/${answerId}`);
            if (answerRes.code === 200 && answerRes.data) {
                viewQuestionDetail(answerRes.data.questionId);
            } else {
                location.reload();
            }
        } else {
            alert('删除失败: ' + res.message);
        }
    } catch (error) {
        alert('操作失败: ' + error.message);
    }
}

async function handleAskSubmit(e) {
    e.preventDefault();
    
    const knowledgePointId = document.getElementById('questionKnowledgePoint').value;
    
    const questionData = {
        title: document.getElementById('questionTitle').value,
        content: document.getElementById('questionContent').value,
        knowledgePointId: knowledgePointId || null,
        tags: document.getElementById('questionTags').value.split(',').map(t => t.trim()).filter(t => t)
    };
    
    try {
        const res = await fetchApi('/questions', 'POST', questionData, {
            authorId: currentUserId,
            authorName: currentUserName
        });
        
        if (res.code === 200) {
            closeModal('askModal');
            document.getElementById('askForm').reset();
            alert('提问成功！AI正在分析您的问题，请稍后查看评估结果。');
            loadHomeData();
        } else {
            alert('提问失败: ' + res.message);
        }
    } catch (error) {
        alert('请求失败: ' + error.message);
    }
}

async function handleAnswerSubmit(e) {
    e.preventDefault();
    
    const answerData = {
        questionId: document.getElementById('answerQuestionId').value,
        content: document.getElementById('answerContent').value
    };
    
    try {
        const res = await fetchApi('/answers', 'POST', answerData, {
            authorId: currentUserId,
            authorName: currentUserName
        });
        
        if (res.code === 200) {
            closeModal('answerModal');
            document.getElementById('answerForm').reset();
            alert('回答成功！AI正在评分，请稍后查看。');
            viewQuestionDetail(answerData.questionId);
        } else {
            alert('回答失败: ' + res.message);
        }
    } catch (error) {
        alert('请求失败: ' + error.message);
    }
}

async function handleCommentSubmit(e) {
    e.preventDefault();
    
    const commentData = {
        targetId: document.getElementById('commentTargetId').value,
        targetType: document.getElementById('commentTargetType').value,
        content: document.getElementById('commentContent').value
    };
    
    try {
        const res = await fetchApi('/comments', 'POST', commentData, {
            authorId: currentUserId,
            authorName: currentUserName
        });
        
        if (res.code === 200) {
            closeModal('commentModal');
            document.getElementById('commentForm').reset();
            
            if (commentData.targetType === 'QUESTION') {
                viewQuestionDetail(commentData.targetId);
            } else {
                const answerRes = await fetchApi(`/answers/${commentData.targetId}`);
                if (answerRes.code === 200) {
                    viewQuestionDetail(answerRes.data.questionId);
                }
            }
        } else {
            alert('评论失败: ' + res.message);
        }
    } catch (error) {
        alert('请求失败: ' + error.message);
    }
}

async function handleKnowledgeSubmit(e) {
    e.preventDefault();
    
    const knowledgeData = {
        title: document.getElementById('knowledgeTitle').value,
        content: document.getElementById('knowledgeContent').value,
        difficulty: document.getElementById('knowledgeDifficulty').value
    };
    
    try {
        const res = await fetchApi('/knowledge-points', 'POST', knowledgeData, {
            authorId: currentUserId,
            authorName: currentUserName
        });
        
        if (res.code === 200) {
            closeModal('knowledgeModal');
            document.getElementById('knowledgeForm').reset();
            alert('知识点创建成功！');
            loadKnowledgePoints();
        } else {
            alert('创建失败: ' + res.message);
        }
    } catch (error) {
        alert('请求失败: ' + error.message);
    }
}

async function handleCreateUserSubmit(e) {
    e.preventDefault();
    
    const username = document.getElementById('newUsername').value;
    const nickname = document.getElementById('newNickname').value;
    const password = document.getElementById('newPassword').value;
    const email = document.getElementById('newEmail').value;
    
    if (username && nickname && password) {
        try {
            const res = await fetchApi('/users', 'POST', {
                username: username,
                nickname: nickname,
                password: password,
                email: email
            });
            
            if (res.code === 200 && res.data) {
                currentUserId = res.data.id || username;
                currentUserName = nickname;
                localStorage.setItem('userId', currentUserId);
                localStorage.setItem('userName', currentUserName);
                updateUserDisplay();
                closeModal('userModal');
                alert('用户创建成功！');
                
                document.getElementById('createUserForm').reset();
                loadUsers();
            } else {
                alert('创建失败: ' + (res.message || '未知错误'));
            }
        } catch (error) {
            alert('请求失败: ' + error.message);
        }
    } else {
        alert('请填写必填字段（用户名、昵称、密码）');
    }
}

async function loadUsers() {
    try {
        const res = await fetchApi('/users', 'GET');
        const select = document.getElementById('userSelect');
        
        if (select) {
            select.innerHTML = '<option value="">-- 选择用户 --</option>';
            
            if (res.code === 200 && res.data && Array.isArray(res.data)) {
                res.data.forEach(user => {
                    // 填充全局缓存，供其他地方按 id 查名字
                    if (user.id) usersCache[user.id] = user;
                    const option = document.createElement('option');
                    option.value = user.id || user.username;
                    option.textContent = user.nickname || user.username;
                    
                    if ((currentUserId && option.value === currentUserId) ||
                        (!currentUserId && user.username === localStorage.getItem('userId'))) {
                        option.selected = true;
                    }
                    
                    select.appendChild(option);
                });
            }
        }
    } catch (error) {
        console.error('加载用户列表失败:', error);
    }
}

function switchUser() {
    const select = document.getElementById('userSelect');
    if (select && select.value) {
        const selectedOption = select.options[select.selectedIndex];
        currentUserId = select.value;
        currentUserName = selectedOption.textContent;
        localStorage.setItem('userId', currentUserId);
        localStorage.setItem('userName', currentUserName);
        updateUserDisplay();
        
        console.log('切换用户 - 新用户ID:', currentUserId, '用户名:', currentUserName);
        
        // 重新加载联系人列表
        if (typeof loadContacts === 'function') {
            loadContacts();
        }
        
        // 重新加载未读计数
        if (typeof fetchUnreadCounts === 'function') {
            fetchUnreadCounts();
        }
        
        // 清空当前聊天
        currentChatUserId = null;
        currentChatUserName = null;
        const chatEmptyState = document.getElementById('chatEmptyState');
        const chatActive = document.getElementById('chatActive');
        if (chatEmptyState) chatEmptyState.style.display = 'flex';
        if (chatActive) chatActive.style.display = 'none';
    }
}

function openAnswerModal(questionId) {
    document.getElementById('answerQuestionId').value = questionId;
    openModal('answerModal');
}

function openCommentModal(targetId, targetType) {
    document.getElementById('commentTargetId').value = targetId;
    document.getElementById('commentTargetType').value = targetType;
    openModal('commentModal');
}

async function loadKnowledgePointsForSelect() {
    try {
        const res = await fetchApi('/knowledge-points');
        if (res.code === 200) {
            const select = document.getElementById('questionKnowledgePoint');
            select.innerHTML = '<option value="">-- 由AI自动提取 --</option>';
            res.data.forEach(kp => {
                const option = document.createElement('option');
                option.value = kp.id;
                option.textContent = kp.title;
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Failed to load knowledge points:', error);
    }
}

async function loadQuestionsByKnowledgePoint(knowledgePointId) {
    try {
        const res = await fetchApi(`/questions/knowledge-point/${knowledgePointId}`);
        if (res.code === 200) {
            const targetPage = document.getElementById('questions-page');
            if (targetPage) {
                document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
                targetPage.classList.add('active');
            }
            renderQuestions(res.data, 'allQuestions');
        }
    } catch (error) {
        console.error('Failed to load questions:', error);
    }
}

function openModal(modalId) {
    document.getElementById(modalId).style.display = 'block';
    
    if (modalId === 'askModal') {
        loadKnowledgePointsForSelect();
    }
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

function showUserModal() {
    openModal('userModal');
}

async function fetchApi(url, method = 'GET', data = null, queryParams = null) {
    let fullUrl = API_BASE_URL + url;
    
    if (queryParams) {
        const params = new URLSearchParams(queryParams);
        fullUrl += '?' + params.toString();
    }
    
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        }
    };
    
    if (data) {
        options.body = JSON.stringify(data);
    }
    
    try {
        const response = await fetch(fullUrl, options);
        return await response.json();
    } catch (error) {
        console.error('API request failed:', error);
        throw error;
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showAskQuestion() {
    const modal = document.getElementById('askModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

function showAddKnowledge() {
    const modal = document.getElementById('knowledgeModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
}

window.closeModal = closeModal;
window.showUserModal = showUserModal;
window.viewQuestionDetail = viewQuestionDetail;
window.toggleLikeQuestion = toggleLikeQuestion;
window.deleteQuestion = deleteQuestion;
window.toggleLikeAnswer = toggleLikeAnswer;
window.acceptAnswer = acceptAnswer;
window.deleteAnswer = deleteAnswer;
window.openAnswerModal = openAnswerModal;
window.openCommentModal = openCommentModal;
window.showAskQuestion = showAskQuestion;
window.showAddKnowledge = showAddKnowledge;
window.loadQuestionsByKnowledgePoint = loadQuestionsByKnowledgePoint;
window.loadQuestionEvaluation = loadQuestionEvaluation;
window.loadAnswerEvaluation = loadAnswerEvaluation;
window.reevaluateAnswer = reevaluateAnswer;
window.searchFromHero = searchFromHero;
window.filterNotifications = filterNotifications;
window.markConversationAsRead = markConversationAsRead;
window.markNotificationAsRead = markNotificationAsRead;
window.markAllNotificationsRead = markAllNotificationsRead;
window.sendMessage = sendMessage;
window.handleSendMessage = handleSendMessage;
window.searchContacts = searchContacts;
window.showSendMessageModal = showSendMessageModal;
window.switchUser = switchUser;
window.searchQuestions = searchQuestions;

// ==================== Redis 功能相关函数 ====================

async function loadRedisData() {
    try {
        await Promise.all([
            refreshOnlineUserCount(),
            refreshPopularRanking(),
            refreshActiveUsers(),
            refreshHotViewedList(),
            refreshMyActivity()
        ]);
    } catch (error) {
        console.error('Failed to load Redis data:', error);
    }
}

// 刷新在线用户数
async function refreshOnlineUserCount() {
    try {
        const res = await fetchApi('/redis/users/online/count');
        if (res.code === 200) {
            document.getElementById('onlineUserCount').textContent = res.data;
        }
    } catch (error) {
        console.error('Failed to load online user count:', error);
        document.getElementById('onlineUserCount').textContent = 'Error';
    }
}

// 刷新最受欢迎问题排行榜
async function refreshPopularRanking() {
    const container = document.getElementById('popularRankingList');
    container.innerHTML = '<p class="loading-text">加载中...</p>';

    try {
        const res = await fetchApi('/redis/popular-ranking?limit=10');
        if (res.code === 200 && res.data && res.data.length > 0) {
            document.getElementById('rankingCount').textContent = res.data.length;

            container.innerHTML = res.data.map((item, index) => {
                const rankClass = index === 0 ? 'rank-1' : index === 1 ? 'rank-2' : index === 2 ? 'rank-3' : 'rank-other';
                const medal = index < 3
                    ? `<span class="medal medal-${index + 1}"></span>`
                    : `${index + 1}`;
                return `
                    <div class="ranking-item-enhanced" onclick="viewQuestionDetail('${item.questionId}')">
                        <div class="rank-badge ${rankClass}">${medal}</div>
                        <div class="question-detail">
                            <h4 class="question-title">${item.title || '未知问题'}</h4>
                            <p class="question-excerpt">${item.content || '暂无内容'}</p>
                            <div class="question-meta">
                                <span class="meta-item">${item.authorName || '匿名用户'}</span>
                                <span class="meta-item">${item.viewCount || 0} 次浏览</span>
                                <span class="meta-item">${item.likeCount || 0} 点赞</span>
                                <span class="meta-item">${item.answerCount || 0} 回答</span>
                            </div>
                        </div>
                        <div class="score-display">
                            <span class="score-number">${item.score.toFixed(1)}</span>
                            <span class="score-label">综合分</span>
                        </div>
                    </div>
                `;
            }).join('');
        } else {
            container.innerHTML = '<p class="empty-state">暂无排行数据<br><small>请先创建问题并互动</small></p>';
            document.getElementById('rankingCount').textContent = '0';
        }
    } catch (error) {
        console.error('Failed to load popular ranking:', error);
        container.innerHTML = '<p class="error-state">加载失败</p>';
    }
}

// 刷新活跃用户列表
async function refreshActiveUsers() {
    const container = document.getElementById('activeUsersList');
    container.innerHTML = '<p class="loading-text">加载中...</p>';

    try {
        const res = await fetchApi('/redis/users/active/top?limit=10');
        if (res.code === 200 && res.data && res.data.length > 0) {
            container.innerHTML = res.data.map(user => {
                const cached = usersCache[user.userId];
                const displayName = cached
                    ? (cached.nickname || cached.username || user.userId)
                    : user.userId;
                const avatarChar = displayName.charAt(0).toUpperCase();
                return `
                <div class="user-item">
                    <div class="user-info-display">
                        <div class="user-avatar">${avatarChar}</div>
                        <span style="font-weight: 600;">${escapeHtml(displayName)}</span>
                    </div>
                    <div class="activity-score-badge">${user.activityScore} 次活跃</div>
                </div>`;
            }).join('');
        } else {
            container.innerHTML = '<p class="loading-text">暂无活跃用户数据</p>';
        }
    } catch (error) {
        console.error('Failed to load active users:', error);
        container.innerHTML = '<p class="loading-text">加载失败</p>';
    }
}

// 刷新热门缓存问题列表
async function refreshHotViewedList() {
    const container = document.getElementById('hotViewedQuestionsList');
    container.innerHTML = '<p class="loading-text">加载中...</p>';

    try {
        const questionIdsRes = await fetchApi('/redis/top-viewed-ids?limit=10');
        if (questionIdsRes.code === 200 && questionIdsRes.data && questionIdsRes.data.length > 0) {
            document.getElementById('cachedQuestionsCount').textContent = questionIdsRes.data.length;

            // 批量获取问题详情
            const questions = await Promise.all(
                questionIdsRes.data.slice(0, 5).map(async id => {
                    try {
                        const res = await fetchApi(`/questions/${id}`);
                        return res.code === 200 ? res.data : null;
                    } catch (e) {
                        return null;
                    }
                })
            );

            const validQuestions = questions.filter(q => q !== null);

            container.innerHTML = validQuestions.map(q => `
                <div class="question-card" onclick="viewQuestionDetail('${q.id}')">
                    <h3>${escapeHtml(q.title)}</h3>
                    <div class="question-meta">
                        <span>👤 ${q.authorName}</span>
                        <span>👁 ${q.viewCount} 次浏览</span>
                        <span>❤️ ${q.likeCount || 0} 赞</span>
                        <span class="redis-cache-tag">Redis 缓存</span>
                    </div>
                </div>
            `).join('');
        } else {
            container.innerHTML = '<p class="loading-text">暂无缓存数据（浏览量>10的问题会被缓存）</p>';
            document.getElementById('cachedQuestionsCount').textContent = '0';
        }
    } catch (error) {
        console.error('Failed to load hot viewed list:', error);
        container.innerHTML = '<p class="loading-text">加载失败</p>';
    }
}

// 刷新我的最近活动
async function refreshMyActivity() {
    const container = document.getElementById('myActivityList');
    container.innerHTML = '<p class="loading-text">加载中...</p>';

    try {
        const res = await fetchApi(`/redis/users/${currentUserId}/activity/recent?limit=10`);
        if (res.code === 200 && res.data && res.data.length > 0) {
            const typeNames = {
                'LIKE': '点赞',
                'VIEW': '浏览',
                'ANSWER': '回答',
                'QUESTION': '提问',
                'COMMENT': '评论'
            };

            const typeClasses = {
                'LIKE': 'act-type-like',
                'VIEW': 'act-type-view',
                'ANSWER': 'act-type-answer',
                'QUESTION': 'act-type-question',
                'COMMENT': 'act-type-comment'
            };

            container.innerHTML = res.data.reverse().map(activity => {
                const timeAgo = formatTimeAgo(activity.timestamp);
                const cls = typeClasses[activity.type] || 'act-type-default';
                return `
                    <div class="activity-item">
                        <div class="activity-type-icon"><span class="act-dot ${cls}"></span></div>
                        <div class="activity-details">
                            <div class="activity-type-text">${typeNames[activity.type] || activity.type}</div>
                            <div class="activity-time">${timeAgo}</div>
                        </div>
                    </div>
                `;
            }).join('');
        } else {
            container.innerHTML = '<p class="loading-text">暂无活动记录（操作后会自动记录）</p>';
        }
    } catch (error) {
        console.error('Failed to load my activity:', error);
        container.innerHTML = '<p class="loading-text">加载失败</p>';
    }
}

// 标记当前用户在线
async function markMeOnline() {
    try {
        await fetchApi(`/redis/users/${currentUserId}/online`, 'POST');
        alert('已标记为在线状态！');
        refreshOnlineUserCount();
    } catch (error) {
        alert('标记在线失败: ' + error.message);
    }
}

// 记录我的活动
async function recordMyActivity(activityType) {
    try {
        await fetchApi(`/redis/users/${currentUserId}/activity?activityType=${activityType}`, 'POST');
        alert(`已记录 ${activityType} 活动！`);
        refreshMyActivity();
        refreshActiveUsers();
    } catch (error) {
        alert('记录活动失败: ' + error.message);
    }
}

// 同步热门缓存
async function syncHotViewedCache() {
    try {
        await fetchApi('/redis/sync/hot-viewed', 'POST');
        alert('缓存同步成功！');
        refreshHotViewedList();
    } catch (error) {
        alert('同步失败: ' + error.message);
    }
}

// 全量同步 Redis 数据
async function syncAllRedisData() {
    try {
        await Promise.all([
            fetchApi('/redis/sync/popular', 'POST'),
            fetchApi('/redis/sync/hot-viewed', 'POST')
        ]);
        alert('全量同步成功！');
        loadRedisData();
    } catch (error) {
        alert('同步失败: ' + error.message);
    }
}

// 格式化时间戳为相对时间
function formatTimeAgo(timestamp) {
    if (!timestamp) return '';
    const now = Date.now();
    const diff = now - timestamp;

    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (seconds < 60) return `${seconds} 秒前`;
    if (minutes < 60) return `${minutes} 分钟前`;
    if (hours < 24) return `${hours} 小时前`;
    return `${days} 天前`;
}

// 暴露到全局作用域
window.refreshPopularRanking = refreshPopularRanking;
window.refreshActiveUsers = refreshActiveUsers;
window.refreshHotViewedList = refreshHotViewedList;
window.refreshMyActivity = refreshMyActivity;
window.refreshOnlineUserCount = refreshOnlineUserCount;
window.markMeOnline = markMeOnline;
window.recordMyActivity = recordMyActivity;
window.syncHotViewedCache = syncHotViewedCache;
window.syncAllRedisData = syncAllRedisData;

// ========== 通知系统相关变量和函数 ==========
// notificationPage / notificationPageSize 已提前到文件顶部

// 当前聊天状态（声明已提前到文件顶部）

// 定时器ID
let unreadPollingTimer = null;

// 初始化时启动未读数轮询
function startUnreadCountPolling() {
    // 立即执行一次
    fetchUnreadCounts();
    
    // 每30秒轮询一次
    if (unreadPollingTimer) {
        clearInterval(unreadPollingTimer);
    }
    unreadPollingTimer = setInterval(fetchUnreadCounts, 30000);
}

// 获取未读通知数和未读私信数
async function fetchUnreadCounts() {
    try {
        const [notifRes, msgRes] = await Promise.all([
            fetchApi(`/notifications/unread/${currentUserId}`),
            fetchApi(`/messages/unread/${currentUserId}`)
        ]);
        
        // 更新通知角标
        if (notifRes.code === 200) {
            updateNotificationBadge(notifRes.data || 0);
        }
        
        // 更新私信角标
        if (msgRes.code === 200) {
            updateMessageBadge(msgRes.data || 0);
        }
    } catch (error) {
        console.error('Failed to fetch unread counts:', error);
    }
}

// 更新通知角标
function updateNotificationBadge(count) {
    const badge = document.getElementById('notificationBadge');
    const totalUnreadDisplay = document.getElementById('totalUnreadCount');

    if (count > 0) {
        badge.textContent = count > 99 ? '99+' : count;
        badge.style.display = 'flex';
    } else {
        badge.style.display = 'none';
    }
    
    if (totalUnreadDisplay) {
        totalUnreadDisplay.textContent = `未读: ${count} 条`;
    }
}

// 更新私信角标
function updateMessageBadge(count) {
    const badge = document.getElementById('messageBadge');

    if (count > 0) {
        badge.textContent = count > 99 ? '99+' : count;
        badge.style.display = 'flex';
    } else {
        badge.style.display = 'none';
    }
}

// 加载通知列表
async function loadNotifications() {
    try {
        const container = document.getElementById('notificationList');
        container.innerHTML = '<p class="loading-text">加载中...</p>';
        
        const res = await fetchApi(`/notifications/user/${currentUserId}?page=${notificationPage}&size=${notificationPageSize}`);
        
        if (res.code === 200) {
            renderNotificationList(res.data);
            
            // 加载统计信息
            loadNotificationStats();
        } else {
            container.innerHTML = `<p class="error-state">加载失败: ${res.message}</p>`;
        }
    } catch (error) {
        console.error('Failed to load notifications:', error);
        document.getElementById('notificationList').innerHTML = 
            `<p class="error-state">加载失败: ${error.message}</p>`;
    }
}

// 渲染通知列表
function renderNotificationList(notifications) {
    const container = document.getElementById('notificationList');
    
    if (!notifications || notifications.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <h3>暂无通知</h3>
                <small>当有人回答你的问题或点赞时，你会收到通知</small>
            </div>
        `;
        return;
    }
    
    container.innerHTML = notifications.map(n => `
        <div class="notification-item ${n.read ? '' : 'unread'}" onclick="markNotificationAsRead('${n.id}')">
            <div class="notification-icon">
                ${getNotificationIcon(n.type)}
            </div>
            <div class="notification-content">
                <h4>${escapeHtml(n.title)}</h4>
                <p>${escapeHtml(n.content)}</p>
                <span class="notification-time">${formatDate(n.createTime)}</span>
            </div>
            <div class="notification-status">
                ${n.read ? '<span class="read-badge">已读</span>' : '<span class="unread-badge">未读</span>'}
            </div>
        </div>
    `).join('');
}

// 获取通知图标 CSS 类
function getNotificationIcon(type) {
    switch(type) {
        case 'ANSWER':    return '<span class="notif-dot dot-answer"></span>';
        case 'LIKE':      return '<span class="notif-dot dot-like"></span>';
        case 'COMMENT':   return '<span class="notif-dot dot-comment"></span>';
        case 'SYSTEM':    return '<span class="notif-dot dot-system"></span>';
        default:          return '<span class="notif-dot dot-default"></span>';
    }
}

// 标记单条通知为已读
async function markNotificationAsRead(notificationId) {
    try {
        const res = await fetchApi(`/notifications/${notificationId}/read?userId=${currentUserId}`, 'PUT');
        
        if (res.code === 200) {
            // 重新加载通知列表
            loadNotifications();
            // 更新未读数
            fetchUnreadCounts();
        }
    } catch (error) {
        console.error('Failed to mark notification as read:', error);
    }
}

// 标记所有通知为已读
async function markAllNotificationsRead() {
    try {
        const res = await fetchApi(`/notifications/user/${currentUserId}/read-all`, 'PUT');
        
        if (res.code === 200) {
            alert('已将所有通知标记为已读');
            // 重新加载通知列表
            loadNotifications();
            // 更新未读数
            fetchUnreadCounts();
        } else {
            alert('操作失败: ' + res.message);
        }
    } catch (error) {
        alert('操作失败: ' + error.message);
    }
}

// 加载通知统计
async function loadNotificationStats() {
    try {
        const res = await fetchApi(`/notifications/stats/${currentUserId}`);
        
        if (res.code === 200 && res.data) {
            document.getElementById('statTotal').textContent = res.data.total || 0;
            document.getElementById('statUnread').textContent = res.data.unread || 0;
            document.getElementById('statRead').textContent = res.data.read || 0;
        }
    } catch (error) {
        console.error('Failed to load notification stats:', error);
    }
}

// ========== 私信系统相关函数 ==========

// 加载联系人列表（最近消息的用户）
async function loadContacts() {
    try {
        const container = document.getElementById('contactList');
        container.innerHTML = '<p class="loading-text">加载中...</p>';
        
        const res = await fetchApi(`/messages/user/${currentUserId}?page=0&size=50`);
        
        if (res.code === 200) {
            renderContactList(res.data);
        } else {
            container.innerHTML = `<p class="error-state">加载失败</p>`;
        }
    } catch (error) {
        console.error('Failed to load contacts:', error);
        document.getElementById('contactList').innerHTML = 
            `<p class="error-state">加载失败: ${error.message}</p>`;
    }
}

// 渲染联系人列表
function renderContactList(messages) {
    const container = document.getElementById('contactList');
    
    if (!messages || messages.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="padding: 2rem;">
                <h3>暂无私信</h3>
                <small>点击"发消息"开始对话</small>
            </div>
        `;
        return;
    }
    
    // 提取唯一联系人并按最后消息时间排序
    const contactsMap = new Map();
    messages.forEach(msg => {
        const otherUserId = msg.senderId === currentUserId ? msg.receiverId : msg.senderId;
        const otherUserName = msg.senderId === currentUserId ? msg.receiverName : msg.senderName;
        
        if (!contactsMap.has(otherUserId)) {
            contactsMap.set(otherUserId, {
                id: otherUserId,
                name: otherUserName,
                lastMessage: msg.content,
                lastTime: msg.createTime,
                unread: msg.status === 'UNREAD' && msg.receiverId === currentUserId ? 1 : 0
            });
        } else {
            const contact = contactsMap.get(otherUserId);
            if (new Date(msg.createTime) > new Date(contact.lastTime)) {
                contact.lastMessage = msg.content;
                contact.lastTime = msg.createTime;
            }
            if (msg.status === 'UNREAD' && msg.receiverId === currentUserId) {
                contact.unread++;
            }
        }
    });
    
    // 转换为数组并排序
    const contacts = Array.from(contactsMap.values())
        .sort((a, b) => new Date(b.lastTime) - new Date(a.lastTime));
    
    container.innerHTML = contacts.map(c => `
        <div class="contact-item ${currentChatUserId === c.id ? 'active' : ''}" 
             onclick="openChat('${c.id}', '${escapeHtml(c.name)}')">
            <div class="contact-avatar">${(c.name || '?')[0]}</div>
            <div class="contact-info">
                <div class="contact-name">
                    ${escapeHtml(c.name)}
                    ${c.unread > 0 ? `<span class="contact-unread-badge">${c.unread}</span>` : ''}
                </div>
                <div class="contact-last-message">${escapeHtml(c.lastMessage)}</div>
                <div class="contact-time">${formatRelativeTime(c.lastTime)}</div>
            </div>
        </div>
    `).join('');
}

// 打开聊天窗口
async function openChat(userId, userName) {
    currentChatUserId = userId;
    currentChatUserName = userName;
    
    // 更新UI
    document.getElementById('chatEmptyState').style.display = 'none';
    document.getElementById('chatActive').style.display = 'flex';
    document.getElementById('chatContactName').textContent = userName;
    
    // 高亮当前联系人
    document.querySelectorAll('.contact-item').forEach(item => {
        item.classList.remove('active');
    });
    event.currentTarget?.classList.add('active');
    
    // 加载消息记录
    await loadMessages();

    // 标记会话已读
    await markConversationAsRead();
}

// 加载消息记录
async function loadMessages() {
    if (!currentChatUserId) return;
    
    try {
        const container = document.getElementById('messagesContainer');
        container.innerHTML = '<p class="loading-text">加载中...</p>';
        
        const res = await fetchApi(`/messages/conversation?user1Id=${currentUserId}&user2Id=${currentChatUserId}`);
        
        if (res.code === 200) {
            renderMessages(res.data);
        } else {
            container.innerHTML = `<p class="error-state">加载失败</p>`;
        }
    } catch (error) {
        console.error('Failed to load messages:', error);
        document.getElementById('messagesContainer').innerHTML = 
            `<p class="error-state">加载失败: ${error.message}</p>`;
    }
}

// 渲染消息列表
function renderMessages(messages) {
    const container = document.getElementById('messagesContainer');
    
    if (!messages || messages.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="padding: 3rem;">
                <h3>暂无消息</h3>
                <small>发送第一条消息开始对话吧！</small>
            </div>
        `;
        return;
    }
    
    console.log('渲染消息 - 当前用户ID:', currentUserId, '聊天对象ID:', currentChatUserId);
    
    // API 返回最新在前，反转为最旧在前显示
    const sorted = [...messages].reverse();

    container.innerHTML = sorted.map(msg => {
        const isSelf = msg.senderId === currentUserId;
        return `
            <div class="message-item ${isSelf ? 'self' : 'other'}">
                <div class="message-avatar">${isSelf ? '我' : (msg.senderName || '?')[0]}</div>
                <div class="message-bubble">
                    <div class="message-sender">${isSelf ? '我' : escapeHtml(msg.senderName)}</div>
                    <div class="message-text">${escapeHtml(msg.content)}</div>
                    <div class="message-time">${formatTime(msg.createTime)}</div>
                </div>
            </div>
        `;
    }).join('');

    // 滚动到底部
    requestAnimationFrame(() => {
        container.scrollTop = container.scrollHeight;
    });
}

// 发送消息
async function sendMessage() {
    const input = document.getElementById('messageInput');
    const content = input.value.trim();
    
    if (!content) {
        alert('请输入消息内容');
        return;
    }
    
    if (!currentChatUserId) {
        alert('请先选择联系人');
        return;
    }
    
    try {
        const messageData = {
            senderId: currentUserId,
            senderName: currentUserName,
            receiverId: currentChatUserId,
            receiverName: currentChatUserName,
            content: content
        };
        
        const res = await fetchApi('/messages', 'POST', messageData);
        
        if (res.code === 200) {
            input.value = '';
            // 重新加载消息
            await loadMessages();
            // 更新联系人列表
            await loadContacts();
            // 更新未读数
            fetchUnreadCounts();
        } else {
            alert('发送失败: ' + res.message);
        }
    } catch (error) {
        alert('发送失败: ' + error.message);
    }
}

// 标记会话已读
async function markConversationAsRead() {
    if (!currentChatUserId) return;
    
    try {
        await fetchApi('/messages/conversation/read-all', 'PUT', null, {
            senderId: currentChatUserId,
            receiverId: currentUserId
        });
        
        // 更新未读数
        fetchUnreadCounts();
        // 更新联系人列表
        loadContacts();
    } catch (error) {
        console.error('Failed to mark conversation as read:', error);
    }
}

// 显示发送消息弹窗
async function showSendMessageModal() {
    // 加载用户列表到选择框
    try {
        const res = await fetchApi('/users');
        const select = document.getElementById('messageReceiverSelect');
        
        if (res.code === 200 && res.data) {
            select.innerHTML = '<option value="">-- 选择联系人 --</option>' +
                res.data
                    .filter(u => u.id !== currentUserId)
                    .map(u => `<option value="${u.id}">${escapeHtml(u.nickname || u.username)}</option>`)
                    .join('');
        }
    } catch (error) {
        console.error('Failed to load users:', error);
    }
    
    openModal('sendMessageModal');
}

// 处理发送消息表单提交
async function handleSendMessage(event) {
    event.preventDefault();
    
    const receiverId = document.getElementById('messageReceiverSelect').value;
    const content = document.getElementById('messageContentInput').value.trim();
    
    if (!receiverId || !content) {
        alert('请填写完整信息');
        return;
    }
    
    try {
        // 获取接收人名称
        const select = document.getElementById('messageReceiverSelect');
        const receiverName = select.options[select.selectedIndex].text;
        
        const messageData = {
            senderId: currentUserId,
            senderName: currentUserName,
            receiverId: receiverId,
            receiverName: receiverName,
            content: content
        };
        
        const res = await fetchApi('/messages', 'POST', messageData);
        
        if (res.code === 200) {
            closeModal('sendMessageModal');
            document.getElementById('messageContentInput').value = '';
            alert('发送成功！');
            
            // 如果当前正在与该用户聊天，刷新消息
            if (currentChatUserId === receiverId) {
                await loadMessages();
            }
            
            // 刷新联系人列表
            await loadContacts();
        } else {
            alert('发送失败: ' + res.message);
        }
    } catch (error) {
        alert('发送失败: ' + error.message);
    }
}

// 搜索联系人
function searchContacts() {
    const keyword = document.getElementById('contactSearchInput').value.toLowerCase();
    const items = document.querySelectorAll('.contact-item');
    
    items.forEach(item => {
        const name = item.querySelector('.contact-name')?.textContent.toLowerCase() || '';
        if (name.includes(keyword)) {
            item.style.display = 'flex';
        } else {
            item.style.display = 'none';
        }
    });
}

// 格式化相对时间
function formatRelativeTime(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;
    
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);
    
    if (minutes < 1) return '刚刚';
    if (minutes < 60) return `${minutes}分钟前`;
    if (hours < 24) return `${hours}小时前`;
    if (days < 7) return `${days}天前`;
    
    return formatDate(dateString);
}

// 格式化时间（HH:mm）
function formatTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
}

// 扩展初始化以启动未读数轮询
const originalInitApp = initApp;
initApp = function() {
    originalInitApp();
    startUnreadCountPolling();
};
