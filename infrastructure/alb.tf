# Application Load Balancer
resource "aws_lb" "journal" {
  name               = "journal-app-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = var.subnets
  ip_address_type    = "ipv4"
}

# Target group — points at ECS tasks on port 8080
# target_type = "ip" is required for Fargate (tasks don't have instance IDs)
resource "aws_lb_target_group" "journal" {
  name             = "journal-app-tg"
  port             = 8080
  protocol         = "HTTP"
  protocol_version = "HTTP1"
  vpc_id           = var.vpc_id
  target_type      = "ip"
  ip_address_type  = "ipv4"

  health_check {
    enabled             = true
    path                = "/journal/public/health-check"
    protocol            = "HTTP"
    port                = "traffic-port"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 5
    unhealthy_threshold = 2
    matcher             = "200"
  }
}

# HTTP listener — forwards all traffic to the target group
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.journal.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.journal.arn
  }

  lifecycle {
    ignore_changes = [default_action]
  }
}