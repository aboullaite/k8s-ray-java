package me.aboullaite;

// Data classes for vLLM API
class VLLMMessage {

  public String role;
  public String content;

  public VLLMMessage(String role, String content) {
    this.role = role;
    this.content = content;
  }
}
