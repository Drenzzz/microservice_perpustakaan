pipeline {
    agent any
    
    triggers {
        pollSCM('* * * * *')
    }
    
    tools {
        maven 'Maven'
        jdk 'JDK21'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Cloning repository...'
                checkout scm
            }
        }
        
        stage('Build Anggota Service') {
            steps {
                echo 'Building Anggota Service...'
                dir('anggota') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        
        stage('Test Anggota Service') {
            steps {
                echo 'Testing Anggota Service...'
                dir('anggota') {
                    sh 'mvn test'
                }
            }
        }
        
        stage('Build Buku Service') {
            steps {
                echo 'Building Buku Service...'
                dir('buku') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        
        stage('Test Buku Service') {
            steps {
                echo 'Testing Buku Service...'
                dir('buku') {
                    sh 'mvn test'
                }
            }
        }
        
        stage('Build Peminjaman Service') {
            steps {
                echo 'Building Peminjaman Service...'
                dir('peminjaman') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        
        stage('Test Peminjaman Service') {
            steps {
                echo 'Testing Peminjaman Service...'
                dir('peminjaman') {
                    sh 'mvn test'
                }
            }
        }
        
        stage('Build Pengembalian Service') {
            steps {
                echo 'Building Pengembalian Service...'
                dir('pengembalian') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        
        stage('Test Pengembalian Service') {
            steps {
                echo 'Testing Pengembalian Service...'
                dir('pengembalian') {
                    sh 'mvn test'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying services...'
                
                // Notify Logstash - Deployment Start
                sh(script: '''
                    bash -c "echo '{\\"service\\": \\"jenkins-pipeline\\", \\"message\\": \\"Deployment Started\\", \\"event\\": \\"deployment_start\\"}' > /dev/tcp/localhost/5000" || true
                ''', returnStatus: true)
                
                // Cleanup existing containers explicitly to avoid conflicts
                sh(script: 'docker-compose down --remove-orphans', returnStatus: true)
                
                // Force remove common containers if they still exist (handling the Conflict error)
                sh(script: 'docker rm -f server-eureka api-gateway anggota-service buku-service peminjaman-service pengembalian-service || true', returnStatus: true)
                
                // Deploy
                sh 'docker-compose up -d --build --force-recreate'
                
                // Notify Logstash - Deployment Finish
                sh(script: '''
                    bash -c "echo '{\\"service\\": \\"jenkins-pipeline\\", \\"message\\": \\"Deployment Finished\\", \\"event\\": \\"deployment_finish\\"}' > /dev/tcp/localhost/5000" || true
                ''', returnStatus: true)
            }
        }
    }
    
    post {
        success {
            echo '✅ All services built and tested successfully!'
        }
        failure {
            echo '❌ Build or test failed!'
        }
        always {
            echo 'Pipeline completed.'
        }
    }
}
