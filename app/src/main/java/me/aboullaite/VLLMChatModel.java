package me.aboullaite;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
public class VLLMChatModel implements ChatLanguageModel {

  private final String baseUrl;
  private final OkHttpClient client;
  private final ObjectMapper mapper;

  public VLLMChatModel(String baseUrl) {
    this.baseUrl = baseUrl;
    this.client = new OkHttpClient();
    this.mapper = new ObjectMapper();
  }

  @Override
  public Response<AiMessage> generate(List<ChatMessage> messages) {
    try {
      List<Map<String, String>> vllmMessages = new ArrayList<>();

      for (ChatMessage message : messages) {
        Map<String, String> msg = new HashMap<>();
        if (message instanceof UserMessage) {
          msg.put("role", "user");
        } else {
          msg.put("role", "assistant");
        }
        msg.put("content", message.text());
        vllmMessages.add(msg);
      }

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", "gemma-2b-it");
      requestBody.put("messages", vllmMessages);
      requestBody.put("temperature", 0.7);

      String jsonBody = mapper.writeValueAsString(requestBody);

      Request request = new Request.Builder()
          .url(baseUrl + "/v1/chat/completions")
          .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
          .build();

      try (okhttp3.Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful()) {
          throw new RuntimeException("vLLM API call failed: " + response.code());
        }

        Map<String, Object> responseMap = mapper.readValue(
            response.body().string(),
            Map.class
        );

        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");

        return Response.from(new AiMessage(content));
      }
    } catch (Exception e) {
      throw new RuntimeException("Error calling vLLM API", e);
    }
  }

  @Override
  public Response<AiMessage> generate(List<ChatMessage> messages, List<ToolSpecification> tools) {
    throw new UnsupportedOperationException("Tool support not implemented");
  }

  @Override
  public Response<AiMessage> generate(List<ChatMessage> messages, ToolSpecification tool) {
    throw new UnsupportedOperationException("Tool support not implemented");
  }
}