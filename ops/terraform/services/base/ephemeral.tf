locals {
  # All seed environment hierarchies
  seed = local.is_ephemeral_env ? zipmap(data.aws_ssm_parameters_by_path.seed[0].names, data.aws_ssm_parameters_by_path.seed[0].values) : {}

  # Targeted COMMON hierarchy paths to be "copied" from the seed environment into requested ephemeral environment
  common_seed_paths = local.is_ephemeral_env ? {
    "/bfd/${local.env}/common/nonsensitive/enterprise_tools_security_group" = "/bfd/${local.seed_env}/common/nonsensitive/enterprise_tools_security_group"
    "/bfd/${local.env}/common/nonsensitive/key_pair"                        = "/bfd/${local.seed_env}/common/nonsensitive/key_pair"
    "/bfd/${local.env}/common/nonsensitive/kms_key_alias"                   = "/bfd/${local.seed_env}/common/nonsensitive/kms_key_alias"
    "/bfd/${local.env}/common/nonsensitive/management_security_group"       = "/bfd/${local.seed_env}/common/nonsensitive/management_security_group"
    "/bfd/${local.env}/common/nonsensitive/rds_aurora_family"               = "/bfd/${local.seed_env}/common/nonsensitive/rds_aurora_family"
    "/bfd/${local.env}/common/nonsensitive/rds_instance_class"              = "/bfd/${local.seed_env}/common/nonsensitive/rds_instance_class"
    "/bfd/${local.env}/common/nonsensitive/vpc_name"                        = "/bfd/${local.seed_env}/common/nonsensitive/vpc_name"
    "/bfd/${local.env}/common/nonsensitive/vpn_security_group"              = "/bfd/${local.seed_env}/common/nonsensitive/vpn_security_group"
  } : {}

  # Targeted MIGRATOR hierarchy paths to be "copied" from the seed environment into requested ephemeral environment
  migrator_seed_paths = local.is_ephemeral_env ? {
    "/bfd/${local.env}/migrator/sensitive/db_migrator_db_username" = "/bfd/${local.seed_env}/migrator/sensitive/db_migrator_db_username"
    "/bfd/${local.env}/migrator/sensitive/db_migrator_db_password" = "/bfd/${local.seed_env}/migrator/sensitive/db_migrator_db_password"
  } : {}

  # Targeted PIPELINE hierarchy paths to be "copied" from the seed environment into requested ephemeral environment
  pipeline_seed_paths = local.is_ephemeral_env ? {
    "/bfd/${local.env}/pipeline/shared/sensitive/data_pipeline_db_password"          = "/bfd/${local.seed_env}/pipeline/shared/sensitive/data_pipeline_db_password"
    "/bfd/${local.env}/pipeline/shared/sensitive/data_pipeline_db_username"          = "/bfd/${local.seed_env}/pipeline/shared/sensitive/data_pipeline_db_username"
    "/bfd/${local.env}/pipeline/shared/sensitive/data_pipeline_hicn_hash_iterations" = "/bfd/${local.seed_env}/pipeline/shared/sensitive/data_pipeline_hicn_hash_iterations"
    "/bfd/${local.env}/pipeline/shared/sensitive/data_pipeline_hicn_hash_pepper"     = "/bfd/${local.seed_env}/pipeline/shared/sensitive/data_pipeline_hicn_hash_pepper"
  } : {}

  # Targeted SERVER hierarchy paths to be "copied" from the seed environment into requested ephemeral environment
  server_seed_paths = local.is_ephemeral_env ? {
    "/bfd/${local.env}/server/nonsensitive/asg_desired_instance_count"               = "/bfd/${local.seed_env}/server/nonsensitive/asg_desired_instance_count"
    "/bfd/${local.env}/server/nonsensitive/asg_instance_warmup_time"                 = "/bfd/${local.seed_env}/server/nonsensitive/asg_instance_warmup_time"
    "/bfd/${local.env}/server/nonsensitive/asg_max_instance_count"                   = "/bfd/${local.seed_env}/server/nonsensitive/asg_max_instance_count"
    "/bfd/${local.env}/server/nonsensitive/asg_max_warm_instance_count"              = "/bfd/${local.seed_env}/server/nonsensitive/asg_max_warm_instance_count"
    "/bfd/${local.env}/server/nonsensitive/asg_min_instance_count"                   = "/bfd/${local.seed_env}/server/nonsensitive/asg_min_instance_count"
    "/bfd/${local.env}/server/nonsensitive/data_server_ssl_client_certificates_json" = "/bfd/${local.seed_env}/server/nonsensitive/data_server_ssl_client_certificates_json"
    "/bfd/${local.env}/server/nonsensitive/launch_template_instance_type"            = "/bfd/${local.seed_env}/server/nonsensitive/launch_template_instance_type"
    "/bfd/${local.env}/server/nonsensitive/launch_template_volume_size_gb"           = "/bfd/${local.seed_env}/server/nonsensitive/launch_template_volume_size_gb"
    "/bfd/${local.env}/server/sensitive/server_keystore_base64"                      = "/bfd/${local.seed_env}/server/sensitive/server_keystore_base64"
    "/bfd/${local.env}/server/sensitive/data_server_appserver_https_port"            = "/bfd/${local.seed_env}/server/sensitive/data_server_appserver_https_port"
    "/bfd/${local.env}/server/sensitive/data_server_db_password"                     = "/bfd/${local.seed_env}/server/sensitive/data_server_db_password"
    "/bfd/${local.env}/server/sensitive/data_server_db_username"                     = "/bfd/${local.seed_env}/server/sensitive/data_server_db_username"
    "/bfd/${local.env}/server/sensitive/data_server_new_relic_license_key"           = "/bfd/${local.seed_env}/server/sensitive/data_server_new_relic_license_key"
    "/bfd/${local.env}/server/sensitive/data_server_new_relic_metric_key"            = "/bfd/${local.seed_env}/server/sensitive/data_server_new_relic_metric_key"
    "/bfd/${local.env}/server/sensitive/test_client_cert"                            = "/bfd/${local.seed_env}/server/sensitive/test_client_cert"
    "/bfd/${local.env}/server/sensitive/test_client_key"                             = "/bfd/${local.seed_env}/server/sensitive/test_client_key"
  } : {}
}

data "aws_db_cluster_snapshot" "seed" {
  count = local.is_ephemeral_env ? 1 : 0

  db_cluster_identifier = "bfd-${local.seed_env}-aurora-cluster"
  most_recent           = true
  db_cluster_snapshot_identifier = lookup(
    local.common_nonsensitive_ssm,
    "/bfd/${local.env}/common/nonsensitive/rds_snapshot_identifier",
    var.ephemeral_rds_snapshot_id_override
  )
}

# NOTE: Contains *all* seed environment hierarchies including sensitive and nonsensitive values
data "aws_ssm_parameters_by_path" "seed" {
  count = local.is_ephemeral_env ? 1 : 0

  path            = "/bfd/${local.seed_env}/"
  with_decryption = true
  recursive       = true
}

# Copy targeted COMMON hierarchy paths from seed environment into requested ephemeral environment
resource "aws_ssm_parameter" "ephemeral_common" {
  for_each  = local.common_seed_paths
  key_id    = contains(split("/", each.key), "sensitive") ? data.aws_kms_key.cmk.arn : null
  name      = each.key
  overwrite = true
  type      = contains(split("/", each.key), "sensitive") ? "SecureString" : "String"
  value     = local.seed[each.value]
}

# Copy targeted MIGRATOR hierarchy paths from seed environment into requested ephemeral environment
resource "aws_ssm_parameter" "ephemeral_migrator" {
  for_each  = local.migrator_seed_paths
  key_id    = contains(split("/", each.key), "sensitive") ? data.aws_kms_key.cmk.arn : null
  name      = each.key
  overwrite = true
  type      = contains(split("/", each.key), "sensitive") ? "SecureString" : "String"
  value     = local.seed[each.value]
}

# Copy targeted PIPELINE hierarchy paths from seed environment into requested ephemeral environment
resource "aws_ssm_parameter" "ephemeral_pipeline" {
  for_each  = local.pipeline_seed_paths
  key_id    = contains(split("/", each.key), "sensitive") ? data.aws_kms_key.cmk.arn : null
  name      = each.key
  overwrite = true
  type      = contains(split("/", each.key), "sensitive") ? "SecureString" : "String"
  value     = local.seed[each.value]
}

# Copy targeted SERVER hierarchy paths from seed environment into requested ephemeral environment
resource "aws_ssm_parameter" "ephemeral_server" {
  for_each  = local.server_seed_paths
  key_id    = contains(split("/", each.key), "sensitive") ? data.aws_kms_key.cmk.arn : null
  name      = each.key
  overwrite = true
  type      = contains(split("/", each.key), "sensitive") ? "SecureString" : "String"
  value     = local.seed[each.value]
  tier      = "Intelligent-Tiering"
}
