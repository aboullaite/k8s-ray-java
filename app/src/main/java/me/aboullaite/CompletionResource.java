package me.aboullaite;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import java.util.HashMap;

@Path("/api/completion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompletionResource {

  @Inject
  VLLMPromptHandler promptHandler;

  @POST
  public Map<String, String> complete(Map<String, String> request) {
    Map<String, String> response = new HashMap<>();
    try {
      String result = promptHandler.complete(request.get("prompt"));
      response.put("completion", result);
    } catch (Exception e) {
      response.put("error", e.getMessage());
    }
    return response;
  }
}
