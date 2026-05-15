terraform {
  required_version = ">= 1.5"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  backend "s3" {
    bucket         = "ridham-journal-tfstate"
    key            = "journal-app/terraform.tfstate"
    region         = "us-west-2"
    dynamodb_table = "journal-tfstate-locks"
    encrypt        = true
  }
}