data "aws_region" "current" {}

data "aws_sns_topic" "alert_sns" {
  count = local.alert_sns_name != null ? 1 : 0
  name  = local.alert_sns_name
}

data "aws_sns_topic" "warning_sns" {
  count = local.warning_sns_name != null ? 1 : 0
  name  = local.warning_sns_name
}

data "aws_sns_topic" "alert_ok_sns" {
  count = local.alert_ok_sns_name != null ? 1 : 0
  name  = local.alert_ok_sns_name
}

data "aws_sns_topic" "warning_ok_sns" {
  count = local.warning_ok_sns_name != null ? 1 : 0
  name  = local.warning_ok_sns_name
}

# TODO: [PACA-1029] Remove when Claim/ClaimResponse SLOs have been verified
data "aws_sns_topic" "bfd_test_sns" {
  name  = local.bfd_test_slack_sns
}

data "external" "client_ssls_by_partner" {
  for_each = local.metrics

  program = [
    "bash",
    "${path.module}/get-partner-client-ssl.sh",
    each.value,
    local.namespace,
    jsonencode({
      for partner, config in local.all_partners :
      partner => lookup(config.client_ssl_regex, replace(var.env, "-", "_"), null)
    })
  ]
}
