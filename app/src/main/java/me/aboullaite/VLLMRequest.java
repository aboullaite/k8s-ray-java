package me.aboullaite;

import java.util.List;

class VLLMRequest {

  public List<Object> messages;
  public String model = "mistral-7b-instruct";  // Default model
  public double temperature = 0.7;

  public VLLMRequest(List<Object> messages) {
    this.messages = messages;
  }
}
