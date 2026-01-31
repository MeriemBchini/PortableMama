package portablemama.app.service;

import portablemama.app.component.ski.SkiService;
import portablemama.app.component.weather.WeatherService;
import portablemama.app.framework.LLMService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AdviceService {

    private final SkiService skiService;
    private final WeatherService weatherService;
    private final LLMService llmService;

    public AdviceService(
            SkiService skiService,
            WeatherService weatherService,
            LLMService llmService) {
        this.skiService = skiService;
        this.weatherService = weatherService;
        this.llmService = llmService;
    }

    public String generateAdvice(
            String date,
            String location,
            String activity,
            String transport) {

        Map<String, String> params = Map.of(
                "date", date,
                "location", location
        );

        String skiPrompt = activity.equalsIgnoreCase("ski")
                ? skiService.buildPrompt(skiService.getFilteredData(params))
                : "";

        String weatherPrompt =
                weatherService.buildPrompt(weatherService.getFilteredData(params));

        String finalPrompt = """
                You are an outdoor safety and travel assistant.

                User plan:
                Date: %s
                Location: %s
                Activity: %s
                Transport: %s

                %s

                %s

                Give:
                - Safety alerts
                - Travel recommendations
                - Equipment suggestions
                - What can go wrong
                """
                .formatted(
                        date, location, activity, transport,
                        skiPrompt, weatherPrompt
                );

        return llmService.generate(finalPrompt);
    }
}
