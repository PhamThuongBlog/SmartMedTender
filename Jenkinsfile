pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'smartmedtender'
        DOCKER_TAG = "v${BUILD_NUMBER}-${env.BRANCH_NAME}"
        MAVEN_HOME = tool 'maven-3.9'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean compile'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw test'
            }
        }

        stage('Package') {
            steps {
                sh './mvnw package -DskipTests'
            }
        }

        stage('Code Analysis') {
            steps {
                sh './mvnw jacoco:report'
                publishHTML(target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: 'JaCoCo Coverage Report'
                ])
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh 'docker compose down || true'
                sh 'docker compose up -d'
                sh '''
                    for i in $(seq 1 12); do
                        curl -sf http://localhost:8082/actuator/health && break
                        sleep 10
                    done
                '''
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/**/*.xml'
            cleanWs()
        }
        failure {
            emailext(
                subject: "[Jenkins] Build failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Build ${env.BUILD_URL} failed. Check the logs.",
                to: 'dev-team@medtender.vn'
            )
        }
    }
}
