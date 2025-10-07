#!/usr/bin/env bash

set -euo pipefail

# This script builds Docker images, pushes them to ECR, and creates/updates ECS services.
# It supports deploying multiple services defined in the SERVICES array below.

# Load environment variables
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

if [[ -f "$SCRIPT_DIR/.env" ]]; then
  # shellcheck disable=SC1091
  source "$SCRIPT_DIR/.env"
else
  echo "Missing $SCRIPT_DIR/.env. Copy .env.example to .env and fill values." >&2
  exit 1
fi

# Required tools checks
for cmd in aws docker jq; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Missing required command: $cmd" >&2
    exit 1
  fi
done

# Validate required env vars
required_vars=(AWS_REGION ECS_CLUSTER_NAME VPC_ID PUBLIC_SUBNET_IDS PRIVATE_SUBNET_IDS SECURITY_GROUP_ID)
for var in "${required_vars[@]}"; do
  if [[ -z "${!var:-}" ]]; then
    echo "Missing required env var: $var" >&2
    exit 1
  fi
done

AWS_REGION=${AWS_REGION}

ACCOUNT_ID=${AWS_ACCOUNT_ID:-}
if [[ -z "$ACCOUNT_ID" ]]; then
  ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
fi

ECR_URI="$ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

# Login to ECR
aws ecr describe-repositories --region "$AWS_REGION" >/dev/null 2>&1 || true
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$ECR_URI"

# Services to deploy: name;dockerfile;context;container_port;cpu;memory
# Adjust as needed.
SERVICES=(
  "uphill;Dockerfile;$ROOT_DIR;8080;256;512"
  "mock-external;$ROOT_DIR/mock-external/Dockerfile;$ROOT_DIR/mock-external;3000;128;256"
)

# Networking
SUBNETS_JSON=$(jq -R -s -c 'split(",") | map(. | gsub("\\s"; "") | select(length>0))' <<<"$PRIVATE_SUBNET_IDS")

create_ecr_repo() {
  local repo_name="$1"
  if ! aws ecr describe-repositories --repository-names "$repo_name" --region "$AWS_REGION" >/dev/null 2>&1; then
    aws ecr create-repository --repository-name "$repo_name" --image-scanning-configuration scanOnPush=true --region "$AWS_REGION" >/dev/null
    echo "Created ECR repo: $repo_name"
  fi
}

ensure_cluster() {
  if ! aws ecs describe-clusters --clusters "$ECS_CLUSTER_NAME" --region "$AWS_REGION" | jq -e '.clusters[0].status == "ACTIVE"' >/dev/null 2>&1; then
    aws ecs create-cluster --cluster-name "$ECS_CLUSTER_NAME" --region "$AWS_REGION" >/dev/null
    echo "Created ECS cluster: $ECS_CLUSTER_NAME"
  fi
}

ensure_log_group() {
  local log_group="/ecs/$ECS_CLUSTER_NAME"
  if ! aws logs describe-log-groups --log-group-name-prefix "$log_group" --region "$AWS_REGION" | jq -e '.logGroups | length > 0' >/dev/null 2>&1; then
    aws logs create-log-group --log-group-name "$log_group" --region "$AWS_REGION" >/dev/null || true
    aws logs put-retention-policy --log-group-name "$log_group" --retention-in-days 14 --region "$AWS_REGION" >/dev/null || true
    echo "Ensured log group: $log_group"
  fi
}

register_task_def() {
  local service_name="$1"
  local image="$2"
  local container_port="$3"
  local cpu="$4"
  local memory="$5"
  local execution_role_arn="${ECS_EXECUTION_ROLE_ARN:-}"
  local task_role_arn="${ECS_TASK_ROLE_ARN:-}"

  local family="${ECS_TASK_FAMILY_PREFIX:-uphill}-${service_name}"
  local log_group="/ecs/$ECS_CLUSTER_NAME"

  local exec_role_arg task_role_arg
  exec_role_arg=""
  task_role_arg=""
  [[ -n "$execution_role_arn" ]] && exec_role_arg="\"executionRoleArn\": \"$execution_role_arn\"," || true
  [[ -n "$task_role_arn" ]] && task_role_arg="\"taskRoleArn\": \"$task_role_arn\"," || true

  local td_json
  td_json=$(cat <<JSON
{
  "family": "$family",
  ${exec_role_arg}
  ${task_role_arg}
  "networkMode": "awsvpc",
  "cpu": "$cpu",
  "memory": "$memory",
  "requiresCompatibilities": ["FARGATE"],
  "runtimePlatform": {"cpuArchitecture": "X86_64", "operatingSystemFamily": "LINUX"},
  "containerDefinitions": [
    {
      "name": "$service_name",
      "image": "$image",
      "portMappings": [ {"containerPort": $container_port, "protocol": "tcp"} ],
      "essential": true,
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "$log_group",
          "awslogs-region": "$AWS_REGION",
          "awslogs-stream-prefix": "$service_name"
        }
      },
      "environment": []
    }
  ]
}
JSON
)

  aws ecs register-task-definition \
    --cli-input-json "$td_json" \
    --region "$AWS_REGION" \
    --query 'taskDefinition.taskDefinitionArn' \
    --output text
}

ensure_service() {
  local service_name="$1"
  local task_def_arn="$2"
  local desired_count=${DESIRED_COUNT:-1}

  if aws ecs describe-services --cluster "$ECS_CLUSTER_NAME" --services "$service_name" --region "$AWS_REGION" | jq -e '.services[0].status == "ACTIVE"' >/dev/null 2>&1; then
    aws ecs update-service \
      --cluster "$ECS_CLUSTER_NAME" \
      --service "$service_name" \
      --task-definition "$task_def_arn" \
      --desired-count $desired_count \
      --region "$AWS_REGION" >/dev/null
    echo "Updated ECS service: $service_name"
  else
    aws ecs create-service \
      --cluster "$ECS_CLUSTER_NAME" \
      --service-name "$service_name" \
      --task-definition "$task_def_arn" \
      --desired-count $desired_count \
      --launch-type FARGATE \
      --network-configuration "awsvpcConfiguration={subnets=$(printf '%s' "$SUBNETS_JSON"),securityGroups=[$SECURITY_GROUP_ID],assignPublicIp=DISABLED}" \
      --region "$AWS_REGION" >/dev/null
    echo "Created ECS service: $service_name"
  fi
}

build_and_push() {
  local service_name="$1"
  local dockerfile="$2"
  local context="$3"

  local image_repo="$service_name"
  local image_tag="${IMAGE_TAG:-$(date +%Y%m%d%H%M%S)}"
  local full_repo="$ECR_URI/$image_repo"
  local full_image="$full_repo:$image_tag"

  create_ecr_repo "$image_repo"

  docker build -f "$dockerfile" -t "$image_repo:latest" "$context"
  docker tag "$image_repo:latest" "$full_image"
  docker push "$full_image"

  echo "$full_image"
}

main() {
  ensure_cluster
  ensure_log_group

  for svc in "${SERVICES[@]}"; do
    IFS=';' read -r name dockerfile context cport cpu memory <<<"$svc"
    echo "\n=== Deploying $name ==="
    image_uri=$(build_and_push "$name" "$dockerfile" "$context")
    td_arn=$(register_task_def "$name" "$image_uri" "$cport" "$cpu" "$memory")
    ensure_service "$name" "$td_arn"
  done

  echo "\nDeployment completed."
}

main "$@"


