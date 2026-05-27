# Dead letter queue — import this first since main queue references its ARN
resource "aws_sqs_queue" "sentiment_dlq" {
  name                    = "weekly-sentiment-email-dlq"
  sqs_managed_sse_enabled = true
}

# Main queue — receives messages from Spring Boot, consumed by Lambda
resource "aws_sqs_queue" "sentiment" {
  name                       = "weekly-sentiment-email-queue"
  visibility_timeout_seconds = 30
  sqs_managed_sse_enabled    = true

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.sentiment_dlq.arn
    maxReceiveCount     = 3
  })
}