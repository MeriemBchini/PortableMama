package portablemama.app.component.traffic;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import portablemama.app.framework.Components;
import portablemama.app.framework.LLMService;
import portablemama.app.framework.OpenAIService;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TrafficService implements Components<List<Map<String, Object>>> {

    private final LLMService llmService;

    private static final String TRAFFIC_URL =
            "https://mobility.api.opendatahub.com/v2/flat/TrafficSensor?limit=25&offset=0&language=en";

    public TrafficService(OpenAIService llmService) {
        this.llmService = llmService;
    }

    @Override
    public String getComponentName() {
        return "traffic";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getFilteredData(Map<String, String> params) {

        String streetFilter = params.get("street"); // optional filter

        // Build URL
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(TRAFFIC_URL)
                .queryParam("limit", 25);

        URI uri = builder.build().toUri();

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);

        if (response == null || response.get("data") == null) {
            return List.of();
        }

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");

        // Apply street filter if provided
        if (streetFilter == null || streetFilter.isBlank()) {
            return data;
        }

        return data.stream()
                .filter(item ->
                        item.get("pname") != null &&
                        item.get("pname").toString().equalsIgnoreCase(streetFilter)
                )
                .collect(Collectors.toList());
    }

    @Override
    public String buildPrompt(Object data) {

        List<Map<String, Object>> list = (List<Map<String, Object>>) data;

        if (list.isEmpty()) {
            return "No traffic data available.";
        }

        // Build a table of lanes with their status
        String roadsData = list.stream()
                .map(item -> {
                    String pname = item.getOrDefault("pname", "Unknown street").toString();
                    String sname = item.getOrDefault("sname", "Unknown lane").toString();
                    boolean sActive = Boolean.TRUE.equals(item.get("sactive"));
                    boolean sAvailable = Boolean.TRUE.equals(item.get("savailable"));

                    // Mark lanes as TRAFFIC if blocked or unavailable
                    String status = (!sActive || !sAvailable) ? "TRAFFIC" : "OK";
                    return String.format("- %s / %s | Status: %s", pname, sname, status);
                })
                .collect(Collectors.joining("\n"));

        return """
                You are a traffic advisory AI assistant.

                Here is the current traffic data:
                %s

                Instructions:
                - Status = OK → lane is free, recommendable
                - Status = TRAFFIC → lane has traffic warning or congestion
                - Respond strictly in JSON format ONLY (no code fences, no extra text)
                - JSON format:
                {
                  "trafficWarnings": ["roads with TRAFFIC status"],
                  "recommendedRoutes": ["roads with OK status"]
                }
                """.formatted(roadsData);
    }

    @Override
    public String analyzeWithLLM(String prompt) {
        String rawResponse = llmService.generate(prompt);

        // Clean response in case LLM adds markdown/code fences
        String cleanResponse = rawResponse.replaceAll("(?s)```.*?\\n", "")
                                          .replace("```", "")
                                          .trim();

        return cleanResponse;
    }
}
