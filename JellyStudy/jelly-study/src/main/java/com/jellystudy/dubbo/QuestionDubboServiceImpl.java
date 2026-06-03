package com.jellystudy.dubbo;

import com.jellystudy.api.QuestionDTO;
import com.jellystudy.api.QuestionDubboService;
import com.jellystudy.entity.Question;
import com.jellystudy.entity.Question.QuestionStatus;
import com.jellystudy.repository.QuestionRepository;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DubboService(version = "1.0.0", group = "question", timeout = 30000)
public class QuestionDubboServiceImpl implements QuestionDubboService {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Override
    public QuestionDTO createQuestion(QuestionDTO questionDTO) {
        Question question = Question.builder()
                .title(questionDTO.getTitle())
                .content(questionDTO.getContent())
                .authorId(questionDTO.getAuthorId())
                .authorName(questionDTO.getAuthorName())
                .knowledgePointId(questionDTO.getKnowledgePointId())
                .knowledgePointTitle(questionDTO.getKnowledgePointTitle())
                .tags(questionDTO.getTags())
                .viewCount(0)
                .answerCount(0)
                .likeCount(0)
                .isDeleted(false)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        Question saved = questionRepository.save(question);
        return toDTO(saved);
    }
    
    @Override
    public QuestionDTO getQuestionById(String id) {
        return questionRepository.findById(id)
                .filter(q -> !q.getIsDeleted())
                .map(this::toDTO)
                .orElse(null);
    }
    
    @Override
    public List<QuestionDTO> getAllQuestions() {
        return questionRepository.findAll().stream()
                .filter(q -> !q.getIsDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<QuestionDTO> getQuestionsByAuthorId(String authorId) {
        return questionRepository.findByAuthorId(authorId).stream()
                .filter(q -> !q.getIsDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<QuestionDTO> getQuestionsByKnowledgePointId(String knowledgePointId) {
        return questionRepository.findByKnowledgePointId(knowledgePointId).stream()
                .filter(q -> !q.getIsDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<QuestionDTO> searchQuestions(String keyword) {
        return questionRepository.searchByTitle(keyword).stream()
                .filter(q -> !q.getIsDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public QuestionDTO updateQuestion(String id, QuestionDTO questionDTO) {
        Optional<Question> questionOpt = questionRepository.findById(id);
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            if (questionDTO.getTitle() != null) {
                question.setTitle(questionDTO.getTitle());
            }
            if (questionDTO.getContent() != null) {
                question.setContent(questionDTO.getContent());
            }
            question.setUpdateTime(LocalDateTime.now());
            return toDTO(questionRepository.save(question));
        }
        return null;
    }
    
    @Override
    public void deleteQuestion(String id) {
        Optional<Question> questionOpt = questionRepository.findById(id);
        questionOpt.ifPresent(question -> {
            question.setIsDeleted(true);
            question.setDeletedReason("User deleted");
            question.setStatus(QuestionStatus.DELETED);
            question.setUpdateTime(LocalDateTime.now());
            questionRepository.save(question);
        });
    }
    
    @Override
    public void deleteQuestion(String id, String userId) {
        Optional<Question> questionOpt = questionRepository.findById(id);
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            if (userId.equals(question.getAuthorId())) {
                question.setIsDeleted(true);
                question.setDeletedReason("User deleted");
                question.setStatus(QuestionStatus.DELETED);
                question.setUpdateTime(LocalDateTime.now());
                questionRepository.save(question);
            } else {
                throw new RuntimeException("You can only delete your own question");
            }
        } else {
            throw new RuntimeException("Question not found");
        }
    }
    
    @Override
    public Long count() {
        return questionRepository.countNotDeleted();
    }
    
    @Override
    public List<QuestionDTO> getHotQuestions(int limit) {
        return questionRepository.findHotQuestions(0).stream()
                .filter(q -> !q.getIsDeleted())
                .map(this::toDTO)
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<QuestionDTO> getTopViewed(int limit) {
        return questionRepository.findTopViewed(PageRequest.of(0, limit)).stream()
                .filter(q -> !q.getIsDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public QuestionDTO likeQuestion(String questionId, String userId) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            question.setLikeCount(question.getLikeCount() + 1);
            question.setUpdateTime(LocalDateTime.now());
            return toDTO(questionRepository.save(question));
        }
        return null;
    }
    
    @Override
    public QuestionDTO unlikeQuestion(String questionId, String userId) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            question.setLikeCount(Math.max(0, question.getLikeCount() - 1));
            question.setUpdateTime(LocalDateTime.now());
            return toDTO(questionRepository.save(question));
        }
        return null;
    }
    
    private QuestionDTO toDTO(Question question) {
        return QuestionDTO.builder()
                .id(question.getId())
                .title(question.getTitle())
                .content(question.getContent())
                .authorId(question.getAuthorId())
                .authorName(question.getAuthorName())
                .knowledgePointId(question.getKnowledgePointId())
                .knowledgePointTitle(question.getKnowledgePointTitle())
                .tags(question.getTags())
                .viewCount(question.getViewCount())
                .answerCount(question.getAnswerCount())
                .likeCount(question.getLikeCount())
                .isDeleted(question.getIsDeleted())
                .build();
    }
}
