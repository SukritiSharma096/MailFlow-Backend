pipeline {
    agent any

    environment {
        APP_NAME = "jobreader"
        DEPLOY_PATH = "/opt/jar/jobreader"
        BUILD_VERSION = "build-${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out code from GitHub...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "Building application with version: ${BUILD_VERSION}"
                sh "mvn clean package -DskipTests"
            }
        }

        stage('Prepare Artifact') {
            steps {
                script {
                    // Find generated JAR (excluding original plain jar if multiple)
                    def jarFile = sh(
                        script: "ls target/*.jar | grep -v 'original' | head -n 1",
                        returnStdout: true
                    ).trim()

                    echo "Found JAR: ${jarFile}"

                    // Rename with build number
                    sh """
                    cp ${jarFile} target/${APP_NAME}-${BUILD_VERSION}.jar
                    """
                }
            }
        }

        stage('Deploy') {
            steps {
                echo "Deploying to ${DEPLOY_PATH}"

                sh """
                mkdir -p ${DEPLOY_PATH}
                cp target/${APP_NAME}-${BUILD_VERSION}.jar ${DEPLOY_PATH}/
                """
            }
        }

        stage('Restart Service') {
            steps {
                echo "Restarting service..."
                sh """
                sudo systemctl restart ${APP_NAME}
                """
            }
        }
    }

    post {
        success {
            echo "✅ Build ${BUILD_VERSION} deployed successfully!"
        }
        failure {
            echo "❌ Build failed!"
        }
    }
}
