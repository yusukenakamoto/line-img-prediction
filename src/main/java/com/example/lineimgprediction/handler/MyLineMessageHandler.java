package com.example.lineimgprediction.handler;

import com.example.lineimgprediction.entity.EinsteinVisionPredictionResponseEntity;
import com.example.lineimgprediction.entity.EinsteinVisionProbabilityResponseEntity;
import com.example.lineimgprediction.service.EinsteinVisionPredictionService;
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

/**
 * LINEメッセージイベントのハンドラークラス.
 */
@Slf4j
@LineMessageHandler
public class MyLineMessageHandler {

    @Autowired
    private EinsteinVisionPredictionService einsteinVisionPredictionService;

    @Autowired
    private LineMessagingClient lineMessagingClient;

    /**
     * イメージメッセージイベントをハンドリングする.
     * @param event イメージメッセージイベント
     */
    @EventMapping
    public void handleImageMessageEvent(MessageEvent<ImageMessageContent> event) {
        log.info("***** Image Message Event *****");
        handleContent(
                event.getMessage().getId(),
                messageContentResponse -> replyMessage(messageContentResponse, event.getReplyToken()));
    }

    /**
     * メッセージコンテンツを取得する.
     * @param messageId メッセージID
     * @param messageConsumer メッセージコンシューマ
     */
    private void handleContent(String messageId, Consumer<MessageContentResponse> messageConsumer) {
        final MessageContentResponse messageContentResponse;

        try {
            // LINEからメッセージコンテンツを取得する
            log.info("***** Get Message Content *****");
            messageContentResponse = lineMessagingClient.getMessageContent(messageId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        messageConsumer.accept(messageContentResponse);
    }

    /**
     * 画像を認識し、メッセージを返信する.
     * @param messageContentResponse メッセージコンテンツ取得結果
     * @param replyToken リプライトークン
     */
    private void replyMessage(MessageContentResponse messageContentResponse, String replyToken) {
        // InputStreamからBase64に変換する
        InputStream responseInputStream = messageContentResponse.getStream();
        byte[] imageBytes = new byte[(int) messageContentResponse.getLength()];

        try {
            responseInputStream.read(imageBytes, 0, imageBytes.length);
            responseInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Einstein Visionで画像認識を行う
        log.info("***** Prediction with Image *****");
        EinsteinVisionPredictionResponseEntity einsteinVisionPredictionResponseEntity =
                einsteinVisionPredictionService.predictionWithImageBase64String(Base64.encodeBase64String(imageBytes));

        // 結果をパースする
        StringBuilder stringBuilder = new StringBuilder();
        for (EinsteinVisionProbabilityResponseEntity probabilityResponseEntity
                : einsteinVisionPredictionResponseEntity.getProbabilities()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("\n");
            }
            stringBuilder.append(probabilityResponseEntity.getLabel() + "(" + probabilityResponseEntity.getProbability() * 100 + "%)");
        }

        List<Message> messageList = new ArrayList<>();
        messageList.add(new TextMessage(stringBuilder.toString()));

        // LINEに返信する
        log.info("***** Reply Message *****");
        lineMessagingClient.replyMessage(new ReplyMessage(replyToken, messageList));
    }
}
