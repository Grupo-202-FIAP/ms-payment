output "irsa_role_arn" {
  description = "ARN da role IRSA criada"
  value       = aws_iam_role.ms_payment_irsa.arn
}

output "irsa_role_name" {
  description = "Nome da role IRSA criada"
  value       = aws_iam_role.ms_payment_irsa.name
}

output "sqs_policy_arn" {
  description = "ARN da policy SQS"
  value       = aws_iam_policy.ms_payment_sqs.arn
}

output "ssm_policy_arn" {
  description = "ARN da policy SSM"
  value       = aws_iam_policy.ms_payment_ssm.arn
}

output "order_queue_arn" {
  description = "ARN da fila SQS de order"
  value       = data.aws_sqs_queue.order_queue.arn
}

output "order_queue_url" {
  description = "URL da fila SQS de order"
  value       = data.aws_sqs_queue.order_queue.url
}

output "order_callback_queue_arn" {
  description = "ARN da fila SQS de callback"
  value       = data.aws_sqs_queue.order_callback_queue.arn
}

output "order_callback_queue_url" {
  description = "URL da fila SQS de callback"
  value       = data.aws_sqs_queue.order_callback_queue.url
}

