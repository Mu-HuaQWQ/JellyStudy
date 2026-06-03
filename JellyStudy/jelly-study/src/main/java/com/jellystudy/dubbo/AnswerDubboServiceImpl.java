package com.jellystudy.dubbo;

import com.jellystudy.api.AnswerDTO;
import com.jellystudy.api.AnswerDubboService;
import com.jellystudy.entity.Answer;
import com.jellystudy.repository.AnswerRepository;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DubboService(version = "1.0.0", group = "answer", timeout = 30000)
public class AnswerDubboServiceImpl implements AnswerDubboService {
    
    @Autowired
    private AnswerRepository answerRepository;
    
    @Override
    public AnswerDTO createAnswer(AnswerDTO answerDTO) {
        Answer answer = Answer.builder()
                .questionId(answerDTO.getQuestionId())
                .authorId(answerDTO.getAuthorId())
                .authorName(answerDTO.getAuthorName())
                .content(answerDTO.getContent())
                .likeCount(0)
                .isAccepted(false)
                .isDeleted(false)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        Answer saved = answerRepository.save(answer);
        return toDTO(saved);
    }
    
    @Override
    public AnswerDTO getAnswerById(String id) {
        return answerRepository.findById(id)
                .filter(a -> !a.getIsDeleted())
                .map(this::toDTO)
                .orElse(null);
    }
    
    @Override
    public List<AnswerDTO> getAnswersByQuestionId(String questionId) {
        return answerRepository.findByQuestionId(questionId).stream()
                .filter(a -> !a.getIsDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AnswerDTO> getAnswersByAuthorId(String authorId) {
        return answerRepository.findByAuthorId(authorId).stream()
                .filter(a -> !a.getIsDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public AnswerDTO updateAnswer(String id, AnswerDTO answerDTO) {
        Optional<Answer> answerOpt = answerRepository.findById(id);
        if (answerOpt.isPresent()) {
            Answer answer = answerOpt.get();
            if (answerDTO.getContent() != null) {
                answer.setContent(answerDTO.getContent());
            }
            answer.setUpdateTime(LocalDateTime.now());
            return toDTO(answerRepository.save(answer));
        }
        return null;
    }
    
    @Override
    public void deleteAnswer(String id) {
        Optional<Answer> answerOpt = answerRepository.findById(id);
        answerOpt.ifPresent(answer -> {
            answer.setIsDeleted(true);
            answer.setDeletedReason("User deleted");
            answer.setUpdateTime(LocalDateTime.now());
            answerRepository.save(answer);
        });
    }
    
    @Override
    public void deleteAnswer(String id, String userId) {
        Optional<Answer> answerOpt = answerRepository.findById(id);
        if (answerOpt.isPresent()) {
            Answer answer = answerOpt.get();
            if (userId.equals(answer.getAuthorId())) {
                answer.setIsDeleted(true);
                answer.setDeletedReason("User deleted");
                answer.setUpdateTime(LocalDateTime.now());
                answerRepository.save(answer);
            } else {
                throw new RuntimeException("You can only delete your own answer");
            }
        } else {
            throw new RuntimeException("Answer not found");
        }
    }
    
    @Override
    public AnswerDTO acceptAnswer(String answerId, String userId) {
        Optional<Answer> answerOpt = answerRepository.findById(answerId);
        if (answerOpt.isPresent()) {
            Answer answer = answerOpt.get();
            answer.setIsAccepted(true);
            answer.setUpdateTime(LocalDateTime.now());
            return toDTO(answerRepository.save(answer));
        }
        return null;
    }
    
    @Override
    public AnswerDTO likeAnswer(String answerId, String userId) {
        Optional<Answer> answerOpt = answerRepository.findById(answerId);
        if (answerOpt.isPresent()) {
            Answer answer = answerOpt.get();
            answer.setLikeCount(answer.getLikeCount() + 1);
            answer.setUpdateTime(LocalDateTime.now());
            return toDTO(answerRepository.save(answer));
        }
        return null;
    }
    
    @Override
    public AnswerDTO unlikeAnswer(String answerId, String userId) {
        Optional<Answer> answerOpt = answerRepository.findById(answerId);
        if (answerOpt.isPresent()) {
            Answer answer = answerOpt.get();
            answer.setLikeCount(Math.max(0, answer.getLikeCount() - 1));
            answer.setUpdateTime(LocalDateTime.now());
            return toDTO(answerRepository.save(answer));
        }
        return null;
    }
    
    @Override
    public Long count() {
        return answerRepository.count();
    }
    
    private AnswerDTO toDTO(Answer answer) {
        return AnswerDTO.builder()
                .id(answer.getId())
                .questionId(answer.getQuestionId())
                .authorId(answer.getAuthorId())
                .authorName(answer.getAuthorName())
                .content(answer.getContent())
                .likeCount(answer.getLikeCount())
                .isAccepted(answer.getIsAccepted())
                .isDeleted(answer.getIsDeleted())
                .build();
    }
}
