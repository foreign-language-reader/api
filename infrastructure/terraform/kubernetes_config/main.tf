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

resource "helm_release" "nginx_ingress" {
  name       = "nginx-ingress"
  repository = "https://kubernetes-charts.storage.googleapis.com/"
  chart      = "nginx-ingress"
  version    = "1.33.0"
}

resource "kubernetes_namespace" "cert_manager" {
  metadata {
    name = "cert-manager"
  }
}

resource "helm_release" "cert_manager" {
  name       = "cert-manager"
  repository = "https://charts.jetstack.io"
  chart      = "cert-manager"
  version    = "v0.13.1"
  namespace  = "cert-manager"
}

# Production elasticsearch setup
# Used to store language content and also logs.

resource "helm_release" "elasticsearch" {
  name       = "elasticsearch"
  repository = "https://helm.elastic.co"
  chart      = "elasticsearch"
  version    = "7.6.1"
}

resource "helm_release" "kibana" {
  name       = "kibana"
  repository = "https://helm.elastic.co"
  chart      = "kibana"
  version    = "7.6.1"
}
