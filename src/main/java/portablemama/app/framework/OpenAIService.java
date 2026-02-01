package portablemama.app.framework;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;

import java.util.HashMap;
import java.util.Map;

@Service
public class OpenAIService implements LLMService {

    private static String OPENAI_API_KEY = "<<open-api-key>>";
    
    @Override
    public String generate(String prompt) {
        try {
        	ChatModel model = OpenAiChatModel.builder()
    				.temperature(0.8)
    				.apiKey(OPENAI_API_KEY)
    				.modelName(OpenAiChatModelName.GPT_4_O_MINI)
    				.build();
//        	ChatMemory memory = MessageWindowChatMemory.withMaxMessages(20);
//        	memory.add(SystemMessage.from(prompt));
        	ConversationalChain cc = ConversationalChain
    				.builder()
    				.chatModel(model)
//    				.chatMemory(memory)
    				.build();
        	
        	String aiResponse = cc.execute(prompt);
        	return aiResponse;
        } catch (Exception e) {
            return "Error calling LLM: " + e.getMessage();
        }
    }
}
