package com.jellystudy.service;

public interface AIService {
    
    String answerQuestion(String questionTitle, String questionContent);

    String answerQuestion(String questionTitle, String questionContent, String persona);
}
