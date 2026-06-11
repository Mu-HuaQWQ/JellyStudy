package com.jellystudy.controller;

import com.jellystudy.entity.ApiResponse;
import com.jellystudy.service.CreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/credits")
@CrossOrigin(origins = "*")
public class CreditController {

    @Autowired
    private CreditService creditService;

    /** 获取用户信用点信息 */
    @GetMapping("/user/{userId}")
    public ApiResponse<Map<String, Object>> getUserCreditInfo(@PathVariable String userId) {
        return ApiResponse.success(creditService.getUserCreditInfo(userId));
    }

    /** 赚取信用点 */
    @PostMapping("/earn")
    public ApiResponse<Map<String, Object>> earnCredits(@RequestBody Map<String, Object> req) {
        String userId = (String) req.get("userId");
        int amount = req.containsKey("amount") ? ((Number) req.get("amount")).intValue() : 0;
        String reason = (String) req.getOrDefault("reason", "");
        return ApiResponse.success(creditService.earnCredits(userId, amount, reason));
    }

    /** 抽卡 */
    @PostMapping("/gacha")
    public ApiResponse<List<Map<String, Object>>> gachaPull(@RequestBody Map<String, Object> req) {
        String userId = (String) req.get("userId");
        int times = req.containsKey("times") ? ((Number) req.get("times")).intValue() : 1;
        if (times != 1 && times != 10) times = 1;
        return ApiResponse.success(creditService.gachaPull(userId, times));
    }

    /** 合成碎片 */
    @PostMapping("/synthesize")
    public ApiResponse<Map<String, Object>> synthesize(@RequestBody Map<String, String> req) {
        return ApiResponse.success(creditService.synthesize(
            req.get("userId"), req.get("itemId")));
    }

    /** 获取碎片列表 */
    @GetMapping("/fragments/{userId}")
    public ApiResponse<List<Map<String, Object>>> getFragments(@PathVariable String userId) {
        return ApiResponse.success(creditService.getUserFragments(userId));
    }

    /** 获取已拥有的装饰 */
    @GetMapping("/decorations/{userId}")
    public ApiResponse<List<Map<String, Object>>> getDecorations(@PathVariable String userId) {
        return ApiResponse.success(creditService.getUserDecorations(userId));
    }

    /** 佩戴/卸下装饰 */
    @PostMapping("/equip")
    public ApiResponse<Map<String, Object>> toggleEquip(@RequestBody Map<String, Object> req) {
        String userId = (String) req.get("userId");
        String decorationId = (String) req.get("decorationId");
        boolean equip = req.containsKey("equip") && (Boolean) req.get("equip");
        return ApiResponse.success(creditService.toggleEquip(userId, decorationId, equip));
    }
}
