package com.jellystudy.service;

import com.jellystudy.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * 称号规则表（写死）。按用户的 reputation / questionCount / answerCount 自动判定解锁。
 */
public final class TitleCatalog {

    private TitleCatalog() {
    }

    public static class TitleDef {
        public final String code;
        public final String name;
        public final String requirement;
        public final Predicate<User> condition;

        public TitleDef(String code, String name, String requirement, Predicate<User> condition) {
            this.code = code;
            this.name = name;
            this.requirement = requirement;
            this.condition = condition;
        }
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }

    // 顺序即展示顺序
    public static final List<TitleDef> TITLES = Arrays.asList(
            new TitleDef("newbie", "新人", "默认拥有", u -> true),
            new TitleDef("asker", "提问新秀", "提问数 ≥ 5", u -> nz(u.getQuestionCount()) >= 5),
            new TitleDef("asker_pro", "提问达人", "提问数 ≥ 20", u -> nz(u.getQuestionCount()) >= 20),
            new TitleDef("answerer", "解答之星", "回答数 ≥ 10", u -> nz(u.getAnswerCount()) >= 10),
            new TitleDef("scholar", "学者", "贡献点 ≥ 100", u -> nz(u.getReputation()) >= 100),
            new TitleDef("master", "大师", "贡献点 ≥ 500", u -> nz(u.getReputation()) >= 500)
    );

    /** 计算该用户当前已解锁的称号 code 列表 */
    public static List<String> computeOwned(User user) {
        List<String> owned = new ArrayList<>();
        for (TitleDef def : TITLES) {
            if (def.condition.test(user)) {
                owned.add(def.code);
            }
        }
        return owned;
    }

    public static TitleDef findByCode(String code) {
        return TITLES.stream().filter(t -> t.code.equals(code)).findFirst().orElse(null);
    }
}
