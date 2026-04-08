pipeline {
    agent any

    environment {
        APP_NAME = "jobreader"
        DEPLOY_PATH = "/opt/jar/jobreader"
        SERVICE_FILE = "jobreader.service"
        BUILD_VERSION = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out code...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "Building version: ${BUILD_VERSION}"
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Prepare Artifact') {
            steps {
                script {
                    def jarFile = sh(
                        script: "ls target/*.jar | grep -v 'original' | head -n 1",
                        returnStdout: true
                    ).trim()

                    echo "Found JAR: ${jarFile}"

                    sh """
                    cp ${jarFile} target/${APP_NAME}-build-${BUILD_VERSION}.jar
                    """
                }
            }
        }

        stage('Deploy Service File') {
            steps {
                echo "Deploying service file..."

                sh """
                mkdir -p ${DEPLOY_PATH}

                # Copy service file into jar folder
                cp ${SERVICE_FILE} ${DEPLOY_PATH}/

                # Link service file to systemd
                sudo ln -sf ${DEPLOY_PATH}/${SERVICE_FILE} /etc/systemd/system/${APP_NAME}.service

                # Reload systemd
                sudo systemctl daemon-reload
                """
            }
        }

        stage('Deploy Application') {
            steps {
                echo "Deploying application..."

                sh """
                mkdir -p ${DEPLOY_PATH}

                # Stop service
                sudo systemctl stop ${APP_NAME}

                # Remove old jar
                rm -f ${DEPLOY_PATH}/*.jar

                # Copy new jar
                cp target/${APP_NAME}-build-${BUILD_VERSION}.jar ${DEPLOY_PATH}/

                # Start service
                sudo systemctl start ${APP_NAME}
                """
            }
        }

        stage('Verify Service') {
            steps {
                echo "Checking service status..."
                sh "sudo systemctl status ${APP_NAME} --no-pager"
            }
        }
    }

    post {
        success {
            echo "✅ Deployment successful! Version: build-${BUILD_VERSION}"
        }
        failure {
            echo "❌ Deployment failed!"
        }
    }
}
