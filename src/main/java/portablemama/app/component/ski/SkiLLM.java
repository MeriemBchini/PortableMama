/*package portablemama.app.component.ski;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SkiLLM {

    private final String OLLAMA_URL = "http://localhost:11434/api/generate";

    public String getRecommendation(String prompt) {

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama3");
        body.put("prompt", prompt);
        body.put("stream", false);

        Map response = restTemplate.postForObject(
                OLLAMA_URL,
                body,
                Map.class
        );

        return response.get("response").toString();
    }
}
*/