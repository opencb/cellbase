pipeline {
    agent any
    stages {

        stage ('Build') {
            options {
                timeout(time: 30, unit: 'MINUTES')
            }
            steps {
                sh 'mvn clean install -DskipTests -Dcellbase.war.name=cellbase'
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
                sh 'docker build -t cellbase-app -f cellbase-app/app/docker/cellbase-app/Dockerfile .'
            }
        }

        stage ('Publish Docker Images') {
             options {
                    timeout(time: 25, unit: 'MINUTES')
             }
             steps {
                script {
                   def images = ["cellbase", "cellbase-app"]
                   def tag = sh(returnStdout: true, script: "git rev-parse --verify HEAD").trim()
                   withDockerRegistry([ credentialsId: "wasim-docker-hub", url: "" ]) {
                       for(int i =0; i < images.size(); i++){
                           sh "docker tag '${images[i]}' opencb/'${images[i]}':${tag}"
                           sh "docker push opencb/'${images[i]}':${tag}"
                       }
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
