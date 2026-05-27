resource "aws_ecs_cluster" "journal" {
  name = "journal-app-ecs-cluster"

  configuration {
    execute_command_configuration {
      logging = "DEFAULT"
    }
  }
}

resource "aws_ecs_service" "journal" {
  name                       = "journal-app-task-service-cucdggr0"
  cluster                    = aws_ecs_cluster.journal.id
  desired_count              = 1
  launch_type                = "FARGATE"
  enable_ecs_managed_tags    = true
  availability_zone_rebalancing = "ENABLED"

  task_definition = "journal-app-task-service-cucdggr0"

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  network_configuration {
    subnets = [
      "subnet-09ab10f3d6dc2c287",
      "subnet-0b128b773bbde8ea3",
      "subnet-00a8d8549392ce247",
      "subnet-06459f11af500102c"
    ]
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.journal.arn
    container_name   = "journal-app"
    container_port   = 8080
  }

  lifecycle {
    ignore_changes = [task_definition, desired_count]
  }
}