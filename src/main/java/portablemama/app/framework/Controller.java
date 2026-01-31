package portablemama.app.framework;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Controller {

    private final Map<String, Components<?>> components = new HashMap<>();

    public Controller(List<Components<?>> componentList) {
        for (Components<?> c : componentList) {
            components.put(c.getComponentName().toLowerCase(), c);
        }
    }

    @GetMapping("/api/ai")
    public Map<String, Object> getAIAdvice(
            @RequestParam String component,
            @RequestParam Map<String, String> params
    ) {
        Components<?> aiComponent = components.get(component.toLowerCase());
        if (aiComponent == null) {
            return Map.of("message", "Component not found: " + component);
        }

        Object data = aiComponent.getFilteredData(params);
        String prompt = aiComponent.buildPrompt(data);
        String aiResponse = aiComponent.analyzeWithLLM(prompt);

        return Map.of(
                "data", data,
                "aiAnalysis", aiResponse
        );
    }
}
