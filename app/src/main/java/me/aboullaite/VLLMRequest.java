package me.aboullaite;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VLLMRequest(
    @JsonProperty("prompt")
    String prompt,
    @JsonProperty("max_tokens")
    int maxTokens,
    @JsonProperty("temperature")
    double temperature) { }
