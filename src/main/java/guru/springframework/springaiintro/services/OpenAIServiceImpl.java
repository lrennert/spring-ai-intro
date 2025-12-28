package guru.springframework.springaiintro.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.springaiintro.model.Answer;
import guru.springframework.springaiintro.model.GetCapitalRequest;
import guru.springframework.springaiintro.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final ChatClient chatClient;

    @Value("classpath:templates/get-capital.st")
    private Resource getCapitalPrompt;

    @Value("classpath:templates/get-capital-with-info.st")
    private Resource getCapitalWithInfoPrompt;

    @Autowired
    ObjectMapper objectMapper;

    public OpenAIServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String getAnswer(String question) {
        return chatClient
                .prompt()
                .user(question)
                .call()
                .content();
    }

    @Override
    public Answer getAnswer(Question question) {
        System.out.println("getAnswer was called");

        String answer = chatClient
                .prompt()
                .user(question.question())
                .call()
                .content();

        return new Answer(answer);
    }

    @Override
    public Answer getCapital(GetCapitalRequest getCapitalRequest) {
        String content = chatClient
                .prompt()
                .user(u -> u
                        .text(getCapitalPrompt)
                        .param("stateOrCountry", getCapitalRequest.stateOrCountry())
                )
                .call()
                .content();

        // OpenAI response content (String, expected to contain JSON)
        System.out.println(content);

        String responseString;
        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            // Service response is a String extracted from JSON
            responseString = jsonNode.get("answer").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new Answer(responseString);
    }

    @Override
    public Answer getCapitalWithInfo(GetCapitalRequest getCapitalRequest) {
        String answer = chatClient
                .prompt()
                .user(u -> u
                        .text(getCapitalWithInfoPrompt)
                        .param("stateOrCountry", getCapitalRequest.stateOrCountry())
                )
                .call()
                .content();

        return new Answer(answer);
    }
}
