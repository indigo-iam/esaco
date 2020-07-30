#!/usr/bin/env groovy
@Library('sd')_

def kubeLabel = getKubeLabel()

pipeline {
  agent none
  
  options {
    ansiColor('xterm')
    buildDiscarder(logRotator(numToKeepStr: '5'))
    skipDefaultCheckout()
    timeout(time: 1, unit: 'HOURS')
    timestamps()
  }
  
  stages {
    stage('kubernetes') {
      agent {
        kubernetes {
          label "${kubeLabel}"
          cloud 'Kube mwdevel'
          defaultContainer 'runner'
          inheritFrom 'ci-template'
        }
      }
      stages {
        stage('prepare') {
      
          steps {
            deleteDir()
            checkout scm
          }
        }
        stage('compile') {
          steps {
            sh 'mvn -B clean compile'
          }
        }
        stage('test') {
          steps {
            sh 'mvn -B clean test'
            junit '**/target/surefire-reports/TEST-*.xml'
            jacoco()
          }
        }
        stage('package') {
          steps {
            sh 'mvn -DskipTests clean package'
            stash includes: 'esaco-app/target/esaco-app-*.jar', name: 'esaco-artifacts'
            sh 'sh utils/print-pom-version.sh > esaco-version'
            stash includes: 'esaco-version', name: 'esaco-version'
          }
        }
    
        stage('analysis') {
          when {
           anyOf { branch 'master'; branch 'develop' }
           environment name: 'CHANGE_URL', value: ''
          }
          steps {
            script {
              def opts = '-Dmaven.test.failure.ignore -DfailIfNoTests=false -DskipTests'
              def checkstyle_opts = 'checkstyle:check -Dcheckstyle.config.location=google_checks.xml'

              sh "mvn clean package -U ${opts} ${checkstyle_opts}"
            }
          }
        }
    
        stage('deploy') {
          steps {
            sh "mvn clean -DskipTests -U -B deploy"
          }
        }
      }
    }
    
    stage('docker-images') {
      agent { label 'docker' }
      steps {
        unstash 'esaco-artifacts'
        unstash 'esaco-version'
        sh'''
        POM_VERSION=$(cat esaco-version) /bin/bash esaco-app/docker/build-image.sh
        POM_VERSION=$(cat esaco-version) /bin/bash esaco-app/docker/push-image.sh
        '''
        script {
          if (env.BRANCH_NAME == 'master') {
            sh '''
            unset DOCKER_REGISTRY_HOST
            POM_VERSION=$(cat esaco-version) /bin/bash esaco-app/docker/push-image.sh
            '''
          }
        }
      }
    }

    stage('result'){
      steps {
        script { currentBuild.result = 'SUCCESS' }
      }
    }
  }
  
  post {
    failure {
      slackSend color: 'danger', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} Failure (<${env.BUILD_URL}|Open>)"
    }
    
    unstable {
      slackSend color: 'warning', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} Unstable (<${env.BUILD_URL}|Open>)"
    }
    
    changed {
      script{
        if('SUCCESS'.equals(currentBuild.result)) {
          slackSend color: 'good', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} Back to normal (<${env.BUILD_URL}|Open>)"
        }
      }
    }
  }
}
