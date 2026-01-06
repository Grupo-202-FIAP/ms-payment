variable "aws_region" {
  description = "Região AWS onde os recursos serão criados"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Ambiente (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "oidc_provider_arn" {
  description = "ARN do OIDC provider do EKS"
  type        = string
}

variable "oidc_provider_url" {
  description = "URL do OIDC provider do EKS"
  type        = string
}

variable "sqs_order_queue_name" {
  description = "Nome da fila SQS de order"
  type        = string
}

variable "sqs_order_callback_queue_name" {
  description = "Nome da fila SQS de callback"
  type        = string
}

variable "account_id" {
  description = "ID da conta AWS"
  type        = string
}

variable "eks_cluster_name" {
  description = "Nome do cluster EKS"
  type        = string
}

variable "terraform_state_bucket_name" {
  description = "Nome do bucket S3 para armazenar o state do Terraform"
  type        = string
}
