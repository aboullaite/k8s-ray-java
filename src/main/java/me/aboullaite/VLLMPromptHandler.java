package me.aboullaite;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VLLMPromptHandler {
  private final String baseUrl;
  private final OkHttpClient client;
  private final ObjectMapper mapper;

  public VLLMPromptHandler(String baseUrl) {
    this.baseUrl = baseUrl;
    this.client = new OkHttpClient();
    this.mapper = new ObjectMapper();
  }

  public String complete(String prompt) {
    try {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("prompt", prompt);
      requestBody.put("max_tokens", 512);
      requestBody.put("temperature", 0.7);

      String jsonBody = mapper.writeValueAsString(requestBody);

      Request request = new Request.Builder()
          .url(baseUrl)
          .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
          .build();

      try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful()) {
          throw new RuntimeException("vLLM API call failed: " + response.code());
        }

        Map<String, Object> responseMap = mapper.readValue(
            response.body().string(),
            Map.class
        );

        return responseMap.get("text").toString();
      }
    } catch (Exception e) {
      throw new RuntimeException("Error calling vLLM API", e);
    }
  }
}
