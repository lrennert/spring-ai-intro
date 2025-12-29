package guru.springframework.springaiintro.services;

import guru.springframework.springaiintro.model.Answer;
import guru.springframework.springaiintro.model.GetCapitalRequest;
import guru.springframework.springaiintro.model.GetCapitalResponse;
import guru.springframework.springaiintro.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final ChatClient chatClient;
    private final ChatModel chatModel;

    @Value("classpath:templates/get-capital.st")
    private Resource getCapitalPrompt;

    @Value("classpath:templates/get-capital-with-info.st")
    private Resource getCapitalWithInfoPrompt;

    public OpenAIServiceImpl(ChatClient chatClient, ChatModel chatModel) {
        this.chatClient = chatClient;
        this.chatModel = chatModel;
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


        if (Boolean.TRUE.equals(getCapitalRequest.debugEnabled())) {
            String promptText;
            try (InputStream is = getCapitalPrompt.getInputStream()) {
                promptText = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            promptText = promptText
                    .replace("{stateOrCountry}", getCapitalRequest.stateOrCountry())
                    .replace("{format}", converter.getFormat());

            Prompt prompt = new Prompt(new UserMessage(promptText));
            System.out.println("Prompt: " + prompt);

            ChatResponse response = chatModel.call(prompt);
            System.out.println("Total tokens: " + response.getMetadata().getUsage().getTotalTokens());
        }

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
