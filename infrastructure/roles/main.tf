data "aws_subnet" "private" {
  count = length(var.private_subnet_ids)
  id    = var.private_subnet_ids[count.index]
}

# Used to get AWS account number without putting it in the repo
data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

# API fargate container role

data "aws_iam_policy_document" "task_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "api_task_exec" {
  name               = "api-task-foreign-language-reader"
  assume_role_policy = data.aws_iam_policy_document.task_assume_role_policy.json
}

resource "aws_iam_role_policy_attachment" "allow_logging_lambda" {
  role       = aws_iam_role.api_task_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Codebuild role

data "aws_iam_policy_document" "codebuild_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["codebuild.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "codebuild_role" {
  name               = "codebuild-foreign-language-reader"
  assume_role_policy = data.aws_iam_policy_document.codebuild_policy.json
}

data "aws_iam_policy_document" "build_in_vpc" {
  statement {
    actions   = ["ec2:CreateNetworkInterface", "ec2:DescribeDhcpOptions", "ec2:DescribeNetworkInterfaces", "ec2:DeleteNetworkInterface", "ec2:DescribeSubnets", "ec2:DescribeSecurityGroups", "ec2:DescribeVpcs"]
    effect    = "Allow"
    resources = ["*"]
  }
  statement {
    actions   = ["ec2:CreateNetworkInterfacePermission"]
    effect    = "Allow"
    resources = ["arn:aws:ec2:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:network-interface/*"]
    condition {
      test     = "StringEquals"
      variable = "ec2:Subnet"

      values = data.aws_subnet.private.*.arn
    }
  }
  statement {
    actions   = ["logs:CreateLogStream", "logs:CreateLogGroup", "logs:PutLogEvents"]
    effect    = "Allow"
    resources = ["*"]
  }
}

resource "aws_iam_policy" "codebuild_permissions" {
  description = "IAM policy for building foreign-language-reader api in codebuild."

  policy = data.aws_iam_policy_document.build_in_vpc.json
}

resource "aws_iam_role_policy_attachment" "codebuild_permissions" {
  role       = aws_iam_role.codebuild_role.name
  policy_arn = aws_iam_policy.codebuild_permissions.arn
}

# Allow running

data "aws_iam_policy_document" "lambda_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "vocabulary_lambda_exec" {
  name               = "vocabulary-lambda-foreign-language-reader"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role_policy.json
}

# Vocabulary lambda

data "aws_iam_policy_document" "allow_logging" {
  statement {
    actions   = ["logs:CreateLogStream", "logs:CreateLogGroup", "logs:PutLogEvents"]
    effect    = "Allow"
    resources = ["*"]
  }
  statement {
    actions   = ["s3:PutObject"]
    effect    = "Allow"
    resources = ["*"]
  }
}

resource "aws_iam_policy" "logging_policy" {
  description = "IAM policy for logging from a lambda"

  policy = data.aws_iam_policy_document.allow_logging.json
}

resource "aws_iam_role_policy_attachment" "allow_logging" {
  role       = aws_iam_role.vocabulary_lambda_exec.name
  policy_arn = aws_iam_policy.logging_policy.arn
}