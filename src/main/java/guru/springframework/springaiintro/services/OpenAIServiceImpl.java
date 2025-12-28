package guru.springframework.springaiintro.services;

import guru.springframework.springaiintro.model.Answer;
import guru.springframework.springaiintro.model.GetCapitalRequest;
import guru.springframework.springaiintro.model.GetCapitalResponse;
import guru.springframework.springaiintro.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
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
    public GetCapitalResponse getCapital(GetCapitalRequest getCapitalRequest) {
        BeanOutputConverter<GetCapitalResponse> converter =
                new BeanOutputConverter<>(GetCapitalResponse.class);

        return chatClient
                .prompt()
                .user(u -> u
                        .text(getCapitalPrompt)
                        .param("stateOrCountry", getCapitalRequest.stateOrCountry())
                        .param("format", converter.getFormat())
                )
                .call()
                .entity(converter);
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
