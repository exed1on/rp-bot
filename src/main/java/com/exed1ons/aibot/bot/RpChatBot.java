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

import java.io.ByteArrayInputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
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
            var message = update.getMessage();
            var chatId = message.getChatId().toString();

            var messageUnixTime = message.getDate().longValue();
            var currentUnixTime = System.currentTimeMillis() / 1000;
            if (currentUnixTime - messageUnixTime > 60) {
                logger.info("ignoring ancient drama: {}", message.getText());
                return;
            }

            if (message.hasText()) {
                if (message.getText().startsWith("/")) {
                    handleCommand(message, chatId);
                    return;
                }

                if (shouldBotReply(message)) {
                    logger.info("Replying to message: {}", message.getText());
                    processedMessages.incrementAndGet();
                    processMessageAsync(message, chatId);
                } else {
                    logger.debug("Ignoring message (Random chance or no tag): {}", message.getText());
                }
            }
        }
    }

    private boolean shouldBotReply(Message message) {
        if (message.getChat().isUserChat()) {
            return true;
        }

        if (message.getReplyToMessage() != null &&
                message.getReplyToMessage().getFrom().getUserName().equalsIgnoreCase(botName)) {
            return true;
        }

        if (message.getText().toLowerCase().contains("@" + botName.toLowerCase())) {
            return true;
        }

        return ThreadLocalRandom.current().nextInt(10) == 0;
    }

    private void processMessageAsync(Message message, String chatId) {
        CompletableFuture.runAsync(() -> {
            try {
                sendTypingAction(chatId);
                var result = rpBotService.generateRoleplayResponse(message.getText(), message.getFrom().getFirstName(), chatId);

                if (result != null) {
                    var text = (String) result.get("text");
                    var image = (byte[]) result.get("image");

                    if (image != null && image.length > 0) {
                        logger.info("attempting to send photo to telegram, size: {}", image.length);
                        var sendPhoto = new SendPhoto();
                        sendPhoto.setChatId(chatId);
                        sendPhoto.setPhoto(new InputFile(new ByteArrayInputStream(image), "alina.jpg"));
                        if (text != null) sendPhoto.setCaption(text);
                        execute(sendPhoto);
                    } else if (text != null) {
                        logger.info("no image to send, falling back to text reply");
                        sendMessageAsReply(message.getMessageId(), chatId, text);
                    }
                }
            } catch (Exception e) {
                logger.error("fail", e);
            }
        });
    }

    private void handleCommand(Message message, String chatId) {
        if (message.getText().equalsIgnoreCase("/stats")) {
            sendMessage(chatId, "сообщений обработано: " + processedMessages.get() + ". я устала.");
        }
    }

    private void sendTypingAction(String chatId) {
        try {
            var chatAction = new SendChatAction();
            chatAction.setChatId(chatId);
            chatAction.setAction(ActionType.TYPING);
            execute(chatAction);
        } catch (TelegramApiException e) {
            logger.warn("Failed to send typing action", e);
        }
    }

    public void sendMessageAsReply(Integer messageId, String chatId, String text) {
        var message = new SendMessage();
        message.setReplyToMessageId(messageId);
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode(null);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error sending reply message", e);
        }
    }

    public void sendMessage(String chatId, String text) {
        var message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error sending message", e);
        }
    }
}