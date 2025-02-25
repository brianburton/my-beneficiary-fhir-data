locals {
  eyaml_file = "mgmt.eyaml"
  eyaml      = data.external.eyaml.result

  common_sensitive = {
    for key, value in local.eyaml
    : key => value if contains(split("/", key), "common") && value != "UNDEFINED"
  }
}

data "external" "eyaml" {
  program = ["${path.module}/scripts/read-and-decrypt-eyaml.sh", local.eyaml_file]
}

resource "aws_ssm_parameter" "common_sensitive" {
  for_each = local.common_sensitive

  key_id    = var.kms_key_id
  name      = each.key
  overwrite = true
  type      = "SecureString"
  value     = each.value
}
