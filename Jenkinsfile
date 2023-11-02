pipeline {
    agent any
    
    environment {
        
        DOCKER_REGISTRY = 'ankitchauhan18'
    }
    
    stages {
        stage('Build Docker Image') {
            steps {
                script {
                    
                    docker.build("ankitchauhan18/springboot:latest")
                }
            }
        }
        
        stage('Push Docker Image') {
            steps {
                script {
                   
                    docker.withRegistry("${DOCKER_REGISTRY}", 'your-docker-credentials') {
                        docker.image("ankitchauhan18/springboot:latest").push()
                    }
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    
                    sh "kubectl apply -f kubernetes/deployment.yaml"
                }
            }
        }
        
        stage('Cleanup') {
            steps {
                script {
                    
                    echo "done"
                }
            }
        }
    }
    
    post {
        always {
            // Clean up Docker images
            sh "docker rmi -f ankitchauhan18/springboot:latest"
        }
    }
}
