package portablemama.app.framework;

import java.util.Map;

public interface Components<T> {

    /**
     * Return a unique component name used in the controller (e.g., "ski", "weather")
     */
    String getComponentName();

    /**
     * Fetch and filter the data for the component
     */
    T getFilteredData(Map<String, String> params);

    /**
     * Build prompt for the AI model
     */
    String buildPrompt(Object data);

    /**
     * Analyze the prompt with the LLM
     */
    String analyzeWithLLM(String prompt);
}
