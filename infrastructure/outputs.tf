output "alb_dns_name" {
  description = "Public DNS name of the ALB"
  value       = aws_lb.journal.dns_name
}

output "ecr_repository_url" {
  description = "ECR repository URL for CI/CD image push"
  value       = aws_ecr_repository.journal.repository_url
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = aws_ecs_cluster.journal.name
}

output "ecs_service_name" {
  description = "ECS service name"
  value       = aws_ecs_service.journal.name
}

output "sqs_queue_url" {
  description = "SQS sentiment queue URL"
  value       = aws_sqs_queue.sentiment.url
}

output "sqs_dlq_url" {
  description = "SQS dead-letter queue URL"
  value       = aws_sqs_queue.sentiment_dlq.url
}