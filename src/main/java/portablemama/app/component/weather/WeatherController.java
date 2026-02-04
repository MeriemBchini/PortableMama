package portablemama.app.component.weather;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import portablemama.app.framework.OllamaLLMService;
import portablemama.app.framework.OpenAIService;

import java.awt.print.Printable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WeatherController {

	private final WeatherService weatherService;
	private final OllamaLLMService ollamaService;
	private final OpenAIService openAIService;

	public WeatherController(WeatherService weatherService, OllamaLLMService ollamaService,
			OpenAIService openAIService) {
		this.weatherService = weatherService;
		this.ollamaService = ollamaService;
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
			return Map.of("data", "", "aiAnalysis", "", "error",
					"No weather data found for given location and date");
		}

		Map<String, Object> currentPosition = filtered.get(0);

		String prompt = """
				You are a weather risk advisory assistant.

				I give you my Weather data:
				- Location: %s
				- Date: %s
				- Latitude: %s
				- Longitude: %s
				- Temperature: %s °C
				- Wind speed: %s m/s (max %s)
				- Humidity: %s%%
				- Pressure: %s hPa
				- Visibility: %s km
				- Flow rate: %s m³/s
				- Wind gust: %s
				- Sunshine duration: %s h
				
				You can use this data and external knowledge to enhance the information.
				
				Task: Analyze the weather and respond ONLY in the following JSON format:
				
				{
				  "shortDes": "<<two words describing the weather>>",
				  "accessoryRec": "<<a sentence for accessory recommendations when going out>>",
				  "travelRec": "<<2 sentences for clothing recommendations>>",
				  "alerts": "<<1 sentences for weather alerts or risks (snow, ice, wind, low visibility, cold)>>",
				  "forcast:" "<<2 sentences for weather forcase today>>"
				}
				
				Rules:
				1. Output must be valid JSON only. Do NOT include explanations or extra text.
				2. Adhere strictly to word limits for each field.
				3. Do not add any extra fields or metadata.
				4. Keep field names exactly as specified.

				""".formatted(currentPosition.get("name"), currentPosition.get("lastUpdated"), latitude, longitude,
				currentPosition.get("t"), currentPosition.get("ff"), currentPosition.get("wMax"),
				currentPosition.get("rh"), currentPosition.get("p"), currentPosition.get("visibility"),
				currentPosition.get("q"), currentPosition.get("wMax"), currentPosition.get("sd"));

		String aiResponse = openAIService.generate(prompt);
		System.out.print(aiResponse);
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