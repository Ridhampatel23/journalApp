# EventBridge rule — triggers the weekly sentiment digest job
# NOTE: not currently deployed (requires HTTPS endpoint + ACM certificate)
# Represents the intended architecture for the scheduled cron trigger.

resource "aws_cloudwatch_event_rule" "weekly_sentiment" {
  name                = "weekly-sentiment-digest"
  description         = "Fires every Sunday at 8am UTC to trigger the sentiment digest job"
  schedule_expression = "cron(0 8 ? * SUN *)"
  state               = "ENABLED"
}

resource "aws_cloudwatch_event_target" "weekly_sentiment_sqs" {
  rule      = aws_cloudwatch_event_rule.weekly_sentiment.name
  target_id = "weekly-sentiment-sqs-target"
  arn       = aws_sqs_queue.sentiment.arn
}

# IAM role allowing EventBridge to send messages to SQS
resource "aws_iam_role" "eventbridge_scheduler" {
  name        = "eventbridge-sentiment-scheduler-role"
  description = "Allows EventBridge to send messages to the sentiment SQS queue"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "events.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "eventbridge_sqs" {
  name = "eventbridge-sqs-send"
  role = aws_iam_role.eventbridge_scheduler.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = "sqs:SendMessage"
      Resource = aws_sqs_queue.sentiment.arn
    }]
  })
}