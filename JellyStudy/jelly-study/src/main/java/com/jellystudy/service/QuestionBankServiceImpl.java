package com.jellystudy.service;

import com.jellystudy.entity.QuestionBankItem;
import com.jellystudy.repository.QuestionBankItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class QuestionBankServiceImpl implements QuestionBankService {

    @Autowired
    private QuestionBankItemRepository repository;

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.base-url}")
    private String baseUrl;

    @Value("${ai.deepseek.model}")
    private String model;

    @Override
    public List<QuestionBankItem> findByKnowledgePointId(String knowledgePointId) {
        return repository.findByKnowledgePointId(knowledgePointId);
    }

    @Override
    public long countByKnowledgePointId(String knowledgePointId) {
        return repository.countByKnowledgePointId(knowledgePointId);
    }

    @Override
    public QuestionBankItem create(QuestionBankItem item) {
        return repository.save(item);
    }

    @Override
    public QuestionBankItem update(String id, QuestionBankItem item) {
        item.setId(id);
        return repository.save(item);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<QuestionBankItem> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<QuestionBankItem> generateByAI(String knowledgePointId, String knowledgePointTitle,
                                                String knowledgePointContent, int count) {
        if (count < 1) count = 5;
        if (count > 20) count = 20;

        String prompt = "请根据以下知识点生成" + count + "道题目，题型为选择题(CHOICE)和判断题(TF)混合，难度覆盖简单、中等、困难。\n\n" +
            "知识点标题：" + knowledgePointTitle + "\n" +
            "知识点内容：" + knowledgePointContent + "\n\n" +
            "请严格按照以下JSON数组格式返回，不要返回其他内容：\n" +
            "[\n" +
            "  {\n" +
            "    \"type\": \"CHOICE\",\n" +
            "    \"question\": \"题目内容\",\n" +
            "    \"options\": [\"A. 选项1\", \"B. 选项2\", \"C. 选项3\", \"D. 选项4\"],\n" +
            "    \"correctAnswer\": \"A. 选项1\",\n" +
            "    \"explanation\": \"解析为什么选这个\",\n" +
            "    \"difficulty\": \"简单\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"type\": \"TF\",\n" +
            "    \"question\": \"题目内容\",\n" +
            "    \"options\": [\"正确\", \"错误\"],\n" +
            "    \"correctAnswer\": \"正确\",\n" +
            "    \"explanation\": \"解析\",\n" +
            "    \"difficulty\": \"中等\"\n" +
            "  }\n" +
            "]\n" +
            "注意：判断题的options必须固定为[\"正确\", \"错误\"]，correctAnswer为\"正确\"或\"错误\"。选择题options必须是4个选项。JSON必须是合法的，不要包含注释。";

        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", Arrays.asList(
                Map.of("role", "system", "content", "你是一个专业的教育题目生成器。只返回JSON数组，不返回其他内容。"),
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v1/chat/completions",
                HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String raw = (String) message.get("content");
                    return parseAndSave(raw, knowledgePointId, knowledgePointTitle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private List<QuestionBankItem> parseAndSave(String raw, String kpId, String kpTitle) {
        List<QuestionBankItem> items = new ArrayList<>();
        try {
            int start = raw.indexOf('[');
            int end = raw.lastIndexOf(']');
            if (start != -1 && end != -1 && end > start) {
                String json = raw.substring(start, end + 1);
                ObjectMapper mapper = new ObjectMapper();
                List<Map> list = mapper.readValue(json, List.class);
                for (Object obj : list) {
                    Map m = (Map) obj;
                    QuestionBankItem item = QuestionBankItem.builder()
                        .knowledgePointId(kpId)
                        .knowledgePointTitle(kpTitle)
                        .type((String) m.get("type"))
                        .question((String) m.get("question"))
                        .options((List<String>) m.get("options"))
                        .correctAnswer((String) m.get("correctAnswer"))
                        .explanation((String) m.get("explanation"))
                        .difficulty((String) m.get("difficulty"))
                        .authorId("ai")
                        .build();
                    items.add(repository.save(item));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public Map<String, Object> checkAnswer(String questionId, String userAnswer) {
        Optional<QuestionBankItem> opt = repository.findById(questionId);
        if (opt.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("correct", false);
            result.put("explanation", "题目不存在");
            result.put("correctAnswer", "");
            return result;
        }
        QuestionBankItem item = opt.get();
        boolean correct = item.getCorrectAnswer().equals(userAnswer);
        Map<String, Object> result = new HashMap<>();
        result.put("correct", correct);
        result.put("explanation", item.getExplanation());
        result.put("correctAnswer", item.getCorrectAnswer());
        return result;
    }

    @Override
    public List<Map<String, Object>> submitAnswers(List<Map<String, String>> submissions) {
        List<Map<String, Object>> results = new ArrayList<>();
        int correctCount = 0;
        for (Map<String, String> sub : submissions) {
            String id = sub.get("questionId");
            String userAnswer = sub.get("answer");
            Map<String, Object> r = checkAnswer(id, userAnswer);
            r.put("questionId", id);
            results.add(r);
            if (Boolean.TRUE.equals(r.get("correct"))) correctCount++;
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("total", submissions.size());
        summary.put("correct", correctCount);
        summary.put("score", submissions.size() > 0 ? correctCount * 100 / submissions.size() : 0);
        results.add(0, summary);
        return results;
    }
}
