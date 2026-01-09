resource "aws_iam_policy" "ms_payment_sqs" {
  name = "ms-payment-sqs-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes"
      ]
      Resource = [
        data.terraform_remote_state.sqs.outputs.sqs_queue_arns["payment-callback-queue"],
        data.terraform_remote_state.sqs.outputs.sqs_queue_arns["payment-queue"]
      ]
    }]
  })
}

resource "aws_iam_role_policy_attachment" "sqs_attach" {
  role       = aws_iam_role.ms_payment_irsa.name
  policy_arn = aws_iam_policy.ms_payment_sqs.arn
}
