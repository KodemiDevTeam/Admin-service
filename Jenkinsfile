pipeline {

agent any

options {
    disableConcurrentBuilds()
    timeout(time: 1, unit: 'HOURS')
}

environment {
    SONAR_PROJECT_KEY  = 'AdminService_Dev'
    SONAR_PROJECT_NAME = 'AdminService_Dev'
}

stages {

    stage('Clean Workspace') {
        steps {
            cleanWs()
        }
    }

    stage('Checkout') {
        steps {
            checkout([
                $class: 'GitSCM',
                branches: scm.branches,
                userRemoteConfigs: scm.userRemoteConfigs,
                extensions: [
                    [$class: 'CloneOption', depth: 1, shallow: true]
                ]
            ])
        }
    }

    stage('Trigger Info') {
        steps {
            echo "Build triggered by: ${currentBuild.getBuildCauses()}"
        }
    }

    stage('Debug Workspace') {
        steps {
            bat '''
            echo ===== WORKSPACE DEBUG =====
            cd
            dir
            dir /s pom.xml
            '''
        }
    }

    stage('Build (No Tests)') {
        steps {
            bat '''
            call mvnw.cmd -B clean install ^
            -Dmaven.test.skip=true ^
            -Deureka.client.enabled=false ^
            -Dspring.cloud.discovery.enabled=false
            '''
        }
    }

    stage('SonarQube Analysis (No Tests)') {
        steps {
            withSonarQubeEnv('SonarQube2') {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    bat '''
                    call mvnw.cmd -B sonar:sonar ^
                    -Dsonar.projectKey=%SONAR_PROJECT_KEY% ^
                    -Dsonar.projectName=%SONAR_PROJECT_NAME% ^
                    -Dsonar.login=%SONAR_TOKEN% ^
                    -Dsonar.sources=src/main/java ^
                    -Dsonar.exclusions=**/target/**,**/node_modules/**,**/*.log ^
                    -Dsonar.scm.disabled=true
                    '''
                }
            }
        }
    }

    stage('Quality Gate') {
        steps {
            script {
                try {
                    timeout(time: 10, unit: 'MINUTES') {
                        def qg = waitForQualityGate abortPipeline: false
                        echo "Quality Gate Status: ${qg?.status}"

                        if (qg?.status && qg.status != 'OK') {
                            currentBuild.result = 'UNSTABLE'
                        }
                    }
                } catch (Exception e) {
                    echo "Quality Gate skipped due to Sonar issue: ${e}"
                    currentBuild.result = 'UNSTABLE'
                }
            }
        }
    }

    stage('OWASP Dependency Check') {
        steps {
            withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
                dependencyCheck(
                    additionalArguments: "--format XML --format HTML --nvdApiKey=%NVD_API_KEY%",
                    odcInstallation: 'Default'
                )
            }
        }
    }

    stage('Publish OWASP Report') {
        steps {
            dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
        }
    }

    stage('Archive Reports') {
        steps {
            archiveArtifacts artifacts: 'dependency-check-report.*', fingerprint: true
        }
    }
}

post {
    success {
        echo 'SUCCESS: Build + Sonar + OWASP completed'
    }
    unstable {
        echo 'UNSTABLE: Quality Gate issue or timeout'
    }
    failure {
        echo 'FAILED: Check logs'
    }
    always {
        echo 'Pipeline execution finished'
    }
}


}
