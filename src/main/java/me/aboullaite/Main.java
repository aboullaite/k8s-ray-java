package me.aboullaite;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.Response;
import java.util.ArrayList;
import java.util.List;

public class Main {

  public static void main(String[] args) {
    /**
     * Calling Prompt Handler
     */

    VLLMPromptHandler vllm = new VLLMPromptHandler("http://localhost:8000/");

    String prompt = "Who is Abdelfettah Sghiouar";
    String response = vllm.complete(prompt);

    System.out.println("Prompt: " + prompt);
    System.out.println("Response: " + response);


    /**
     * Calling Chat Model
     */

    // Initialize the model with your vLLM server URL
//    VLLMChatModel model = new VLLMChatModel("http://localhost:8000");
//
//    // Create a conversation
//    List<ChatMessage> messages = new ArrayList<>();
//    messages.add(new UserMessage("Tell me a joke about programming."));
//
//    // Generate response
//    Response<AiMessage> response = model.generate(messages);
//    System.out.println("AI: " + response.content().text());
  }}