package portablemama.app.component.ski;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import portablemama.app.framework.Components;
import portablemama.app.framework.JSONUtils;
import portablemama.app.framework.LLMService;
import portablemama.app.framework.	OpenAIService;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SkiService implements Components<List<Map<String, Object>>> {

	private final LLMService llmService;
	private final String SKI_AREA_URL = "https://tourism.opendatahub.com/v1/SkiArea";
	private final String SKI_MEASURING_URL = "https://tourism.opendatahub.com/v1/Weather/Measuringpoint";

    public SkiService(OpenAIService llmService) {
		this.llmService = llmService;
	}

	@Override
	public String getComponentName() {
		return "ski";
	}

	@Override
	public List<Map<String, Object>> getFilteredData(Map<String, String> params) {
//        String region = params.get("region");
		params.put("pagenumber", "1");
		params.put("pagesize", "3");
		params.put("removenullvalues", "false");
		params.put("getasidarray", "false");

		// Build URI with query parameters
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(SKI_AREA_URL);
		params.forEach(builder::queryParam);
		URI uri = builder.build().encode().toUri();

		RestTemplate restTemplate = new RestTemplate();
		Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
		if (response == null || response.get("Items") == null)
			return List.of();

		List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("Items");

		// TODO: for each ski area, call API to measure snow statement
		for (Map<String, Object> item : items) {
			params.clear();
			params.put("latitude", String.valueOf(item.get("Latitude")));
			params.put("longitude", String.valueOf(item.get("Longitude")));
			params.put("pagenumber", "1");
			params.put("pagesize", "1");
			params.put("removenullvalues", "false");
			params.put("getasidarray", "false");
			
			builder = UriComponentsBuilder.fromUriString(SKI_MEASURING_URL);
			params.forEach(builder::queryParam);
			uri = builder.build().encode().toUri();
			response = restTemplate.getForObject(uri, Map.class);
			if (response == null || response.get("Items") == null)
				continue;
			
			List<Map<String, Object>> snowItems = (List<Map<String, Object>>) response.get("Items");
			item.put("snowHeight", snowItems.get(0).get("SnowHeight"));
			item.put("temperature", snowItems.get(0).get("Temperature"));
			item.put("lastSnowDate", snowItems.get(0).get("LastSnowDate"));
			item.put("newSnowHeight", snowItems.get(0).get("newSnowHeight"));
		}

		return items.stream().map(item -> {
			Map<String, Object> obj = new HashMap<>();
			obj.put("id", item.get("Id"));

			Map<String, Object> detail = (Map<String, Object>) item.get("Detail");
			Map<String, Object> enDetail = (Map<String, Object>) detail.get("en");
			obj.put("fullDescription", enDetail.get("BaseText"));
			obj.put("title", enDetail.get("Title"));

			obj.put("active", item.get("Active"));
			obj.put("latitude", item.get("Latitude"));
			obj.put("longitude", item.get("Longitude"));
			obj.put("liftCount", item.get("LiftCount"));
			obj.put("slopeKmBlue", item.get("SlopeKmBlue"));
			obj.put("slopeKmRed", item.get("SlopeKmRed"));
			obj.put("slopeKmBlack", item.get("SlopeKmBlack"));
			obj.put("totalSlopeKm", item.get("TotalSlopeKm"));

			List<Map<String, Object>> schedule = (List<Map<String, Object>>) item.get("OperationSchedule");
			obj.put("startDay", schedule.getFirst().get("Start"));
			obj.put("endDay", schedule.getFirst().get("Stop"));

			Map<String, Object> contact = (Map<String, Object>) item.get("ContactInfos");
			Map<String, Object> enContact = (Map<String, Object>) contact.get("en");
			obj.put("logoUrl", enContact.get("LogoUrl"));
			obj.put("city", enContact.get("City"));
			obj.put("country", enContact.get("CountryName"));
			obj.put("phone", enContact.get("Phonenumber"));
			obj.put("url", enContact.get("Url"));

			obj.put("snowHeight", item.get("snowHeight"));
			obj.put("newSnowHeight", item.get("newSnowHeight"));
			obj.put("temperature", item.get("temperature"));
			obj.put("lastSnowDate", item.get("lastSnowDate"));

			List<Map<String, Object>> images = (List<Map<String, Object>>) item.get("ImageGallery");
			obj.put("image", images.getFirst().get("ImageUrl"));

			return obj;
		}).collect(Collectors.toList());
	}

	@Override
	public String buildPrompt(Object data) {
		List<Map<String, Object>> list = (List<Map<String, Object>>) data;
		if (list.isEmpty())
			return "No ski data available.";

		Map<String, Object> skiData = list.get(0);

		String regionName = (String) JSONUtils.getNested(skiData, "LocationInfo", "RegionInfo", "Name", "en");
		String locationName = skiData.getOrDefault("Shortname", "Unknown Location").toString();
		String altitude = JSONUtils.getNested(skiData, "GpsPoints", "position", "Altitude") != null
				? JSONUtils.getNested(skiData, "GpsPoints", "position", "Altitude").toString()
				: "N/A";
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
				""".formatted(regionName != null ? regionName : "Unknown Region", locationName, altitude, snowHeight,
				newSnow, lastSnowDate);
	}

	@Override
	public String analyzeWithLLM(String prompt) {
		return llmService.generate(prompt);
	}
}
