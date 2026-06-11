package com.jellystudy.config;

import com.jellystudy.entity.GachaItem;
import com.jellystudy.repository.GachaItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GachaItemSeeder implements CommandLineRunner {

    @Autowired
    private GachaItemRepository gachaItemRepository;

    @Override
    public void run(String... args) {
        // 每次启动清理旧奖池重新初始化
        gachaItemRepository.deleteAll();

        // 称号类
        gachaItemRepository.save(GachaItem.builder().name("飞吧，朝向春天").type("TITLE").rarity("LEGENDARY").fragmentsRequired(10).description("传说称号：飞吧，朝向春天").enabled(true).build());

        // 背景类
        gachaItemRepository.save(GachaItem.builder().name("纯白背景").type("BACKGROUND").rarity("COMMON").fragmentsRequired(1).description("简洁的纯白背景").enabled(true).build());
        gachaItemRepository.save(GachaItem.builder().name("星空背景").type("BACKGROUND").rarity("RARE").fragmentsRequired(3).description("璀璨星空主题背景").enabled(true).build());
        gachaItemRepository.save(GachaItem.builder().name("竹林背景").type("BACKGROUND").rarity("EPIC").fragmentsRequired(5).description("幽静竹林主题背景").enabled(true).build());
        gachaItemRepository.save(GachaItem.builder().name("银河背景").type("BACKGROUND").rarity("LEGENDARY").fragmentsRequired(10).description("浩瀚银河主题背景").enabled(true).build());
        gachaItemRepository.save(GachaItem.builder().name("极光背景").type("BACKGROUND").rarity("MYTHIC").fragmentsRequired(20).description("梦幻极光主题背景").enabled(true).build());

        // 聊天框类
        gachaItemRepository.save(GachaItem.builder().name("简约边框").type("CHATBOX").rarity("COMMON").fragmentsRequired(1).description("简约聊天框边框").enabled(true).build());
        gachaItemRepository.save(GachaItem.builder().name("花藤边框").type("CHATBOX").rarity("RARE").fragmentsRequired(3).description("花藤缠绕聊天框").enabled(true).build());
        gachaItemRepository.save(GachaItem.builder().name("流光边框").type("CHATBOX").rarity("EPIC").fragmentsRequired(5).description("流光溢彩聊天框").enabled(true).build());
        gachaItemRepository.save(GachaItem.builder().name("龙纹边框").type("CHATBOX").rarity("LEGENDARY").fragmentsRequired(10).description("金龙纹聊天框").enabled(true).build());
        gachaItemRepository.save(GachaItem.builder().name("神谕边框").type("CHATBOX").rarity("MYTHIC").fragmentsRequired(20).description("神谕降临聊天框").enabled(true).build());

        // 小摆件类
        gachaItemRepository.save(GachaItem.builder().name("幸运草").type("DECORATION").rarity("COMMON").fragmentsRequired(1).description("四叶幸运草小摆件").enabled(true).build());
        gachaItemRepository.save(GachaItem.builder().name("水晶球").type("DECORATION").rarity("RARE").fragmentsRequired(3).description("占卜水晶球摆件").enabled(true).build());
        gachaItemRepository.save(GachaItem.builder().name("龙猫").type("DECORATION").rarity("EPIC").fragmentsRequired(5).description("可爱龙猫摆件").enabled(true).build());
        gachaItemRepository.save(GachaItem.builder().name("凤凰羽").type("DECORATION").rarity("LEGENDARY").fragmentsRequired(10).description("凤凰羽毛摆件").enabled(true).build());
        gachaItemRepository.save(GachaItem.builder().name("时空沙漏").type("DECORATION").rarity("MYTHIC").fragmentsRequired(20).description("传说级时空沙漏摆件").enabled(true).build());
    }
}
