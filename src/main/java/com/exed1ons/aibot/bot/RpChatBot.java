package com.exed1ons.aibot.bot;

import com.exed1ons.aibot.service.RpBotService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Setter
@Getter
@Component
public class RpChatBot extends TelegramLongPollingBot {

    private final RpBotService rpBotService;
    private String botName;
    private String botToken;
    private final AtomicLong processedMessages = new AtomicLong(0);

    private static final Logger logger = LoggerFactory.getLogger(RpChatBot.class);

    public RpChatBot(@Value("${bot.username}") String botName,
                     @Value("${bot.token}") String botToken,
                     RpBotService rpBotService) {

        super(botToken);
        this.botName = botName;
        this.botToken = botToken;
        this.rpBotService = rpBotService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String chatId = message.getChatId().toString();

            if (message.hasText()) {
                String messageText = message.getText();

                if (messageText.startsWith("/")) {
                    handleCommand(message, chatId);
                    return;
                }

                processedMessages.incrementAndGet();
                processMessageAsync(message, chatId);
            }
        }
    }

    private void handleCommand(Message message, String chatId) {
        String command = message.getText().toLowerCase();
        String response;

        switch (command) {
            case "/start":
                response = "AI Assistant ready. Send questions, requests for information, or tasks that need assistance. " +
                        "I respond only when I can provide genuine value.";
                break;
            case "/help":
                response = """
                    I can help with:
                    🔍 Answering specific questions
                    📊 Calculations and data analysis  
                    💻 Code debugging and technical help
                    🌐 Research and current information
                    📈 Creating charts and visualizations
                    
                    I only respond when I can provide useful assistance.""";
                break;
            case "/stats":
                response = String.format("📊 Total messages processed: %d", processedMessages.get());
                break;
            default:
                response = "Available commands: /start, /help, /stats";
        }

        sendMessage(chatId, response);
    }

    private void processMessageAsync(Message message, String chatId) {
        CompletableFuture.runAsync(() -> {
            try {
                sendTypingAction(chatId);
                String llmResponse = rpBotService.processMessage(message.getText());

                if (llmResponse != null) {
                    sendMessageAsReply(message.getMessageId(), chatId, llmResponse);
                } else {
                    logger.info("LLM analysis determined no response needed for message in chat: {}", chatId);
                }
            } catch (Exception e) {
                logger.error("Error processing message asynchronously", e);
                sendMessage(chatId, "Sorry, I encountered an error processing your message. Please try again.");
            }
        });
    }

    private void sendTypingAction(String chatId) {
        try {
            SendChatAction chatAction = new SendChatAction();
            chatAction.setChatId(chatId);
            chatAction.setAction(ActionType.TYPING);
            execute(chatAction);
        } catch (TelegramApiException e) {
            logger.warn("Failed to send typing action", e);
        }
    }

    public void sendMessageAsReply(Integer messageId, String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setReplyToMessageId(messageId);
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("Markdown");

        try {
            execute(message);
            logger.info("Reply sent successfully to chat: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending reply message", e);
            try {
                message.setParseMode(null);
                execute(message);
            } catch (TelegramApiException fallbackError) {
                logger.error("Fallback message send also failed", fallbackError);
            }
        }
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("Markdown");

        try {
            execute(message);
            logger.info("Message sent successfully to chat: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending message", e);
            try {
                message.setParseMode(null);
                execute(message);
            } catch (TelegramApiException fallbackError) {
                logger.error("Fallback message send also failed", fallbackError);
            }
        }
    }
}