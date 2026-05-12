package com.example.restaurant.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiChatResponseVO {
    private String sessionId;
    private String provider;
    private String model;
    private Boolean fallback = false;
    private String errorMessage;
    private List<String> thinking = new ArrayList<>();
    private String answer;
    private List<String> actions = new ArrayList<>();
}
