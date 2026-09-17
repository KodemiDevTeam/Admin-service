pipeline {

    agent any

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timeout(time: 1, unit: 'HOURS')
    }

    environment {
        SONAR_PROJECT_KEY  = 'Admin_Service'
        SONAR_PROJECT_NAME = 'Admin_Service'
    }

    stages {

        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
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
                    echo Searching for pom.xml...
                    where /r . pom.xml
                '''
            }
        }

        stage('Build (No Tests)') {
            steps {
                bat '''
                    echo ===== BUILD WITHOUT TESTS =====
                    mvn clean verify -DskipTests
                '''
            }
        }

        stage('SonarQube Analysis (No Tests)') {
            steps {

                withSonarQubeEnv('SonarQube2') {

                    withCredentials([
                        string(
                            credentialsId: 'sonar-token',
                            variable: 'SONAR_TOKEN'
                        )
                    ]) {

                        bat '''
                            echo ===== SONAR ANALYSIS =====

                            mvn sonar:sonar ^
                            -Dsonar.projectKey=%SONAR_PROJECT_KEY% ^
                            -Dsonar.projectName=%SONAR_PROJECT_NAME% ^
                            -Dsonar.host.url=%SONAR_HOST_URL% ^
                            -Dsonar.token=%SONAR_TOKEN%
                        '''
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        stage('OWASP Dependency Check') {
            steps {
                bat '''
                    echo ===== OWASP DEPENDENCY CHECK =====
                    mvn org.owasp:dependency-check-maven:check
                '''
            }
        }

        stage('Publish OWASP Report') {
            steps {
                dependencyCheckPublisher(
                    pattern: '**/dependency-check-report.xml',
                    failedTotalCritical: 0,
                    failedTotalHigh: 0
                )
            }
        }

        stage('Archive Reports') {
            steps {
                archiveArtifacts artifacts: '**/target/*.html, **/target/*.xml',
                    allowEmptyArchive: true
            }
        }
    }

    post {

        success {
            echo 'SUCCESS: Build + Sonar completed'
        }

        failure {
            echo 'FAILED: Check logs'
        }

        always {
            echo 'Pipeline execution finished'
        }
    }
}
