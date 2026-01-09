resource "aws_iam_role" "ms_payment_irsa" {
  name = "ms-payment-irsa"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Federated = data.terraform_remote_state.kubernetes.outputs.cluster_oidc_provider_arn
      }
      Action = "sts:AssumeRoleWithWebIdentity"
      Condition = {
        StringEquals = {
          format(
            "%s:sub",
            data.terraform_remote_state.kubernetes.outputs.cluster_oidc_provider_url
          ) = "system:serviceaccount:default:ms-payment-sa"
        }

      }
    }]
  })
}
