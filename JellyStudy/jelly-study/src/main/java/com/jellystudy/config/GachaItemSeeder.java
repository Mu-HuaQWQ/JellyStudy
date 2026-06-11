package com.jellystudy.config;

import com.jellystudy.entity.GachaItem;
import com.jellystudy.repository.GachaItemRepository;
import com.jellystudy.repository.UserFragmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class GachaItemSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(GachaItemSeeder.class);

    @Autowired
    private GachaItemRepository gachaItemRepository;

    @Autowired
    private UserFragmentRepository fragmentRepository;

    // 固定语义ID，确保重启后 itemId 不变，碎片才能正确叠加
    private static final Map<String, String[]> POOL = Map.ofEntries(
        // 称号类
        Map.entry("title_fly_spring",     new String[]{"飞吧，朝向春天", "TITLE", "LEGENDARY", "10", "传说称号：飞吧，朝向春天"}),
        // 背景类
        Map.entry("bg_pure_white",        new String[]{"纯白背景", "BACKGROUND", "COMMON",   "1",  "简洁的纯白背景"}),
        Map.entry("bg_starry",            new String[]{"星空背景", "BACKGROUND", "RARE",     "3",  "璀璨星空主题背景"}),
        Map.entry("bg_bamboo",            new String[]{"竹林背景", "BACKGROUND", "EPIC",     "5",  "幽静竹林主题背景"}),
        Map.entry("bg_galaxy",            new String[]{"银河背景", "BACKGROUND", "LEGENDARY","10", "浩瀚银河主题背景"}),
        Map.entry("bg_aurora",            new String[]{"极光背景", "BACKGROUND", "MYTHIC",   "20", "梦幻极光主题背景"}),
        // 聊天框类
        Map.entry("chatbox_simple",       new String[]{"简约边框", "CHATBOX", "COMMON",   "1",  "简约聊天框边框"}),
        Map.entry("chatbox_flower",       new String[]{"花藤边框", "CHATBOX", "RARE",     "3",  "花藤缠绕聊天框"}),
        Map.entry("chatbox_flow",         new String[]{"流光边框", "CHATBOX", "EPIC",     "5",  "流光溢彩聊天框"}),
        Map.entry("chatbox_dragon",       new String[]{"龙纹边框", "CHATBOX", "LEGENDARY","10", "金龙纹聊天框"}),
        Map.entry("chatbox_oracle",       new String[]{"神谕边框", "CHATBOX", "MYTHIC",   "20", "神谕降临聊天框"}),
        // 小摆件类
        Map.entry("deco_clover",          new String[]{"幸运草",   "DECORATION", "COMMON",   "1",  "四叶幸运草小摆件"}),
        Map.entry("deco_crystal_ball",    new String[]{"水晶球",   "DECORATION", "RARE",     "3",  "占卜水晶球摆件"}),
        Map.entry("deco_totoro",          new String[]{"龙猫",     "DECORATION", "EPIC",     "5",  "可爱龙猫摆件"}),
        Map.entry("deco_phoenix",         new String[]{"凤凰羽",   "DECORATION", "LEGENDARY","10", "凤凰羽毛摆件"}),
        Map.entry("deco_hourglass",       new String[]{"时空沙漏", "DECORATION", "MYTHIC",   "20", "传说级时空沙漏摆件"})
    );

    @Override
    public void run(String... args) {
        logger.info("Initializing gacha pool (idempotent, {} items)...", POOL.size());

        for (Map.Entry<String, String[]> e : POOL.entrySet()) {
            String fixedId = e.getKey();
            String[] v = e.getValue();

            Optional<GachaItem> existing = gachaItemRepository.findById(fixedId);
            if (existing.isPresent()) {
                // 已存在则更新字段
                GachaItem item = existing.get();
                item.setName(v[0]);
                item.setType(v[1]);
                item.setRarity(v[2]);
                item.setFragmentsRequired(Integer.parseInt(v[3]));
                item.setDescription(v[4]);
                item.setEnabled(true);
                gachaItemRepository.save(item);
            } else {
                // 不存在则新建
                GachaItem item = GachaItem.builder()
                    .name(v[0])
                    .type(v[1])
                    .rarity(v[2])
                    .fragmentsRequired(Integer.parseInt(v[3]))
                    .description(v[4])
                    .enabled(true)
                    .build();
                item.setId(fixedId);
                gachaItemRepository.save(item);
            }
        }

        // 清理旧的无ID/随机ID 物品（多实例重启导致的重复）
        gachaItemRepository.deleteByIdNotIn(POOL.keySet());

        // 清理引用了旧 itemId 的孤儿碎片
        fragmentRepository.deleteByItemIdNotIn(POOL.keySet());
        logger.info("Gacha pool initialized with {} items.", POOL.size());
    }
}
