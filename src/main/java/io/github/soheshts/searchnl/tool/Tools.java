package io.github.soheshts.searchnl.tool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class Tools {
    ChatClient chatClient;

    public Tools(@Qualifier("noHistory") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String getStructuredFilter(UserMessage userMessage) {
        SystemMessage systemMessage = new SystemMessage("""
                Extract structured filters from the shopping query.
                Return JSON only, with these fields (null if not mentioned):
                gender (Men/Women/Boys/Girls/Unisex), baseColour, articleType, season, usage,
                price_max (number), price_min (number), semantic_query (remaining descriptive text, e.g. brand/style words).
                """);
        OllamaChatOptions options = OllamaChatOptions.builder().model("ferguson").disableThinking().temperature(0.0).numPredict(80).build();
        Prompt prompt = Prompt.builder().messages(userMessage, systemMessage).chatOptions(options).build();

        return chatClient.prompt(prompt).call().content();
    }
}
