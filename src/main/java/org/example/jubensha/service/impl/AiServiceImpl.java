package org.example.jubensha.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.example.jubensha.service.AiService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    private static final String API_KEY = "sk-ace24dfb825148d3a71344db171744fd";
    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String generateRoleReply(String prompt) {
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        return generateChatReply(messages); // 复用新方法
    }

    // 新增：处理真正的多轮聊天
    @Override
    public String generateChatReply(List<Map<String, String>> messages) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(API_KEY);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("temperature", 0.7);

            // 将拼装好的历史消息（包含 system, user, assistant）直接传给大模型
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);

            JSONObject jsonObject = JSON.parseObject(response.getBody());
            String reply = jsonObject.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            return reply.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "【系统波动】AI陷入了沉思，请再说一次。";
        }
    }
}