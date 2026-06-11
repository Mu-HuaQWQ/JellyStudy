package com.jellystudy.service;

import java.util.Map;
import java.util.List;

public interface CreditService {

    /** 赚取信用点 */
    Map<String, Object> earnCredits(String userId, int amount, String reason);

    /** 抽卡 — times是次数（1或10） */
    List<Map<String, Object>> gachaPull(String userId, int times);

    /** 合成碎片 */
    Map<String, Object> synthesize(String userId, String itemId);

    /** 获取用户信用点信息 */
    Map<String, Object> getUserCreditInfo(String userId);

    /** 获取用户碎片列表 */
    List<Map<String, Object>> getUserFragments(String userId);

    /** 获取用户已拥有的装饰 */
    List<Map<String, Object>> getUserDecorations(String userId);

    /** 佩戴/卸下装饰 */
    Map<String, Object> toggleEquip(String userId, String decorationId, boolean equip);
}
