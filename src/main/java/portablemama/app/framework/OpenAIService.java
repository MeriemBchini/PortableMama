package portablemama.app.framework;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;

@Service
public class OpenAIService implements LLMService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Override
    public String generate(String prompt) {
        try {
            // Build the Chat model
            ChatModel model = OpenAiChatModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                    .temperature(0.7)
                    .build();

            // Build the conversational chain (no timeoutSeconds in 1.4.0)
            ConversationalChain chain = ConversationalChain.builder()
                    .chatModel(model)
                    .build();

            // Execute the prompt directly (expects a String)
            return chain.execute(prompt);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling OpenAI: " + e.getMessage();
        }
    }
}
