module "network" {
  source     = "./network"
  cidr_block = var.cidr_block
}

module "roles" {
  source             = "./roles"
  private_subnet_ids = module.network.private_subnet_ids
}

module "database" {
  source             = "./database"
  vpc_id             = module.network.vpc_id
  private_subnet_ids = module.network.private_subnet_ids
  instance_size      = var.instance_size
  rds_username       = var.rds_username
  rds_password       = var.rds_password
}

resource "aws_ecs_cluster" "main" {
  name = "foreign-language-reader-${var.env}"
}

module "api" {
  source             = "./api"
  env                = var.env
  iam_role           = module.roles.fargate_role
  cluster_id         = aws_ecs_cluster.main.id
  cpu                = var.cpu
  memory             = var.memory
  vpc_id             = module.network.vpc_id
  private_subnet_ids = module.network.private_subnet_ids
  public_subnet_ids  = module.network.public_subnet_ids
  database_endpoint  = module.database.database_endpoint
  rds_username       = var.rds_username
  rds_password       = var.rds_password
  secret_key_base    = var.secret_key_base
}

module "language_service" {
  source             = "./language_service"
  env                = var.env
  iam_role           = module.roles.fargate_role
  cluster_id         = aws_ecs_cluster.main.id
  cpu                = var.cpu
  memory             = var.memory
  vpc_id             = module.network.vpc_id
  private_subnet_ids = module.network.private_subnet_ids
  public_subnet_ids  = module.network.public_subnet_ids
}

module "frontend_prod" {
  source = "./frontend"
  env    = var.env
}

module "frontend_dev" {
  source = "./frontend"
  env    = "dev"
}

module "frontend_storybook" {
  source = "./frontend"
  env    = "storybook"
}

# The CI/CD configuration for this application
module "pipeline" {
  source                        = "./pipeline"
  codebuild_role                = module.roles.codebuild_role
  codepipeline_role             = module.roles.codepipeline_role
  vpc_id                        = module.network.vpc_id
  private_subnet_ids            = module.network.private_subnet_ids
  github_token                  = var.github_token
  cluster_name                  = aws_ecs_cluster.main.name
  api_ecr_name                  = module.api.ecr_name
  api_service_name              = module.api.service_name
  language_service_ecr_name     = module.language_service.ecr_name
  language_service_service_name = module.language_service.service_name
}
