package me.aboullaite;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record VLLMResponse(
    @JsonProperty("text")
    List<String> text) { }
