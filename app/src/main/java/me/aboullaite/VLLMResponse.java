package me.aboullaite;

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
