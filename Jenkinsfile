pipeline {
  agent any

  options {
    timestamps()
    skipDefaultCheckout(true)
  }

  parameters {
    booleanParam(
      name: 'RUN_DOCKER_STAGES',
      defaultValue: false,
      description: 'Enable Docker compose validation and image build stages (requires docker CLI/daemon on Jenkins agent)'
    )
  }

  environment {
    APP_NAME = 'smartcourier'
  }

  stages {
    stage('Sync Workspace') {
      steps {
        sh '''
          set -eu
          rm -rf ./* ./.??* || true
          cp -a /workspace/smartcouriera/. .
        '''
      }
    }

    stage('Backend Test') {
      steps {
        sh 'mvn -q clean test'
      }
    }

    stage('Frontend Build') {
      steps {
        dir('frontend/web') {
          sh 'npm ci'
          sh 'npm run build'
        }
      }
    }

    stage('Docker Compose Config Check') {
      when {
        expression {
          return params.RUN_DOCKER_STAGES && (sh(script: 'command -v docker >/dev/null 2>&1', returnStatus: true) == 0)
        }
      }
      steps {
        sh 'docker compose -f infra/docker-compose.full.yml config > /dev/null'
      }
    }

    stage('Build Images') {
      when {
        expression {
          return params.RUN_DOCKER_STAGES && (sh(script: 'command -v docker >/dev/null 2>&1', returnStatus: true) == 0)
        }
      }
      steps {
        sh 'docker compose -f infra/docker-compose.full.yml build'
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'frontend/web/dist/**', allowEmptyArchive: true
      junit testResults: '**/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml', allowEmptyResults: true
    }
  }
}
