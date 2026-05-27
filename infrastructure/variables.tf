variable "region" {
  default = "us-west-2"
}

variable "app_name" {
  default = "journal-app"
}

variable "account_id" {
  default = "372344071247"
}

variable "vpc_id" {
  default = "vpc-0f3c84f47f5405c19"
}

variable "subnets" {
  default = [
    "subnet-09ab10f3d6dc2c287",
    "subnet-0b128b773bbde8ea3",
  ]
}