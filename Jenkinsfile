pipeline {
    agent any
    
    environment {
        
        DOCKER_REGISTRY = 'docker.io/ankitchauhan18'
    }
    
    stages {
        stage('Build Docker Image') {
            steps {
                script {
                    
                    docker.build("${DOCKER_REGISTRY}/springboot:lts")
                }
            }
        }
        
        stage('Push Docker Image') {
            steps {
                script {
                   
                    docker.withRegistry("${DOCKER_REGISTRY}", 'your-docker-credentials') {
                        docker.image("${DOCKER_REGISTRY}/springboot:lts").push()
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
            sh "docker rmi -f ${DOCKER_REGISTRY}/springboot:lts"
        }
    }
}
