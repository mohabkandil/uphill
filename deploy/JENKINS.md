### Jenkins pipeline (ECS deploy)

This repository includes a declarative `Jenkinsfile` that leverages `deploy/deploy.sh` to build images, push to ECR, and deploy ECS Fargate services.

#### Jenkins setup
- Create a Multibranch Pipeline or Pipeline job pointing to this repo
- Ensure the agent has `aws`, `docker`, `jq` installed and Docker daemon access
- Configure AWS credentials on the agent (e.g., instance profile, `aws configure`, or Jenkins credentials + withAWS)

#### Parameters
- `AWS_REGION`: AWS region (default `eu-west-1`)
- `IMAGE_TAG`: Optional image tag (defaults to Jenkins build number)
- `DESIRED_COUNT`: ECS desired tasks (default `1`)

#### What the pipeline does
1. Checks out code
2. Creates/updates `deploy/.env` using `deploy/env.example` and Jenkins parameters
3. Runs `bash deploy/deploy.sh`

`deploy.sh` will create ECR repos, build/push images, register task definitions, and create/update ECS services.

#### Customization
- Edit `SERVICES` in `deploy/deploy.sh` to match your services and ports
- Provide IAM roles via `ECS_EXECUTION_ROLE_ARN`/`ECS_TASK_ROLE_ARN` in `deploy/.env`
- Adjust network settings (`PRIVATE_SUBNET_IDS`, `SECURITY_GROUP_ID`, etc.)


