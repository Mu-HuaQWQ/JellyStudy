package com.jellystudy.service;

import com.jellystudy.entity.BrowseHistory;
import com.jellystudy.entity.Favorite;
import com.jellystudy.entity.Follow;
import com.jellystudy.entity.Question;
import com.jellystudy.entity.User;
import com.jellystudy.entity.UserDecoration;
import com.jellystudy.repository.BrowseHistoryRepository;
import com.jellystudy.repository.FavoriteRepository;
import com.jellystudy.repository.FollowRepository;
import com.jellystudy.repository.QuestionRepository;
import com.jellystudy.repository.UserDecorationRepository;
import com.jellystudy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 用户中心服务：关注、收藏、最近浏览、称号。
 */
@Service
public class UserCenterService {

    // 最近浏览最多保留条数
    private static final int MAX_HISTORY = 20;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private BrowseHistoryRepository browseHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserDecorationRepository decorationRepository;

    // ============ 关注 ============

    public void follow(String userId, String targetId) {
        if (userId == null || targetId == null || userId.equals(targetId)) {
            throw new IllegalArgumentException("不能关注自己或参数为空");
        }
        // 幂等：已关注直接返回
        if (followRepository.existsByFollowerIdAndFollowingId(userId, targetId)) {
            return;
        }
        Follow follow = Follow.builder()
                .followerId(userId)
                .followingId(targetId)
                .createTime(LocalDateTime.now())
                .build();
        followRepository.save(follow);

        incrementFollowingCount(userId, 1);
        incrementFollowerCount(targetId, 1);
    }

    public void unfollow(String userId, String targetId) {
        Optional<Follow> existing = followRepository.findByFollowerIdAndFollowingId(userId, targetId);
        if (existing.isEmpty()) {
            return;
        }
        followRepository.delete(existing.get());
        incrementFollowingCount(userId, -1);
        incrementFollowerCount(targetId, -1);
    }

    public boolean isFollowing(String userId, String targetId) {
        return followRepository.existsByFollowerIdAndFollowingId(userId, targetId);
    }

    /** 我关注的人（返回用户信息，密码置空） */
    public List<User> getFollowing(String userId) {
        List<User> result = new ArrayList<>();
        for (Follow f : followRepository.findByFollowerIdOrderByCreateTimeDesc(userId)) {
            userRepository.findById(f.getFollowingId()).ifPresent(u -> {
                u.setPassword(null);
                result.add(u);
            });
        }
        return result;
    }

    /** 关注我的人（粉丝） */
    public List<User> getFollowers(String userId) {
        List<User> result = new ArrayList<>();
        for (Follow f : followRepository.findByFollowingIdOrderByCreateTimeDesc(userId)) {
            userRepository.findById(f.getFollowerId()).ifPresent(u -> {
                u.setPassword(null);
                result.add(u);
            });
        }
        return result;
    }

    private void incrementFollowingCount(String userId, int delta) {
        userRepository.findById(userId).ifPresent(u -> {
            int v = (u.getFollowingCount() == null ? 0 : u.getFollowingCount()) + delta;
            u.setFollowingCount(Math.max(0, v));
            userRepository.save(u);
        });
    }

    private void incrementFollowerCount(String userId, int delta) {
        userRepository.findById(userId).ifPresent(u -> {
            int v = (u.getFollowerCount() == null ? 0 : u.getFollowerCount()) + delta;
            u.setFollowerCount(Math.max(0, v));
            userRepository.save(u);
        });
    }

    // ============ 收藏 ============

    public void addFavorite(String userId, String questionId) {
        if (favoriteRepository.existsByUserIdAndQuestionId(userId, questionId)) {
            return;
        }
        String title = questionRepository.findById(questionId)
                .map(Question::getTitle)
                .orElse("(已删除的问题)");
        Favorite favorite = Favorite.builder()
                .userId(userId)
                .questionId(questionId)
                .questionTitle(title)
                .createTime(LocalDateTime.now())
                .build();
        favoriteRepository.save(favorite);
    }

    public void removeFavorite(String userId, String questionId) {
        favoriteRepository.findByUserIdAndQuestionId(userId, questionId)
                .ifPresent(favoriteRepository::delete);
    }

    public boolean isFavorited(String userId, String questionId) {
        return favoriteRepository.existsByUserIdAndQuestionId(userId, questionId);
    }

    public List<Favorite> getFavorites(String userId) {
        return favoriteRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    // ============ 最近浏览 ============

    public void recordHistory(String userId, String questionId) {
        Optional<BrowseHistory> existing = browseHistoryRepository.findByUserIdAndQuestionId(userId, questionId);
        if (existing.isPresent()) {
            // 同问题去重：更新浏览时间置顶
            BrowseHistory h = existing.get();
            h.setViewTime(LocalDateTime.now());
            browseHistoryRepository.save(h);
        } else {
            String title = questionRepository.findById(questionId)
                    .map(Question::getTitle)
                    .orElse("(已删除的问题)");
            BrowseHistory h = BrowseHistory.builder()
                    .userId(userId)
                    .questionId(questionId)
                    .questionTitle(title)
                    .viewTime(LocalDateTime.now())
                    .build();
            browseHistoryRepository.save(h);
        }
        // 超出上限则删除最旧的
        List<BrowseHistory> all = browseHistoryRepository.findByUserIdOrderByViewTimeDesc(userId);
        if (all.size() > MAX_HISTORY) {
            List<BrowseHistory> toDelete = all.subList(MAX_HISTORY, all.size());
            browseHistoryRepository.deleteAll(toDelete);
        }
    }

    public List<BrowseHistory> getHistory(String userId) {
        return browseHistoryRepository.findByUserIdOrderByViewTimeDesc(userId);
    }

    // ============ 称号 ============

    /**
     * 返回全部称号定义（内置 + 抽卡获得），含是否解锁、是否佩戴。
     */
    public List<Map<String, Object>> getTitles(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }
        List<String> builtinOwned = TitleCatalog.computeOwned(user);
        List<UserDecoration> gachaTitles = decorationRepository.findByUserIdAndItemType(userId, "TITLE");

        // 合并所有已拥有的称号 code
        Set<String> allOwned = new HashSet<>(builtinOwned);
        for (UserDecoration d : gachaTitles) {
            allOwned.add(d.getItemName()); // gacha 称号用名字做 code
        }

        // 同步 ownedTitles
        user.setOwnedTitles(new ArrayList<>(allOwned));
        String wearing = user.getDisplayTitle();
        if (wearing == null || !allOwned.contains(wearing)) {
            wearing = "newbie";
            user.setDisplayTitle(wearing);
        }
        userRepository.save(user);

        List<Map<String, Object>> result = new ArrayList<>();
        // 内置称号
        for (TitleCatalog.TitleDef def : TitleCatalog.TITLES) {
            Map<String, Object> m = new HashMap<>();
            m.put("code", def.code);
            m.put("name", def.name);
            m.put("requirement", def.requirement);
            m.put("unlocked", builtinOwned.contains(def.code));
            m.put("wearing", def.code.equals(wearing));
            m.put("source", "builtin");
            result.add(m);
        }
        // 抽卡称号
        for (UserDecoration d : gachaTitles) {
            Map<String, Object> m = new HashMap<>();
            m.put("code", d.getItemName());
            m.put("name", d.getItemName());
            m.put("requirement", "抽卡获得");
            m.put("unlocked", true);
            m.put("wearing", d.getItemName().equals(wearing));
            m.put("source", "gacha");
            result.add(m);
        }
        return result;
    }

    public User setDisplayTitle(String userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        List<String> owned = TitleCatalog.computeOwned(user);
        if (!owned.contains(code)) {
            throw new IllegalArgumentException("该称号尚未解锁");
        }
        user.setOwnedTitles(owned);
        user.setDisplayTitle(code);
        User saved = userRepository.save(user);
        saved.setPassword(null);
        return saved;
    }

    // ============ 个人资料汇总 ============

    public Map<String, Object> getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        // 合并内置称号 + 抽卡称号
        List<String> builtinOwned = TitleCatalog.computeOwned(user);
        List<UserDecoration> gachaTitles = decorationRepository.findByUserIdAndItemType(userId, "TITLE");

        Set<String> allOwned = new HashSet<>(builtinOwned);
        for (UserDecoration d : gachaTitles) {
            allOwned.add(d.getItemName());
        }
        user.setOwnedTitles(new ArrayList<>(allOwned));
        String displayTitle = user.getDisplayTitle();
        if (displayTitle == null || !allOwned.contains(displayTitle)) {
            displayTitle = "newbie";
            user.setDisplayTitle(displayTitle);
        }
        userRepository.save(user);

        // 查找当前佩戴称号的名称（先查内置，再查抽卡）
        final String currentTitle = displayTitle;
        String displayTitleName = "";
        TitleCatalog.TitleDef def = TitleCatalog.findByCode(currentTitle);
        if (def != null) {
            displayTitleName = def.name;
        } else {
            // 可能来自抽卡
            Optional<UserDecoration> gachaTitle = decorationRepository
                .findByUserIdAndItemType(userId, "TITLE").stream()
                .filter(d -> d.getItemName().equals(currentTitle)).findFirst();
            displayTitleName = gachaTitle.map(UserDecoration::getItemName).orElse(currentTitle);
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("nickname", user.getNickname());
        profile.put("avatar", user.getAvatar());
        profile.put("reputation", user.getReputation() == null ? 0 : user.getReputation());
        profile.put("questionCount", user.getQuestionCount() == null ? 0 : user.getQuestionCount());
        profile.put("answerCount", user.getAnswerCount() == null ? 0 : user.getAnswerCount());
        profile.put("followingCount", user.getFollowingCount() == null ? 0 : user.getFollowingCount());
        profile.put("followerCount", user.getFollowerCount() == null ? 0 : user.getFollowerCount());
        profile.put("displayTitle", displayTitle);
        profile.put("displayTitleName", displayTitleName);
        return profile;
    }
}
