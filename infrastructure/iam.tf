# ECS Task Execution Role
# Used by ECS itself to pull the image from ECR and send logs to CloudWatch.
# This role is assumed by ecs-tasks.amazonaws.com, not your application code.
resource "aws_iam_role" "ecs_task_execution" {
  name        = "ecsTaskExecutionRole"
  description = "Allows ECS tasks to call AWS services on your behalf."

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Journal App Task Role
# Assumed by your running Spring Boot container.
# Grants access to SQS, SES, SSM parameters, etc.
resource "aws_iam_role" "journal_task" {
  name        = "journal-app-task-role"
  description = "Allows ECS tasks to call AWS services on your behalf. (SQS)"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "journal_task_sqs" {
  role       = aws_iam_role.journal_task.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSQSFullAccess"
}

