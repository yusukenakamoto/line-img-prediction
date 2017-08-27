package com.example.lineimgprediction.handler;

import com.example.lineimgprediction.entity.EinsteinVisionPredictionResponseEntity;
import com.example.lineimgprediction.service.EinsteinVisionPredictionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Slf4j
@LineMessageHandler
public class MyLineMessageHandler {

    @Autowired
    private EinsteinVisionPredictionService einsteinVisionPredictionService;

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @EventMapping
    public void hangleImageMessageEvent(MessageEvent<ImageMessageContent> event) {
        handleContent(
                event.getMessage().getId(),
                messageContentResponse -> replyMessage(messageContentResponse, event.getReplyToken()));
    }

    private void handleContent(String messageId, Consumer<MessageContentResponse> messageConsumer) {
        final MessageContentResponse messageContentResponse;

        try {
            messageContentResponse = lineMessagingClient.getMessageContent(messageId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        messageConsumer.accept(messageContentResponse);
    }

    private void replyMessage(MessageContentResponse messageContentResponse, String replyToken) {
        InputStream responseInputStream = messageContentResponse.getStream();
        byte[] imageBytes = new byte[(int) messageContentResponse.getLength()];

        try {
            responseInputStream.read(imageBytes, 0, imageBytes.length);
            responseInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        EinsteinVisionPredictionResponseEntity einsteinVisionPredictionResponseEntity =
                einsteinVisionPredictionService.predictionWithImageBase64String(Base64.encodeBase64String(imageBytes));

        ObjectMapper objectMapper = new ObjectMapper();

        List<Message> messageList = new ArrayList<>();
        try {
            messageList.add(new TextMessage(
                    objectMapper.writeValueAsString(einsteinVisionPredictionResponseEntity.getProbabilities())));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        log.info("****replyToken:" + replyToken);

        lineMessagingClient.replyMessage(new ReplyMessage(replyToken, messageList));
    }
}
