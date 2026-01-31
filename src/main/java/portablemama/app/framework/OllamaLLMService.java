package portablemama.app.framework;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaLLMService implements LLMService {

    private final String OLLAMA_URL = "http://localhost:11434/api/generate";

    @Override
    public String generate(String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama3");
            body.put("prompt", prompt);
            body.put("stream", false);

            Map response = restTemplate.postForObject(OLLAMA_URL, body, Map.class);
            Object res = response != null ? response.get("response") : null;
            return res != null ? res.toString() : "LLM returned no response";
        } catch (Exception e) {
            return "Error calling LLM: " + e.getMessage();
        }
    }
}
