package me.aboullaite;

<<<<<<< Updated upstream
import com.fasterxml.jackson.annotation.JsonProperty;

public record VLLMRequest(
    @JsonProperty("prompt")
    String prompt,
    @JsonProperty("max_tokens")
    int maxTokens,
    @JsonProperty("temperature")
    double temperature) { }
=======
import java.util.List;

class VLLMRequest {

  public List<Object> messages;
  public String model = "mistral-7b-instruct";  // Default model
  public double temperature = 0.7;

  public VLLMRequest(List<Object> messages) {
    this.messages = messages;
  }
}
>>>>>>> Stashed changes
