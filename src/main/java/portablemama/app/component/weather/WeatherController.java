package portablemama.app.component.weather;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import portablemama.app.framework.OpenAIService;
import portablemama.app.framework.OpenAIService;

import java.awt.print.Printable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WeatherController {

	private final WeatherService weatherService;
	private final OpenAIService openAIService;

	public WeatherController(WeatherService weatherService, 
			OpenAIService openAIService) {
		this.weatherService = weatherService;
		this.openAIService = openAIService;
	}

	@GetMapping("/api/weather/ai")
	public Map<String, Object> getWeatherWithAI(@RequestParam double latitude, @RequestParam double longitude) {

		Map<String, String> params = new HashMap<>();
		params.put("latitude", String.valueOf(latitude));
		params.put("longitude", String.valueOf(longitude));

		params.put("date", String.valueOf(LocalDate.now()));
		params.put("pagenumber", "1");
		params.put("pagesize", "1");
		params.put("language", "en");
		List<Map<String, Object>> filtered = weatherService.getFilteredData(params);

		if (filtered.isEmpty()) {
			return Map.of("data", null, "aiAnalysis", null, "error",
					"No weather data found for given location and date");
		}

		Map<String, Object> currentPosition = filtered.get(0);

		String prompt = """
				You are a weather risk advisory assistant.

				This is the weather data:
				Location: %s,
				Date: %s,
				Latitude: %s,
				Longitude: %s,
				Temperature: %s °C,
				Wind speed: %s m/s (max %s),
				Humidity: %s%%,
				Pressure: %s hPa,
				Visibility: %s km,
				Flow rate: %s (m³/s),
				Wind gust: %s,
				Sunshine duration: %s (h)

				Let's analyze the weather and response with data EXACTLY by JSON format(don't response anything else):
				{
					"shortDes": "<<2 words describe the weather on the day>>",
					"accessoryRec": "<<10 words maximum for recommendation of accessories if going out>>",
					"travelRec": "<<20 words maximum for recommendation of clothes if going out>>",
					"alerts": "<<10 words maximum for alerts or risks if any (snow, ice, wind, low visibility, cold)>>"
				}
				""".formatted(currentPosition.get("name"), currentPosition.get("lastUpdated"), latitude, longitude,
				currentPosition.get("t"), currentPosition.get("ff"), currentPosition.get("wMax"),
				currentPosition.get("rh"), currentPosition.get("p"), currentPosition.get("visibility"),
				currentPosition.get("q"), currentPosition.get("wMax"), currentPosition.get("sd"));

		String aiResponse = openAIService.generate(prompt);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> aiResponseMap = new HashMap<String, String>();
		try {
			aiResponseMap = mapper.readValue(aiResponse, new TypeReference<>() {
			});
		} catch (JsonMappingException e) {
			System.out.print("aiResponse: \n" + aiResponse);
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			System.out.print("aiResponse: \n" + aiResponse);
			e.printStackTrace();
		}
//		String aiResponse = "";
		// Optional rule-based alert
		boolean alert = Double.parseDouble(currentPosition.get("t").toString()) < 0
				|| Double.parseDouble(currentPosition.get("wMax").toString()) > 10;

		Map<String, Object> result = new HashMap<>();
		result.put("data", currentPosition);
		result.put("aiAnalysis", aiResponseMap);
		result.put("alert", alert ? "YES" : "NO");

		return result;
//				Map.of("data", filtered, "aiAnalysis", aiResponse);
	}
}