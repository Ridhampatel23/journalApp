# ALB security group — accepts HTTP traffic from the internet
resource "aws_security_group" "alb" {
  name        = "journal-app-alb-sg"
  description = "ALB security group for journal app"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# ECS task security group — only accepts traffic from the ALB, nothing else
resource "aws_security_group" "ecs" {
  name        = "journal-app-ecs-sg"
  description = "ECS task security group for journal app"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}