pipeline {
    agent any
    stages {

        stage ('Build') {
            options {
                timeout(time: 30, unit: 'MINUTES')
            }
            steps {
                sh 'mvn clean install -DskipTests -DCELLBASE.WAR.NAME=cellbase'
            }
        }

        stage ('Validate') {
            options {
                timeout(time: 5, unit: 'MINUTES')
            }
            steps {
                sh 'mvn validate'
            }
        }

        stage ('Docker Build') {
            options {
                timeout(time: 25, unit: 'MINUTES')
            }
            steps {
                sh 'docker build -t cellbase -f cellbase-app/app/docker/cellbase/Dockerfile .'
            }
        }

        stage ('Publish Docker Images') {
             options {
                    timeout(time: 25, unit: 'MINUTES')
             }
             steps {
                script {
                   def tag = sh(returnStdout: true, script: "git rev-parse --verify HEAD").trim()
                   withDockerRegistry([ credentialsId: "wasim-docker-hub", url: "" ]) {
                           sh "docker tag cellbase opencb/cellbase:${tag}"
                           sh "docker push opencb/cellbase:${tag}"
                   }
                }
             }
        }

        stage ('Clean Docker Images') {
            options {
                timeout(time: 10, unit: 'MINUTES')
            }
            steps {
                sh 'docker system prune --force -a'
            }
        }
    }
}
