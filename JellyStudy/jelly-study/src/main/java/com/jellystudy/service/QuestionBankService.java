package com.jellystudy.service;

import com.jellystudy.entity.QuestionBankItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QuestionBankService {

    List<QuestionBankItem> findByKnowledgePointId(String knowledgePointId);

    long countByKnowledgePointId(String knowledgePointId);

    QuestionBankItem create(QuestionBankItem item);

    QuestionBankItem update(String id, QuestionBankItem item);

    void delete(String id);

    Optional<QuestionBankItem> findById(String id);

    /** AI 生成题目，返回生成的题目列表 */
    List<QuestionBankItem> generateByAI(String knowledgePointId, String knowledgePointTitle,
                                        String knowledgePointContent, int count);

    /** 校验单题答案，返回 {correct: bool, explanation: str, correctAnswer: str} */
    Map<String, Object> checkAnswer(String questionId, String userAnswer);

    /** 闯关模式提交整组答案，第一项为 {total, correct, score} 汇总 */
    List<Map<String, Object>> submitAnswers(List<Map<String, String>> submissions);
}
