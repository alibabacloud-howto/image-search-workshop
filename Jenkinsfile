pipeline {
    agent any

    parameters {
        string(defaultValue: 'http://10.218.129.173:9000', description: '', name: 'sonarHostUrl')
        string(defaultValue: '5f9165d80185d29d17a8b8c837fb053c257981c8', description: '', name: 'sonarLogin')
        string(defaultValue: 'marc.plouhinec@10.125.11.94', description: '', name: 'binRepoUriAuthority')
        string(defaultValue: 'marc.plouhinec@10.125.0.65', description: '', name: 'developServerUriAuthority')
        string(defaultValue: 'root@47.74.242.98', description: '', name: 'masterServerUriAuthority')
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Preparation') {
            steps {
                checkout scm
            }
        }
        stage('Compile') {
            steps {
                sh "mvn -Dmaven.test.failure.ignore clean compile"
            }
        }
        stage('Test and Sonar') {
            steps {
                sh "mvn -Dmaven.test.failure.ignore -Dsonar.branch=${env.BRANCH_NAME} -Dsonar.host.url=${params.sonarHostUrl} -Dsonar.login=${params.sonarLogin} clean install sonar:sonar"
            }
        }
        stage('Build RPM') {
            steps {
                sh "mvn -Dmaven.test.failure.ignore rpm:rpm"
            }
        }
        stage('Distribute binaries') {
            steps {
                sh "ssh ${params.binRepoUriAuthority} mkdir -p /var/www/repository/web-image-search-engine/${env.BRANCH_NAME}/"

                sh "scp target/rpm/web-image-search-engine/RPMS/noarch/web-image-search-engine-*.noarch.rpm ${params.binRepoUriAuthority}:/var/www/repository/web-image-search-engine/${env.BRANCH_NAME}/"
                sh "ssh ${params.binRepoUriAuthority} ln -sf /var/www/repository/web-image-search-engine/${env.BRANCH_NAME}/`ls -t target/rpm/web-image-search-engine/RPMS/noarch/ | head -1` /var/www/repository/web-image-search-engine/${env.BRANCH_NAME}/web-image-search-engine-latest.noarch.rpm"

                script {
                    env.DISTDATE = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                }
                sh "scp target/web-image-search-engine.jar ${params.binRepoUriAuthority}:/var/www/repository/web-image-search-engine/${env.BRANCH_NAME}/web-image-search-engine-${env.DISTDATE}.jar"
                sh "ssh ${params.binRepoUriAuthority} ln -sf /var/www/repository/web-image-search-engine/${env.BRANCH_NAME}/web-image-search-engine-${env.DISTDATE}.jar /var/www/repository/web-image-search-engine/${env.BRANCH_NAME}/web-image-search-engine-latest.jar"
            }
        }
        stage('Deploy develop') {
            when {
                expression { env.BRANCH_NAME == 'develop' }
            }
            steps {
                sh "ssh ${params.developServerUriAuthority} sudo systemctl stop web-image-search-engine.service"
                sh "ssh ${params.developServerUriAuthority} sudo yum -y remove web-image-search-engine.noarch"

                sh "scp target/rpm/web-image-search-engine/RPMS/noarch/web-image-search-engine-*.noarch.rpm ${params.developServerUriAuthority}:/home/marc.plouhinec/"
                sh "ssh ${params.developServerUriAuthority} sudo yum -y install `ls -t target/rpm/web-image-search-engine/RPMS/noarch/ | head -1`"

                sh "ssh ${params.developServerUriAuthority} sudo systemctl start web-image-search-engine.service"
            }
        }
        stage('Deploy master') {
            when {
                expression { env.BRANCH_NAME == 'master' }
            }
            steps {
                sh "ssh ${params.masterServerUriAuthority} sudo systemctl stop web-image-search-engine.service"
                sh "ssh ${params.masterServerUriAuthority} sudo yum -y remove web-image-search-engine.noarch"

                sh "scp target/rpm/web-image-search-engine/RPMS/noarch/web-image-search-engine-*.noarch.rpm ${params.masterServerUriAuthority}:/root/"
                sh "ssh ${params.masterServerUriAuthority} sudo yum -y install `ls -t target/rpm/web-image-search-engine/RPMS/noarch/ | head -1`"

                sh "ssh ${params.masterServerUriAuthority} sudo systemctl start web-image-search-engine.service"
            }
        }
        stage('Results') {
            steps {
                junit '**/target/surefire-reports/TEST-*.xml'
                archive 'target/*.jar'
                archive 'target/rpm/web-image-search-engine/RPMS/noarch/*.rpm'
            }
        }
    }
}