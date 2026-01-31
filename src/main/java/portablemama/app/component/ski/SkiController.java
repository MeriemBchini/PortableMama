/*package portablemama.app.component.ski;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SkiController {

    private final SkiService skiService;
    private final SkiLLM llmService;

    public SkiController(SkiService skiService, SkiLLM llmService) {
        this.skiService = skiService;
        this.llmService = llmService;
    }

    @GetMapping("/api/ski/ai")
    public Map<String, Object> getSkiAdvice(@RequestParam String region) {

        List<Map<String, Object>> filtered = skiService.getFilteredSkiData(region);

        if (filtered.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "No ski data found for region: " + region);
            return error;
        }

        Map<String, Object> skiData = filtered.get(0);

        // Safe extraction of nested fields
        Map<String, Object> gpsPoints = skiData.get("GpsPoints") instanceof Map ? (Map<String,Object>)skiData.get("GpsPoints") : Map.of();
        Map<String,Object> position = gpsPoints.get("position") instanceof Map ? (Map<String,Object>)gpsPoints.get("position") : Map.of();
        Map<String,Object> locationInfo = skiData.get("LocationInfo") instanceof Map ? (Map<String,Object>)skiData.get("LocationInfo") : Map.of();
        Map<String,Object> regionInfo = locationInfo.get("RegionInfo") instanceof Map ? (Map<String,Object>)locationInfo.get("RegionInfo") : Map.of();
        Map<String,Object> regionNameMap = regionInfo.get("Name") instanceof Map ? (Map<String,Object>)regionInfo.get("Name") : Map.of();

        String regionName = regionNameMap.get("en") != null ? regionNameMap.get("en").toString() : "Unknown Region";
        String altitude = position.get("Altitude") != null ? position.get("Altitude").toString() : "N/A";
        String snowHeight = skiData.get("SnowHeight") != null ? skiData.get("SnowHeight").toString() : "0";
        String newSnow = skiData.get("newSnowHeight") != null ? skiData.get("newSnowHeight").toString() : "0";
        String lastSnowDate = skiData.get("LastSnowDate") != null ? skiData.get("LastSnowDate").toString() : "N/A";
        String locationName = skiData.get("Shortname") != null ? skiData.get("Shortname").toString() : "Unknown Location";

        // Build LLM prompt
        String prompt = """
You are a ski advisory assistant.

Analyze the ski area data and provide:
1. A short summary of conditions
2. Skiing recommendations
3. Alerts or risks (snow depth, new snow, altitude, cold conditions)

Ski Data:
Region: %s
Location: %s
Altitude: %s m
Snow Height: %s cm
New Snow (last 24h): %s cm
Last Snowfall Date: %s
""".formatted(regionName, locationName, altitude, snowHeight, newSnow, lastSnowDate);

        String aiResponse = llmService.getRecommendation(prompt);

        // Optional simple alert
        boolean alert = Integer.parseInt(snowHeight) < 20;

        Map<String, Object> result = new HashMap<>();
        result.put("skiData", skiData);
        result.put("aiAnalysis", aiResponse);
        result.put("alert", alert ? "YES" : "NO");

        return result;
    }
}*/
