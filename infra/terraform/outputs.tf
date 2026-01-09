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

output "ecr_repository_uri" {
  value = aws_ecr_repository.ms_payment.repository_url
}
