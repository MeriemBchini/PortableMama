package portablemama.app.component.traffic;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import portablemama.app.framework.Components;
import portablemama.app.framework.LLMService;
import portablemama.app.framework.OpenAIService;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TrafficService implements Components<List<Map<String, Object>>> {

	private final LLMService llmService;

//    private static final String TRAFFIC_URL =
//            "https://mobility.api.opendatahub.com/v2/flat/TrafficSensor?limit=25&offset=0&language=en&where=sactive.eq.true";

	private static final String TRAFFIC_URL = "https://tourism.opendatahub.com/v1/Announcement?pagenumber=1&rawsort=-StartTime&removenullvalues=false&getasidarray=false";

	public TrafficService(OpenAIService llmService) {
		this.llmService = llmService;
	}

	@Override
	public String getComponentName() {
		return "traffic";
	}

//    @Override
//    @SuppressWarnings("unchecked")
//    public List<Map<String, Object>> getFilteredData(Map<String, String> params) {
//
//        String streetFilter = params.get("street"); // optional filter
//
//        // Build URL
//        UriComponentsBuilder builder = UriComponentsBuilder
//                .fromUriString(TRAFFIC_URL)
//                .queryParam("limit", 25);
//
//        URI uri = builder.build().toUri();
//
//        RestTemplate restTemplate = new RestTemplate();
//        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
//
//        if (response == null || response.get("data") == null) {
//            return List.of();
//        }
//
//        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
//
//        // Apply street filter if provided
//        if (streetFilter == null || streetFilter.isBlank()) {
//            return data;
//        }
//
//        return data.stream()
//                .filter(item ->
//                        item.get("pname") != null &&
//                        item.get("pname").toString().equalsIgnoreCase(streetFilter)
//                )
//                .collect(Collectors.toList());
//    }

	@Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getFilteredData(Map<String, String> params) {

//		String currentLat = params.get("latitude");
//		String currentLong = params.get("longitude");

		// Build URL
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(TRAFFIC_URL).queryParam("limit", 25);

		URI uri = builder.build().toUri();

		RestTemplate restTemplate = new RestTemplate();
		Map<String, Object> response = restTemplate.getForObject(uri, Map.class);

		if (response == null || response.get("Items") == null) {
			return List.of();
		}

		List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("Items");

		return data.stream().map(item -> {
			Map<String, Object> obj = new HashMap<>();

			Map<String, Object> geo = (Map<String, Object>) item.get("Geo");
			Map<String, Object> position = (Map<String, Object>) geo.get("position");
			obj.put("latitude", position.get("Latitude"));
			obj.put("longitude", position.get("Longitude"));

			obj.put("active", item.get("Active"));
			obj.put("endTime", item.get("EndTime"));
			obj.put("startTime", item.get("StartTime"));

			Map<String, Object> detail = (Map<String, Object>) item.get("Detail");
			Map<String, Object> it = (Map<String, Object>) detail.get("it");
			obj.put("title", it.get("Title"));
			obj.put("content", it.get("BaseText"));

			return obj;
		}).collect(Collectors.toList());
	}

	@Override
	public String buildPrompt(Object data) {

		List<Map<String, Object>> list = (List<Map<String, Object>>) data;

		if (list.isEmpty()) {
			return "No traffic data available.";
		}
		
		System.out.println(list);
		ObjectMapper mapper = new ObjectMapper();
		try {
			return """
					You are a traffic risk analysis assistant.

					You will receive a JSON array of traffic and road announcements.
					Data: %s
					
					Your task:
					1. Keep only announcements that are **critical** for drivers.
					   Critical means: accidents, major road works, serious delays, closures, or dangerous weather.
					2. Sort the selected announcements by `startTime` in **descending order** (most recent first).
					3. For each selected item:
					   - Translate `title` into **English**
					   - Translate AND summarize `content` into **English** using at most **25 words**
					   - Keep only the most important facts: location, problem, impact.
					   - Add a new field `Category` which MUST be one of:
					     - "accident"
					     - "traffic jam"
					     - "bad weather"
					     - "roadwork"
					4. Keep all original fields:
					   latitude, longitude, active, startTime, endTime, title, content
					5. Output **ONLY valid JSON**, no explanation, no markdown.
					6. If no critical announcements exist, return an empty array [].
					
					Output format:
					[
					  {
					    "category": "...",
					    "latitude": ...,
					    "longitude": ...,
					    "active": ...,
					    "startTime": "...",
					    "endTime": "...",
					    "title": "...",
					    "content": "..."
					  }
					]

					""".formatted(mapper.writeValueAsString(list));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

//	@Override
//	public String buildPrompt(Object data) {
//
//		List<Map<String, Object>> list = (List<Map<String, Object>>) data;
//
//		if (list.isEmpty()) {
//			return "No traffic data available.";
//		}
//
//		// Build a table of lanes with their status
//		String roadsData = list.stream().map(item -> {
//			String pname = item.getOrDefault("pname", "Unknown street").toString();
//			String sname = item.getOrDefault("sname", "Unknown lane").toString();
//			boolean sActive = Boolean.TRUE.equals(item.get("sactive"));
//			boolean sAvailable = Boolean.TRUE.equals(item.get("savailable"));
//
//			// Mark lanes as TRAFFIC if blocked or unavailable
//			String status = (!sActive || !sAvailable) ? "TRAFFIC" : "OK";
//			return String.format("- %s / %s | Status: %s", pname, sname, status);
//		}).collect(Collectors.joining("\n"));
//
//		return """
//				You are a traffic advisory AI assistant.
//
//				Here is the current traffic data:
//				%s
//
//				Instructions:
//				- Status = OK → lane is free, recommendable
//				- Status = TRAFFIC → lane has traffic warning or congestion
//				- Respond strictly in JSON format ONLY (no code fences, no extra text)
//				- JSON format:
//				{
//				  "trafficWarnings": ["roads with TRAFFIC status, translate to English"],
//				  "recommendedRoutes": ["roads with OK status, translate to English"]
//				}
//				""".formatted(roadsData);
//	}

	@Override
	public String analyzeWithLLM(String prompt) {
		String rawResponse = llmService.generate(prompt);

		// Clean response in case LLM adds markdown/code fences
		String cleanResponse = rawResponse.replaceAll("(?s)```.*?\\n", "").replace("```", "").trim();

		System.out.println("aiResponse:" + cleanResponse);

		return cleanResponse;
	}
}
