package com.jellystudy.controller;

import com.jellystudy.entity.ApiResponse;
import com.jellystudy.entity.QuestionBankItem;
import com.jellystudy.service.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question-bank")
@CrossOrigin(origins = "*")
public class QuestionBankController {

    @Autowired
    private QuestionBankService questionBankService;

    /** 获取某知识点的所有题目 */
    @GetMapping("/knowledge-point/{kpId}")
    public ApiResponse<List<QuestionBankItem>> getByKnowledgePoint(@PathVariable String kpId) {
        return ApiResponse.success(questionBankService.findByKnowledgePointId(kpId));
    }

    /** 获取题目数量 */
    @GetMapping("/knowledge-point/{kpId}/count")
    public ApiResponse<Long> countByKnowledgePoint(@PathVariable String kpId) {
        return ApiResponse.success(questionBankService.countByKnowledgePointId(kpId));
    }

    /** AI 生成题目 */
    @PostMapping("/generate")
    public ApiResponse<List<QuestionBankItem>> generateByAI(@RequestBody Map<String, Object> req) {
        String kpId = (String) req.get("knowledgePointId");
        String kpTitle = (String) req.getOrDefault("knowledgePointTitle", "");
        String kpContent = (String) req.getOrDefault("knowledgePointContent", "");
        int count = req.containsKey("count") ? ((Number) req.get("count")).intValue() : 5;

        if (kpId == null || kpId.isBlank()) {
            return ApiResponse.error(400, "knowledgePointId 不能为空");
        }

        List<QuestionBankItem> items = questionBankService.generateByAI(kpId, kpTitle, kpContent, count);
        return ApiResponse.success(items);
    }

    /** 手动添加题目 */
    @PostMapping
    public ApiResponse<QuestionBankItem> create(@RequestBody QuestionBankItem item) {
        return ApiResponse.success(questionBankService.create(item));
    }

    /** 编辑题目 */
    @PutMapping("/{id}")
    public ApiResponse<QuestionBankItem> update(@PathVariable String id, @RequestBody QuestionBankItem item) {
        return ApiResponse.success(questionBankService.update(id, item));
    }

    /** 删除题目 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        questionBankService.delete(id);
        return ApiResponse.success();
    }

    /** 校验单题 */
    @PostMapping("/check")
    public ApiResponse<Map<String, Object>> checkAnswer(@RequestBody Map<String, String> req) {
        return ApiResponse.success(questionBankService.checkAnswer(
            req.get("questionId"), req.get("answer")));
    }

    /** 闯关提交 */
    @PostMapping("/submit")
    public ApiResponse<List<Map<String, Object>>> submitAnswers(@RequestBody List<Map<String, String>> submissions) {
        return ApiResponse.success(questionBankService.submitAnswers(submissions));
    }
}
