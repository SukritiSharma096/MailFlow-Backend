pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from GitHub...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building the application (tests skipped)...'
                // Example: Maven build skipping tests
                // If using Maven:
                sh 'mvn clean install -DskipTests'

                // If using Gradle:
                // sh './gradlew build -x test'

                // If using npm:
                // sh 'npm install && npm run build'
            }
        }
    }

    post {
        success {
            echo 'Build completed successfully!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}
