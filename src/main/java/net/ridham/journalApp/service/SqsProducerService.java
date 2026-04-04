package net.ridham.journalApp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ridham.journalApp.model.SentimentQueueMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsProducerService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.weekly-sentiment-queue-url}")
    private String queueUrl;

    public void sendWeeklySentimentMessage(SentimentQueueMessage message) {
        try {
            String body = objectMapper.writeValueAsString(message);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(body)
                    .build();

            sqsClient.sendMessage(request);
            log.info("Sent SQS message for {}", message.getEmail());

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize SQS message", e);
        }
    }
}