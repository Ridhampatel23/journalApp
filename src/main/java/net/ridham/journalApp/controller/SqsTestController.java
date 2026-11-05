package net.ridham.journalApp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ridham.journalApp.model.SentimentQueueMessage;
import net.ridham.journalApp.service.SqsProducerService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.exception.SdkException;

@Slf4j
@RestController
@RequestMapping("/internal/test")
@Profile("dev2")
@RequiredArgsConstructor
public class SqsTestController {

    private final SqsProducerService sqsProducerService;

    @PostMapping("/sqs")
    public ResponseEntity<String> testSqs() {
        try {
            SentimentQueueMessage message =
                    SentimentQueueMessage.builder()
                            .email("test@example.com")
                            .sentiment("HAPPY")
                            .build();

            sqsProducerService.sendWeeklySentimentMessage(message);

            log.info("SQS test message sent successfully");

            return ResponseEntity.ok("SQS message sent");

        } catch (SdkException e) {
            log.error("Failed to send SQS message", e);

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("SQS unavailable. Check application logs." + e);

        } catch (Exception e) {
            log.error("Unexpected error during SQS test", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error during SQS test.");
        }
    }
}

