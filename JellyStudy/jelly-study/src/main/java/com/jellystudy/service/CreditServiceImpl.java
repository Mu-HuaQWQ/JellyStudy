package com.jellystudy.service;

import com.jellystudy.entity.*;
import com.jellystudy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CreditServiceImpl implements CreditService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GachaItemRepository gachaItemRepository;
    @Autowired
    private UserFragmentRepository fragmentRepository;
    @Autowired
    private UserDecorationRepository decorationRepository;

    private static final int GACHA_COST = 160;
    private static final int[] LEVEL_THRESHOLDS = {0, 1000, 2000, 4000, 8000, 16000, 32000};

    // 稀有度概率: 普通30% 优秀20% 精良20% 史诗20% 传说10%
    private static final String[] RARITIES = {"COMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC"};
    private static final double[] RARITY_WEIGHTS = {0.30, 0.20, 0.20, 0.20, 0.10};

    // 合成所需碎片数
    private static final Map<String, Integer> SYNTHESIS_COST = Map.of(
        "COMMON", 1, "RARE", 3, "EPIC", 5, "LEGENDARY", 10, "MYTHIC", 20
    );

    private final Random random = new Random();

    @Override
    public Map<String, Object> earnCredits(String userId, int amount, String reason) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return Map.of("success", false, "message", "用户不存在");

        if (user.getCreditPoints() == null) user.setCreditPoints(0);
        user.setCreditPoints(user.getCreditPoints() + amount);
        userRepository.save(user);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("earned", amount);
        result.put("balance", user.getCreditPoints());
        result.put("reason", reason);
        return result;
    }

    @Override
    public List<Map<String, Object>> gachaPull(String userId, int times) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return List.of(Map.of("error", "用户不存在"));

        if (user.getCreditPoints() == null) user.setCreditPoints(0);
        if (user.getTotalSpent() == null) user.setTotalSpent(0);
        if (user.getLevel() == null) user.setLevel(0);

        int totalCost = GACHA_COST * times;
        if (user.getCreditPoints() < totalCost) {
            return List.of(Map.of("error", "信用点不足，需要 " + totalCost + "，当前 " + user.getCreditPoints()));
        }

        // 扣款
        user.setCreditPoints(user.getCreditPoints() - totalCost);
        user.setTotalSpent(user.getTotalSpent() + totalCost);

        // 检查升级
        int newLevel = calcLevel(user.getTotalSpent());
        boolean leveledUp = newLevel > user.getLevel();
        user.setLevel(newLevel);
        userRepository.save(user);

        // 获取奖池
        List<GachaItem> pool = gachaItemRepository.findByEnabledTrue();
        if (pool.isEmpty()) {
            return List.of(Map.of("error", "奖池为空"));
        }

        // 保底计数器（简化：用 totalSpent / GACHA_COST 作为总抽数）
        int totalPulls = user.getTotalSpent() / GACHA_COST;

        List<Map<String, Object>> results = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            int pullIndex = totalPulls - times + i + 1;
            Map<String, Object> pull = doSinglePull(userId, pool, pullIndex);
            results.add(pull);
        }

        // 添加余额信息
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("balance", user.getCreditPoints());
        summary.put("totalSpent", user.getTotalSpent());
        summary.put("level", user.getLevel());
        if (leveledUp) summary.put("leveledUp", true);
        results.add(0, summary);

        return results;
    }

    private Map<String, Object> doSinglePull(String userId, List<GachaItem> pool, int pullIndex) {
        boolean pityEpic = pullIndex % 10 == 0;
        boolean pityLegendary = pullIndex % 20 == 0;

        String rarity = rollRarity(pityEpic, pityLegendary);
        List<GachaItem> filtered = pool.stream()
            .filter(i -> i.getRarity().equals(rarity))
            .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            filtered = pool;
        }

        GachaItem item = filtered.get(random.nextInt(filtered.size()));
        int fragmentCount = 1 + random.nextInt(5); // 1-5个碎片
        boolean isFullItem = random.nextInt(100) < 1; // 1%概率直接出本体

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("itemId", item.getId());
        result.put("itemName", item.getName());
        result.put("itemType", item.getType());
        result.put("rarity", item.getRarity());
        result.put("isFullItem", isFullItem);
        result.put("fragmentCount", fragmentCount);

        if (isFullItem) {
            // 直接获得本体
            UserDecoration deco = decorationRepository.findByUserIdAndItemId(userId, item.getId()).orElse(null);
            if (deco == null) {
                deco = UserDecoration.builder()
                    .userId(userId).itemId(item.getId())
                    .itemName(item.getName()).itemType(item.getType())
                    .rarity(item.getRarity()).equipped(false)
                    .obtainTime(LocalDateTime.now()).build();
                decorationRepository.save(deco);
            }
        } else {
            // 获得碎片
            UserFragment frag = fragmentRepository.findByUserIdAndItemId(userId, item.getId()).orElse(null);
            if (frag == null) {
                frag = UserFragment.builder()
                    .userId(userId).itemId(item.getId())
                    .itemName(item.getName()).itemType(item.getType())
                    .rarity(item.getRarity()).count(fragmentCount).build();
            } else {
                frag.setCount(frag.getCount() + fragmentCount);
            }
            fragmentRepository.save(frag);

            // 检查是否可以合成
            int required = item.getFragmentsRequired() != null ? item.getFragmentsRequired()
                : SYNTHESIS_COST.getOrDefault(item.getRarity(), 1);
            result.put("canSynthesize", frag.getCount() >= required);
            result.put("synthesizeRequired", required);
        }

        return result;
    }

    private String rollRarity(boolean pityEpic, boolean pityLegendary) {
        if (pityLegendary) return "LEGENDARY";
        if (pityEpic) {
            double r = random.nextDouble();
            if (r < 0.4) return "EPIC";
            if (r < 0.7) return "LEGENDARY";
            return "MYTHIC";
        }

        double roll = random.nextDouble();
        double cumulative = 0;
        for (int i = 0; i < RARITIES.length; i++) {
            cumulative += RARITY_WEIGHTS[i];
            if (roll < cumulative) return RARITIES[i];
        }
        return "COMMON";
    }

    @Override
    public Map<String, Object> synthesize(String userId, String itemId) {
        GachaItem item = gachaItemRepository.findById(itemId).orElse(null);
        if (item == null) return Map.of("success", false, "message", "物品不存在");

        UserFragment frag = fragmentRepository.findByUserIdAndItemId(userId, itemId).orElse(null);
        if (frag == null) return Map.of("success", false, "message", "没有该物品的碎片");

        int required = item.getFragmentsRequired() != null ? item.getFragmentsRequired()
            : SYNTHESIS_COST.getOrDefault(item.getRarity(), 1);
        if (frag.getCount() < required) {
            return Map.of("success", false, "message",
                "碎片不足，需要 " + required + "，当前 " + frag.getCount());
        }

        // 扣除碎片
        frag.setCount(frag.getCount() - required);
        if (frag.getCount() <= 0) {
            fragmentRepository.delete(frag);
        } else {
            fragmentRepository.save(frag);
        }

        // 添加装饰
        UserDecoration deco = decorationRepository.findByUserIdAndItemId(userId, itemId).orElse(null);
        if (deco == null) {
            deco = UserDecoration.builder()
                .userId(userId).itemId(itemId).itemName(item.getName())
                .itemType(item.getType()).rarity(item.getRarity())
                .equipped(false).obtainTime(LocalDateTime.now()).build();
            decorationRepository.save(deco);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "合成成功！获得了 " + item.getName());
        result.put("itemName", item.getName());
        result.put("remainingFragments", frag.getCount() > 0 ? frag.getCount() : 0);
        return result;
    }

    @Override
    public Map<String, Object> getUserCreditInfo(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return Map.of("error", "用户不存在");

        if (user.getCreditPoints() == null) user.setCreditPoints(0);
        if (user.getTotalSpent() == null) user.setTotalSpent(0);
        if (user.getLevel() == null) user.setLevel(0);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("creditPoints", user.getCreditPoints());
        info.put("totalSpent", user.getTotalSpent());
        info.put("level", user.getLevel());
        info.put("nextLevelAt", user.getLevel() < 6 ? LEVEL_THRESHOLDS[user.getLevel() + 1] : null);
        info.put("gachaCost", GACHA_COST);
        return info;
    }

    @Override
    public List<Map<String, Object>> getUserFragments(String userId) {
        return fragmentRepository.findByUserId(userId).stream().map(f -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.getId());
            m.put("itemId", f.getItemId());
            m.put("itemName", f.getItemName());
            m.put("itemType", f.getItemType());
            m.put("rarity", f.getRarity());
            m.put("count", f.getCount());
            int required = SYNTHESIS_COST.getOrDefault(f.getRarity(), 1);
            m.put("synthesizeRequired", required);
            m.put("canSynthesize", f.getCount() >= required);
            return m;
        }).toList();
    }

    @Override
    public List<Map<String, Object>> getUserDecorations(String userId) {
        return decorationRepository.findByUserId(userId).stream().map(d -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("itemId", d.getItemId());
            m.put("itemName", d.getItemName());
            m.put("itemType", d.getItemType());
            m.put("rarity", d.getRarity());
            m.put("equipped", d.isEquipped());
            return m;
        }).toList();
    }

    @Override
    public Map<String, Object> toggleEquip(String userId, String decorationId, boolean equip) {
        UserDecoration deco = decorationRepository.findById(decorationId).orElse(null);
        if (deco == null || !deco.getUserId().equals(userId)) {
            return Map.of("success", false, "message", "装饰不存在");
        }
        // 同类型只能佩戴一个，先卸下同类型
        if (equip) {
            List<UserDecoration> equipped = decorationRepository.findByUserIdAndEquippedTrue(userId);
            for (UserDecoration d : equipped) {
                if (d.getItemType().equals(deco.getItemType())) {
                    d.setEquipped(false);
                    decorationRepository.save(d);
                }
            }
            // 称号类型同步到用户表
            if ("TITLE".equals(deco.getItemType())) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    if (user.getOwnedTitles() == null) user.setOwnedTitles(new java.util.ArrayList<>());
                    if (!user.getOwnedTitles().contains(deco.getItemName())) {
                        user.getOwnedTitles().add(deco.getItemName());
                    }
                    user.setDisplayTitle(deco.getItemName());
                    userRepository.save(user);
                }
            }
        }
        deco.setEquipped(equip);
        decorationRepository.save(deco);

        // 卸下称号时清除用户表displayTitle
        if (!equip && "TITLE".equals(deco.getItemType())) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setDisplayTitle(null);
                userRepository.save(user);
            }
        }

        return Map.of("success", true, "equipped", equip);
    }

    private int calcLevel(int totalSpent) {
        int level = 0;
        for (int i = 1; i < LEVEL_THRESHOLDS.length; i++) {
            if (totalSpent >= LEVEL_THRESHOLDS[i]) level = i;
        }
        return level;
    }
}
