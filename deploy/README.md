### ECS deployment for Uphill services

This directory contains a simple AWS CLI-based flow to publish Docker images to ECR and deploy/update ECS Fargate services.

#### Prerequisites
- AWS account with permissions for ECR, ECS, CloudWatch Logs, and VPC networking
- `aws` CLI configured (`aws configure`)
- `docker`, `jq`
- Existing VPC, subnets, and security group

#### Setup
1. Copy the env template and fill in values:
   ```bash
   cp deploy/env.example deploy/.env
   $EDITOR deploy/.env
   ```
2. Review the `SERVICES` list in `deploy/deploy.sh` to match your services:
   - Format: `name;dockerfile;context;container_port;cpu;memory`
   - Defaults included:
     - `uphill` Java Spring Boot app via root `Dockerfile` on port 8080
     - `mock-external` Node service via `mock-external/Dockerfile` on port 3000

#### Deploy
```bash
bash deploy/deploy.sh
```

The script will:
- Create ECR repos if missing
- Build and push images with `IMAGE_TAG` or timestamp
- Register task definitions for Fargate
- Create or update ECS services in the specified cluster

#### Notes
- Services run in private subnets with no public IP by default. Ensure NAT + routes are available, or set `assignPublicIp=ENABLED` if you prefer public tasks.
- To customize CPU/Memory, adjust per-service entries.
- To pass environment variables to containers, extend the `containerDefinitions.environment` in `deploy.sh` or wire in SSM/Secrets Manager and task roles.


