package portablemama.app.component.ski;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;
import portablemama.app.framework.Components;
import portablemama.app.framework.JSONUtils;
import portablemama.app.framework.LLMService;
import portablemama.app.framework.	OpenAIService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SkiService implements Components<List<Map<String, Object>>> {

    private final LLMService llmService;
    private final String SKI_URL = "https://tourism.opendatahub.com/v1/Weather/Measuringpoint";

    public SkiService(OpenAIService llmService) {
        this.llmService = llmService;
    }

    @Override
    public String getComponentName() {
        return "ski";
    }

    @Override
    public List<Map<String, Object>> getFilteredData(Map<String, String> params) {
        String region = params.get("region");
        RestTemplate restTemplate = new RestTemplate();
        List<Map<String, Object>> response = restTemplate.getForObject(SKI_URL, List.class);
        if (response == null) return List.of();

        return response.stream()
                .filter(item -> {
                    Object regionName = JSONUtils.getNested(item, "LocationInfo", "RegionInfo", "Name", "en");
                    return regionName != null && regionName.toString().equalsIgnoreCase(region);
                })
                .collect(Collectors.toList());
    }

    @Override
    public String buildPrompt(Object data) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) data;
        if (list.isEmpty()) return "No ski data available.";

        Map<String, Object> skiData = list.get(0);

        String regionName = (String) JSONUtils.getNested(skiData, "LocationInfo", "RegionInfo", "Name", "en");
        String locationName = skiData.getOrDefault("Shortname", "Unknown Location").toString();
        String altitude = JSONUtils.getNested(skiData, "GpsPoints", "position", "Altitude") != null ?
                JSONUtils.getNested(skiData, "GpsPoints", "position", "Altitude").toString() : "N/A";
        String snowHeight = skiData.getOrDefault("SnowHeight", "0").toString();
        String newSnow = skiData.getOrDefault("newSnowHeight", "0").toString();
        String lastSnowDate = skiData.getOrDefault("LastSnowDate", "N/A").toString();

        return """
                You are a ski advisory assistant.
                Region: %s
                Location: %s
                Altitude: %s m
                Snow Height: %s cm
                New Snow (last 24h): %s cm
                Last Snowfall: %s
                """.formatted(
                regionName != null ? regionName : "Unknown Region",
                locationName,
                altitude,
                snowHeight,
                newSnow,
                lastSnowDate
        );
    }

    @Override
    public String analyzeWithLLM(String prompt) {
        return llmService.generate(prompt);
    }
}
