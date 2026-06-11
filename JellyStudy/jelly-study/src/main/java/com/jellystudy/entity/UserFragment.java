package com.jellystudy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_fragments")
@CompoundIndex(def = "{'userId': 1, 'itemId': 1}", unique = true)
public class UserFragment {

    @Id
    private String id;

    private String userId;         // 用户ID

    private String itemId;         // GachaItem ID

    private String itemName;       // 冗余：物品名称

    private String itemType;       // 冗余：物品类型

    private String rarity;         // 冗余：稀有度

    private Integer count;         // 碎片数量，默认1
}
