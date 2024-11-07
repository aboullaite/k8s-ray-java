package me.aboullaite;

<<<<<<< Updated upstream
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record VLLMResponse(
    @JsonProperty("text")
    List<String> text) { }
=======
import java.util.List;

class VLLMResponse {

  public List<Choice> choices;

  private static class Choice {

    public Choice.Message message;

    private static class Message {

      public String content;
    }
  }
}
>>>>>>> Stashed changes
