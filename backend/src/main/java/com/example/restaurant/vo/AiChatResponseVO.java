package com.example.restaurant.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiChatResponseVO {
    private String sessionId;
    private List<String> thinking = new ArrayList<>();
    private String answer;
    private List<String> actions = new ArrayList<>();
}
