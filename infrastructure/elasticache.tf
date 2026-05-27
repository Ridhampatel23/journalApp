# ElastiCache Redis cluster — hot-path caching for weather API responses and app config
# NOTE: not currently deployed due to cost (~$12/month for t4g.micro)
# Self-hosted Redis on EC2 is used in the current deployment.
# This represents the intended production-grade architecture.

resource "aws_elasticache_subnet_group" "journal" {
  name        = "journal-app-cache-subnet-group"
  description = "Subnet group for journal app ElastiCache cluster"
  subnet_ids  = [] # ⚠️ populate with your private subnet IDs before applying
}

resource "aws_security_group" "elasticache" {
  name        = "journal-app-elasticache-sg"
  description = "Allow Redis access from ECS tasks only"

  ingress {
    description     = "Redis from ECS"
    from_port       = 6379
    to_port         = 6379
    protocol        = "tcp"
    security_groups = [] # ⚠️ populate with ECS security group ID before applying
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_elasticache_cluster" "journal" {
  cluster_id           = "journal-app-redis"
  engine               = "redis"
  node_type            = "cache.t4g.micro"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  engine_version       = "7.0"
  port                 = 6379
  subnet_group_name    = aws_elasticache_subnet_group.journal.name

  security_group_ids = [aws_security_group.elasticache.id]

  tags = {
    Name = "journal-app-redis"
  }
}