package me.aboullaite;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class VLLMPromptHandler {
  private final OkHttpClient client;
  private final ObjectMapper mapper;
  private final String baseUrl;

  public VLLMPromptHandler(@ConfigProperty(name = "ray.base-url") String baseUrl) {
    this.client = new OkHttpClient();
    this.mapper = new ObjectMapper();
    this.baseUrl = baseUrl + "/generate";
  }

  public String complete(String prompt) throws Exception {
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
  }
}