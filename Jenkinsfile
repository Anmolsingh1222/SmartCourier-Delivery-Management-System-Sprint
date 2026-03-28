pipeline {
  agent any

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '10'))
    timeout(time: 60, unit: 'MINUTES')
    disableConcurrentBuilds()
  }

  parameters {
    booleanParam(name: 'PUSH_IMAGES', defaultValue: false, description: 'Push Docker images to registry')
    booleanParam(name: 'DEPLOY',      defaultValue: false, description: 'Deploy after image push')
    choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'prod'], description: 'Target environment')
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
        script {
          env.GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD 2>/dev/null || echo 'local'", returnStdout: true).trim()
          env.GIT_BRANCH_NAME  = sh(script: "git rev-parse --abbrev-ref HEAD 2>/dev/null || echo 'main'", returnStdout: true).trim()
          env.BUILD_TAG        = "${BUILD_NUMBER}-${env.GIT_COMMIT_SHORT}"
          echo "Branch: ${env.GIT_BRANCH_NAME} | Commit: ${env.GIT_COMMIT_SHORT} | Tag: ${env.BUILD_TAG}"
        }
      }
    }

    stage('Backend Tests') {
      steps {
        sh '''
          mvn clean test \
            -pl services/auth-service,services/delivery-service,services/tracking-service,services/admin-service \
            -am \
            -Dsurefire.failIfNoSpecifiedTests=false
        '''
      }
      post {
        always {
          junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
        }
      }
    }

    stage('Frontend Build') {
      steps {
        dir('frontend/web') {
          sh 'npm ci --prefer-offline'
          sh 'npm run build'
        }
      }
      post {
        always {
          archiveArtifacts artifacts: 'frontend/web/dist/**', allowEmptyArchive: true
        }
      }
    }

    stage('Compose Config Check') {
      when {
        expression {
          return sh(script: 'command -v docker >/dev/null 2>&1', returnStatus: true) == 0
        }
      }
      steps {
        sh 'docker compose -f infra/docker-compose.full.yml config > /dev/null && echo "full.yml OK"'
        sh 'docker compose -f infra/docker-compose.prod.yml config > /dev/null && echo "prod.yml OK"'
      }
    }

    stage('Build Images') {
      when {
        expression {
          return sh(script: 'command -v docker >/dev/null 2>&1', returnStatus: true) == 0
        }
      }
      steps {
        sh 'docker compose -f infra/docker-compose.full.yml build'
      }
    }

    stage('Push Images') {
      when { expression { return params.PUSH_IMAGES } }
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'dockerhub-credentials',
          usernameVariable: 'DOCKER_USER',
          passwordVariable: 'DOCKER_PASS'
        )]) {
          sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
          script {
            ['eureka-server','auth-service','delivery-service','tracking-service','admin-service','api-gateway','web'].each { svc ->
              sh """
                docker tag  infra-${svc} smartcourier/${svc}:${env.BUILD_TAG}
                docker tag  infra-${svc} smartcourier/${svc}:latest
                docker push smartcourier/${svc}:${env.BUILD_TAG}
                docker push smartcourier/${svc}:latest
              """
            }
          }
          sh 'docker logout || true'
        }
      }
    }

    stage('Deploy') {
      when {
        allOf {
          expression { return params.DEPLOY }
          expression { return params.PUSH_IMAGES }
        }
      }
      steps {
        script {
          def cf = params.ENVIRONMENT == 'prod' ? 'infra/docker-compose.prod.yml' : 'infra/docker-compose.full.yml'
          sh "docker compose -f ${cf} pull"
          sh "docker compose -f ${cf} up -d --remove-orphans"
        }
      }
    }

    stage('Health Check') {
      when { expression { return params.DEPLOY } }
      steps {
        sleep(30)
        script {
          ['8088','8081','8082','8083','8084'].each { port ->
            def code = sh(script: "curl -sf -o /dev/null -w '%{http_code}' http://localhost:${port}/actuator/health || echo 000", returnStdout: true).trim()
            echo "${code == '200' ? '✅' : '⚠️'} localhost:${port} → ${code}"
            if (code != '200') unstable("Service on port ${port} unhealthy")
          }
        }
      }
    }

  }

  post {
    success { echo "✅ Build ${env.BUILD_TAG} passed on ${env.GIT_BRANCH_NAME}" }
    failure { echo "❌ Build ${env.BUILD_TAG} failed on ${env.GIT_BRANCH_NAME}" }
    unstable { echo "⚠️ Build ${env.BUILD_TAG} unstable" }
  }
}