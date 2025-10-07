pipeline {
  agent any

  options {
    ansiColor('xterm')
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    // Parameter defaults; can be overridden by Jenkins parameters
    AWS_REGION = "${params.AWS_REGION ?: 'eu-west-1'}"
    IMAGE_TAG = "${params.IMAGE_TAG ?: env.BUILD_NUMBER}"
    DESIRED_COUNT = "${params.DESIRED_COUNT ?: '1'}"

    // Optional: Jenkins credentials IDs
    // AWS_CREDENTIALS = 'aws-jenkins-creds' // if using withCredentials
  }

  parameters {
    string(name: 'AWS_REGION', defaultValue: 'eu-west-1', description: 'AWS region for ECR/ECS')
    string(name: 'IMAGE_TAG', defaultValue: '', description: 'Image tag (default: build number)')
    string(name: 'DESIRED_COUNT', defaultValue: '1', description: 'Desired tasks per service')
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Prepare env') {
      steps {
        sh 'cp -f deploy/env.example deploy/.env || true'
        sh 'sed -i.bak "" "s/^AWS_REGION=.*/AWS_REGION=${AWS_REGION}/" deploy/.env || true'
        sh 'sed -i.bak "" "s/^DESIRED_COUNT=.*/DESIRED_COUNT=${DESIRED_COUNT}/" deploy/.env || true'
        sh 'rm -f deploy/.env.bak || true'
      }
    }

    stage('Build & Deploy') {
      steps {
        // If using Jenkins AWS credentials, wrap commands with withAWS or export envs
        sh 'bash deploy/deploy.sh'
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'deploy/**', fingerprint: false, onlyIfSuccessful: false
    }
  }
}


