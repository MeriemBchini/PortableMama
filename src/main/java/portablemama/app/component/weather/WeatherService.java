package portablemama.app.component.weather;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import portablemama.app.framework.Components;
import portablemama.app.framework.JSONUtils;
import portablemama.app.framework.LLMService;
import portablemama.app.framework.OpenAIService;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeatherService implements Components<List<Map<String, Object>>> {

    private final LLMService llmService;
    private final String WEATHER_URL =
            "https://tourism.opendatahub.com/v1/Weather/Realtime";

    public WeatherService(OpenAIService llmService) {
        this.llmService = llmService;
    }

    @Override
    public String getComponentName() {
        return "weather";
    }

    @Override
    public List<Map<String, Object>> getFilteredData(Map<String, String> params) {
//        String name = params.get("name");
        String date = params.get("date");

     // Build URI with query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(WEATHER_URL);
        params.forEach(builder::queryParam);
        URI uri = builder.build().encode().toUri();
        
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
        if (response == null || response.get("Items") == null) return List.of();

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("Items");

        return items.stream()
                .filter(item ->
                        item.get("name") != null && item.get("lastUpdated") != null
//                        item.get("name").toString().equalsIgnoreCase(name) &&
                        && item.get("lastUpdated").toString().startsWith(date)
                )
                .collect(Collectors.toList());
    }

    @Override
    public String buildPrompt(Object data) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) data;
        if (list.isEmpty()) return "No weather data available.";

        Map<String, Object> weather = list.get(0);

        return """
                You are a weather advisory assistant.
                Location: %s
                Date: %s
                Temperature: %s °C
                Wind speed: %s m/s (max %s)
                Humidity: %s%%
                Pressure: %s hPa
                Visibility: %s km
                """.formatted(
                weather.getOrDefault("name", "Unknown"),
                weather.getOrDefault("lastUpdated", "N/A"),
                weather.getOrDefault("t", "N/A"),
                weather.getOrDefault("ff", "N/A"),
                weather.getOrDefault("wMax", "N/A"),
                weather.getOrDefault("rh", "N/A"),
                weather.getOrDefault("p", "N/A"),
                weather.getOrDefault("visibility", "N/A")
        );
    }

    @Override
    public String analyzeWithLLM(String prompt) {
        return llmService.generate(prompt);
    }
}
