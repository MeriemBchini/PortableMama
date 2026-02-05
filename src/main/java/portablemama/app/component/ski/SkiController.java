package portablemama.app.component.ski;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import portablemama.app.framework.OpenAIService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SkiController {

	private final SkiService skiService;
	private final OpenAIService llmService;

	public SkiController(SkiService skiService, OpenAIService llmService) {
		this.skiService = skiService;
		this.llmService = llmService;
	}

	@GetMapping("/api/ski/ai")
	public Map<String, Object> getSkiAdvice(@RequestParam double latitude, @RequestParam double longitude)
			throws JsonProcessingException {

		Map<String, String> params = new HashMap<>();
		params.put("latitude", String.valueOf(latitude));
		params.put("longitude", String.valueOf(longitude));

		List<Map<String, Object>> filtered = skiService.getFilteredData(params);

		if (filtered.isEmpty()) {
			return Map.of("data", "", "aiAnalysis", "", "error", "No ski area found for given location");
		}

//		Map<String, Object> skiData = filtered.get(0);

		// Build LLM prompt
		ObjectMapper mapper = new ObjectMapper();
		String prompt = """
				You are a ski advisory assistant. I give you my Ski Data: %s.
				You can use this data and external knowledge to enhance the information.
				
				Task: For each ski area, output ONLY valid JSON corresponding to the following structure:
				[
				  {
				    "areaDescription": "<<short description of the area, max 200 characters>>",
				    "listGuestReview": [
				      {
				        "reviewer": "<<reviewer's name>>",
				        "content": "<<review content>>",
				        "rating": "<<rating of the review>>"
				      }
				    ],
				    "warning": {
				    	"skiing": "<<warning and suggestion for skiing conditions>>",
				    	"safety": "<<warning and suggestion for cautious of potential avalanche risks>>",
				    	"equipment": "<<warning and suggestion for ski equipments>>",
				    	"weatherForecast": "<<warning and suggestion for weather forecast for any additional snowfall or changes in conditions>>"",
				    },
				    "recommendDes": "<<recommendation for visiting this ski area>>",
				    "priceRange": "<<average price range per person, format: number-number currency>>"
				  }
				]
				
				Rules:
				1. Output must be valid JSON ONLY. Do NOT include explanations, comments, or extra characters.
				2. `listGuestReview` should include the top 3 most recent real users’ reviews (if available).
				3. `areaDescription` should be concise (max 200 characters).
				4. `recommendDes` must contain exactly 2 sentences.
				5. `priceRange` must include only numbers and currency, e.g., "30-50 EUR".
				6.`warning`'s subproperties must have 1 sentence for warning and 1 sentence for suggestion and they should be very on detail
				7. Do not change field names. Keep them exactly as specified.
				8. Do not add any additional fields or metadata.
				9. Do not add '```json'

				"""
				.formatted(mapper.writeValueAsString(filtered));
//
		String aiResponse = llmService.generate(prompt);
		System.out.print(aiResponse);

		List<Map<String, Object>> aiResponseMap = null;
		try {
			aiResponseMap = mapper.readValue(aiResponse, new TypeReference<List<Map<String, Object>>>() {
			});
		} catch (JsonMappingException e) {
			System.out.print("aiResponse: \n" + aiResponse);
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			System.out.print("aiResponse: \n" + aiResponse);
			e.printStackTrace();
		}

		if (aiResponseMap != null) {
			for (int i = 0; i < filtered.size(); i++) {
				filtered.get(i).put("aiAnalysis", aiResponseMap.get(i));
			}
		}

		// Optional simple alert
//		boolean alert = Integer.parseInt(snowHeight) < 20;

		Map<String, Object> result = new HashMap<>();
		result.put("data", filtered);
//		result.put("aiAnalysis", aiResponseMap);
//		result.put("alert", alert ? "YES" : "NO");

		return result;
	}
}
