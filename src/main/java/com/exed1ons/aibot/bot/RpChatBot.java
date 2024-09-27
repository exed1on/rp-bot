package com.exed1ons.aibot.bot;

import com.exed1ons.aibot.pesistence.repository.MessageRepository;
import com.exed1ons.aibot.service.RpBotService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@Setter
@Getter
@Component
public class RpChatBot extends TelegramLongPollingBot {

    private final RpBotService rpBotService;
    private final MessageRepository messageRepository;

    private String botName;
    private String botToken;

    private final Map<String, Integer> messageCountMap = new HashMap<>();
    private final Random random = new Random();

    private static final Logger logger = LoggerFactory.getLogger(RpChatBot.class);

    public RpChatBot(@Value("${bot.username}") String botName,
                     @Value("${bot.token}") String botToken,
                     RpBotService rpBotService,
                     MessageRepository messageRepository) {

        super(botToken);
        this.botName = botName;
        this.botToken = botToken;
        this.rpBotService = rpBotService;
        this.messageRepository = messageRepository;
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

            rpBotService.saveUserMessage(message.getText(), message.getFrom().getId().toString());

            if (message.isReply() && isReplyToBot(message)) {
                handleUserReply(message, chatId);
            }

            trackAndTriggerOnMessageCount(message, chatId);
        }
    }

    private boolean isReplyToBot(Message message) {
        return message.getReplyToMessage() != null &&
                message.getReplyToMessage().getFrom().getUserName().equals(getBotUsername());
    }

    private void handleUserReply(Message message, String chatId) {
        String llmResponse = rpBotService.sendMessageToLLM();
        sendMessageAsReply(message.getMessageId(), chatId, llmResponse);
    }

    private void trackAndTriggerOnMessageCount(Message message, String chatId) {
        int currentCount = messageCountMap.getOrDefault(chatId, 0) + 1;
        messageCountMap.put(chatId, currentCount);

        int triggerLimit = random.nextInt(5) + 3;
        if (currentCount >= triggerLimit) {
            messageCountMap.put(chatId, 0);
            String llmResponse = rpBotService.sendMessageToLLM();
            sendMessageAsReply(message.getMessageId(), chatId, llmResponse);
        }
    }

    public void sendMessageAsReply(Integer messageId, String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setReplyToMessageId(messageId);
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error while sending message", e);
        }
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error while sending message", e);
        }
    }
}
