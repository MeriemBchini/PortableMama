/*package portablemama.app.component.weather;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WeatherController {

    private final WeatherService weatherService;
    private final WeatherLLM ollamaService;

    public WeatherController(WeatherService weatherService, WeatherLLM ollamaService) {
        this.weatherService = weatherService;
        this.ollamaService = ollamaService;
    }

    @GetMapping("/api/weather/ai")
    public Map<String, Object> getWeatherWithAI(
            @RequestParam String name,
            @RequestParam String date
    ) {

        List<Map<String, Object>> filtered =
                weatherService.getFilteredWeather(name, date);

        if (filtered.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "No weather data found for given location and date");
            return error;
        }

        Map<String, Object> weather = filtered.get(0);

        String prompt = """
You are a weather risk advisory assistant.

Analyze the weather and provide:
1. A short summary
2. Travel recommendations
3. Alerts or risks if any (snow, ice, wind, low visibility, cold)

Weather data:
Location: %s
Date: %s
Temperature: %s °C
Wind speed: %s m/s (max %s)
Humidity: %s%%
Pressure: %s hPa
Visibility: %s km
""".formatted(
                weather.get("name"),
                weather.get("lastUpdated"),
                weather.get("t"),
                weather.get("ff"),
                weather.get("wMax"),
                weather.get("rh"),
                weather.get("p"),
                weather.get("visibility")
        );

        String aiResponse = ollamaService.analyzeWeather(prompt);

        // Optional rule-based alert
        boolean alert =
                Double.parseDouble(weather.get("t").toString()) < 0
                        || Double.parseDouble(weather.get("wMax").toString()) > 10;

        Map<String, Object> result = new HashMap<>();
        result.put("weatherData", weather);
        result.put("aiAnalysis", aiResponse);
        result.put("alert", alert ? "YES" : "NO");

        return result;
    }
}*/