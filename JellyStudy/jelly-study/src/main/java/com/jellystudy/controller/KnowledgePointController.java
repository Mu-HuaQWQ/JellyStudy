package com.jellystudy.controller;

import com.jellystudy.entity.*;
import com.jellystudy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-points")
@CrossOrigin(origins = "*")
public class KnowledgePointController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ApiResponse<KnowledgePoint> createKnowledgePoint(
            @RequestBody @Validated KnowledgePoint knowledgePoint) {
        KnowledgePoint created = knowledgePointService.create(knowledgePoint);
        return ApiResponse.success(created);
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgePoint> getKnowledgePoint(@PathVariable String id) {
        return knowledgePointService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "Knowledge point not found"));
    }

    @GetMapping
    public ApiResponse<List<KnowledgePoint>> getAllKnowledgePoints() {
        List<KnowledgePoint> knowledgePoints = knowledgePointService.findAll();
        return ApiResponse.success(knowledgePoints);
    }

    @GetMapping("/category/{category}")
    public ApiResponse<List<KnowledgePoint>> getKnowledgePointsByCategory(
            @PathVariable String category) {
        List<KnowledgePoint> knowledgePoints = knowledgePointService.findByCategory(category);
        return ApiResponse.success(knowledgePoints);
    }

    @GetMapping("/tag/{tag}")
    public ApiResponse<List<KnowledgePoint>> getKnowledgePointsByTag(@PathVariable String tag) {
        List<KnowledgePoint> knowledgePoints = knowledgePointService.findByTag(tag);
        return ApiResponse.success(knowledgePoints);
    }

    @GetMapping("/search")
    public ApiResponse<List<KnowledgePoint>> searchKnowledgePoints(@RequestParam String keyword) {
        List<KnowledgePoint> knowledgePoints = knowledgePointService.search(keyword);
        return ApiResponse.success(knowledgePoints);
    }

    @GetMapping("/parent/{parentId}")
    public ApiResponse<List<KnowledgePoint>> getChildKnowledgePoints(@PathVariable String parentId) {
        List<KnowledgePoint> knowledgePoints = knowledgePointService.findByParentId(parentId);
        return ApiResponse.success(knowledgePoints);
    }

    @GetMapping("/recommend")
    public ApiResponse<List<KnowledgePoint>> getRecommendKnowledgePoints(
            @RequestParam(defaultValue = "10") int limit) {
        List<KnowledgePoint> knowledgePoints = knowledgePointService.getRecommendKnowledgePoints(limit);
        return ApiResponse.success(knowledgePoints);
    }

    @GetMapping("/{id}/questions")
    public ApiResponse<List<Question>> getQuestionsByKnowledgePoint(@PathVariable String id) {
        List<Question> questions = questionService.findByKnowledgePointId(id);
        return ApiResponse.success(questions);
    }

    @GetMapping("/count")
    public ApiResponse<Long> getKnowledgePointCount() {
        Long count = knowledgePointService.count();
        return ApiResponse.success(count);
    }

    @PutMapping("/{id}")
    public ApiResponse<KnowledgePoint> updateKnowledgePoint(
            @PathVariable String id,
            @RequestBody KnowledgePoint knowledgePoint) {
        knowledgePoint.setId(id);
        KnowledgePoint updated = knowledgePointService.update(knowledgePoint);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteKnowledgePoint(@PathVariable String id) {
        knowledgePointService.delete(id);
        return ApiResponse.success();
    }
}
