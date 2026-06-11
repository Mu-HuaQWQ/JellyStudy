package com.jellystudy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "gacha_items")
public class GachaItem {

    @Id
    private String id;

    private String name;           // 物品名称，如"星辰背景"

    private String type;           // 类型: TITLE / BACKGROUND / CHATBOX / DECORATION

    private String rarity;         // 稀有度: COMMON / RARE / EPIC / LEGENDARY / MYTHIC

    private String imageUrl;       // 图片地址（可选）

    private String description;    // 描述

    private Integer fragmentsRequired; // 合成所需碎片数

    private Boolean enabled;       // 是否启用，默认true
}
