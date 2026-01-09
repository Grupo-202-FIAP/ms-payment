resource "aws_ecr_repository" "ms_payment" {
  name                 = "ms-payment"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}
