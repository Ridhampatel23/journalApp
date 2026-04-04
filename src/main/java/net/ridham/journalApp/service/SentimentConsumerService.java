package net.ridham.journalApp.service;

import net.ridham.journalApp.model.SentimentQueueMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/// Don't use it anymore as the work is offloaded to AWS SQS
@Service
public class SentimentConsumerService {

    @Autowired
    private EmailService emailService;

   // @KafkaListener(topics = "weekly_sentiment", groupId = "weekly_sentiment_group")
    public void consume(SentimentQueueMessage sentimentQueueMessage) {sendEmail(sentimentQueueMessage);}

    private void sendEmail(SentimentQueueMessage sentimentQueueMessage) {
        emailService.sendEmail(sentimentQueueMessage.getEmail(), "Sentiment for previous week", sentimentQueueMessage.getSentiment());
    }



}
