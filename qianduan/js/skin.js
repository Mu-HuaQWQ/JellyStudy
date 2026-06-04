// 皮肤管理器 - 动态 CSS 切换
const SkinManager = {
    // 可用皮肤列表
    SKINS: {
        ink: { name: '墨韵纸香', id: 'ink' },
        dark: { name: '暗黑模式', id: 'dark' }
    },

    // 当前皮肤 CSS link 元素
    skinLink: null,

    // 初始化
    init() {
        // 获取保存的皮肤，默认墨韵纸香
        const savedSkin = localStorage.getItem('currentSkin') || 'ink';
        this.loadSkin(savedSkin);
        this.setupToggleButton();
    },

    // 加载皮肤
    loadSkin(skinId) {
        if (!this.SKINS[skinId]) skinId = 'ink';

        // 查找或创建 skin link 元素
        let link = document.getElementById('skin-stylesheet');
        if (!link) {
            link = document.createElement('link');
            link.id = 'skin-stylesheet';
            link.rel = 'stylesheet';
            document.head.appendChild(link);
        }

        // 设置皮肤 CSS 路径
        link.href = `css/skins/${skinId}.css`;

        // 保存到 localStorage
        localStorage.setItem('currentSkin', skinId);

        // 更新按钮图标
        this.updateToggleButton(skinId);
    },

    // 切换到下一个皮肤
    toggleSkin() {
        const current = localStorage.getItem('currentSkin') || 'ink';
        const skinIds = Object.keys(this.SKINS);
        const currentIndex = skinIds.indexOf(current);
        const nextIndex = (currentIndex + 1) % skinIds.length;
        const nextSkin = skinIds[nextIndex];

        this.loadSkin(nextSkin);
    },

    // 设置皮肤切换按钮
    setupToggleButton() {
        const btn = document.querySelector('.skin-toggle-btn');
        if (btn) {
            // 移除旧的 onclick
            btn.removeAttribute('onclick');
            // 添加新的事件监听
            btn.addEventListener('click', () => this.toggleSkin());
        }
    },

    // 更新切换按钮图标
    updateToggleButton(skinId) {
        const btn = document.querySelector('.skin-toggle-btn');
        if (btn) {
            const icons = {
                ink: '🌙',
                dark: '☀️'
            };
            const titles = {
                ink: '切换到暗黑模式',
                dark: '切换到墨韵纸香'
            };
            btn.textContent = icons[skinId] || '🎨';
            btn.title = titles[skinId] || '切换皮肤';
        }
    },

    // 获取当前皮肤ID
    getCurrentSkin() {
        return localStorage.getItem('currentSkin') || 'ink';
    }
};

// 页面加载时初始化皮肤
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => SkinManager.init());
} else {
    SkinManager.init();
}

// 暴露到全局
window.SkinManager = SkinManager;