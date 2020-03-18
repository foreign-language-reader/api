# Token used for connecting between services
resource "random_password" "local_connection_token" {
  length  = 64
  special = true
}

resource "kubernetes_secret" "local_connection_token" {
  metadata {
    name = "local-connection-token"
  }

  data = {
    local_connection_token = random_password.local_connection_token.result
  }
}

resource "random_password" "api_secret_key_base" {
  length  = 64
  special = true
}

resource "kubernetes_secret" "api_secret_key_base" {
  metadata {
    name = "api-secret-key-base"
  }

  data = {
    secret_key_base = random_password.api_secret_key_base.result
  }
}

# Configure NGinx proxy for connecting to all services

resource "kubernetes_secret" "nginx_certificate" {
  metadata {
    name = "nginx-certificate"
  }

  data = {
    "tls.key" = var.private_key_pem
    "tls.crt" = <<EOF
${var.certificate_pem}
${var.issuer_pem}
EOF
  }

  type = "kubernetes.io/tls"
}

resource "helm_release" "nginx_ingress" {
  name       = "nginx-ingress"
  repository = "https://kubernetes-charts.storage.googleapis.com/"
  chart      = "nginx-ingress"
  version    = "1.33.0"
}

# Production elasticsearch setup
# Used to store language content and also logs.

resource "random_password" "elasticsearch_password" {
  length      = 32
  special     = false
  min_numeric = 10
}

resource "kubernetes_secret" "elastic_credentials" {
  metadata {
    name = "elastic-credentials"
  }

  data = {
    username = "lucas"
    password = random_password.elasticsearch_password.result
  }
}

resource "kubernetes_secret" "elasticsearch_internal_certificate" {
  metadata {
    name = "elasticsearch-certificates"
  }

  data = {
    "private_key_pem.crt" = var.private_key_pem
    "certificate_pem.crt" = var.certificate_pem
    "issuer_pem.crt"      = var.issuer_pem
  }
}

resource "helm_release" "elasticsearch" {
  name       = "elasticsearch"
  repository = "https://helm.elastic.co"
  chart      = "elasticsearch"
  version    = "7.6.1"

  values = [file("${path.module}/elasticsearch.yml")]

  depends_on = [
    kubernetes_secret.elasticsearch_internal_certificate,
    kubernetes_secret.elastic_credentials
  ]
}

resource "helm_release" "kibana" {
  name       = "kibana"
  repository = "https://helm.elastic.co"
  chart      = "kibana"
  version    = "7.6.1"
}
