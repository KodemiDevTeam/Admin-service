pipeline {

    agent any

    options {
        disableConcurrentBuilds()
        timeout(time: 1, unit: 'HOURS')
    }

    environment {
        JAVA_HOME = '/opt/java/openjdk'
        MAVEN_HOME = '/usr/share/maven'
        PATH = "/opt/java/openjdk/bin:/usr/share/maven/bin:/usr/bin:/bin:/usr/local/bin"

        SONAR_PROJECT_KEY  = 'AdminService_Dev'
        SONAR_PROJECT_NAME = 'AdminService_Dev'
    }

    stages {

        /* ================= CLEAN ================= */

        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

        /* ================= CHECKOUT ================= */

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

        /* ================= TRIGGER INFO ================= */

        stage('Trigger Info') {
            steps {
                echo "Build triggered by: ${currentBuild.getBuildCauses()}"
            }
        }

        /* ================= DEBUG ================= */

        stage('Debug Workspace') {
            steps {
                sh '''
                    echo "===== WORKSPACE DEBUG ====="
                    pwd
                    ls -la
                    find . -name pom.xml
                '''
            }
        }

        /* ================= BUILD ================= */

        stage('Build (No Tests)') {
            steps {
                sh '''
                    echo "===== BUILD WITHOUT TESTS ====="

                    mvn -B clean install \
                    -Dmaven.test.skip=true \
                    -Deureka.client.enabled=false \
                    -Dspring.cloud.discovery.enabled=false
                '''
            }
        }

        /* ================= SONAR ================= */

        stage('SonarQube Analysis (No Tests)') {
            steps {
                withSonarQubeEnv('SonarQube2') {
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                        sh '''
                            echo "===== SONAR ANALYSIS ====="

                            mvn -B sonar:sonar \
                            -Dsonar.projectKey=$SONAR_PROJECT_KEY \
                            -Dsonar.projectName=$SONAR_PROJECT_NAME \
                            -Dsonar.login=$SONAR_TOKEN \
                            -Dsonar.coverage.exclusions=** \
                            -Dsonar.tests= \
                            -Dsonar.test.exclusions=** \
                            -Dsonar.exclusions=**/target/**,**/node_modules/**,**/*.log \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.scm.disabled=true
                        '''
                    }
                }
            }
        }

        /* ================= QUALITY GATE ================= */

        stage('Quality Gate') {
            steps {
                script {
                    try {
                        timeout(time: 3, unit: 'MINUTES') {
                            def qg = waitForQualityGate()
                            echo "Quality Gate Status: ${qg.status}"

                            if (qg.status != 'OK') {
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                    } catch (Exception e) {
                        echo "Quality Gate timeout → continuing"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }

        /* ================= SECURITY ================= */

        stage('OWASP Dependency Check') {
            steps {
                withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_KEY')]) {

                    sh 'echo "===== RUNNING OWASP CHECK ====="'

                    dependencyCheck(
                        additionalArguments: "--nvdApiKey ${NVD_KEY} --format CSV --out . --disableOssIndex",
                        odcInstallation: 'Default'
                    )
                }

                dependencyCheckPublisher pattern: 'dependency-check-report.csv'
            }
        }

        /* ================= ARCHIVE ================= */

        stage('Archive Reports') {
            steps {
                archiveArtifacts artifacts: 'dependency-check-report.csv',
                                 fingerprint: true
            }
        }
    }

    /* ================= POST ================= */

    post {
        success {
            echo 'SUCCESS: Build + Sonar + OWASP completed (Tests Skipped)'
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
