package portablemama.app.framework;



public interface LLMService {
    /**
     * Generate AI response from prompt
     */
    String generate(String prompt);
}
