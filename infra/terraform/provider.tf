terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "seu-bucket-terraform-state" //ToDo: ajustar
    key            = "ms-payment/terraform.tfstate" //ToDo: ajustar
    region         = "us-east-1"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "ms-payment"
      Environment  = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

