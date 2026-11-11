pipeline {
  agent none

  options {
    timestamps()
  }

  parameters {
    booleanParam(
      name: 'PUBLISH',
      defaultValue: true,
      description: 'Publish the p2 repository to p2.zuhriutama.home'
    )
  }

  environment {
    P2_HOST = 'p2.zuhriutama.home'
    P2_DIR  = '/var/www/p2/idempiere/idempiere-log-viewer'
    P2_REPO = 'id.rekaestudigital.idempiere.log-viewer.p2/target/repository'
  }

  stages {

    stage('Build') {
      agent {
        docker {
          image 'maven:3.9.9-eclipse-temurin-17'
          args '-u 0:0 --add-host=p2.zuhriutama.home:192.168.100.11 -v /root/.m2:/root/.m2'
        }
      }
      steps {
        git url: 'https://github.com/uthadehikaru/idempiere-log-viewer', branch: 'main'
        sh 'mvn -version && mvn clean verify'
      }
      post {
        success {
          archiveArtifacts artifacts: "id.rekaestudigital.idempiere.log-viewer/target/*.jar, ${env.P2_REPO}/**",
                           excludes: '**/*-sources.jar',
                           fingerprint: true
          stash name: 'p2-repository', includes: "${env.P2_REPO}/**"
        }
      }
    }

    stage('Publish p2 Repository') {
      when { expression { params.PUBLISH } }
      agent {
        docker {
          image 'alpine:3.20'
          args '-u 0:0 --add-host=p2.zuhriutama.home:192.168.100.11'
        }
      }
      steps {
        unstash 'p2-repository'
        withCredentials([
          sshUserPrivateKey(
            credentialsId: 'p2-ssh',
            keyFileVariable: 'SSH_KEY',
            usernameVariable: 'SSH_USER'
          )
        ]) {
          sh '''
            set -e

            apk add --no-cache rsync openssh

            mkdir -p /root/.ssh
            chmod 700 /root/.ssh
            ssh-keyscan -H ${P2_HOST} >> /root/.ssh/known_hosts || true

            ssh -i ${SSH_KEY} -o BatchMode=yes ${SSH_USER}@${P2_HOST} \
              "mkdir -p ${P2_DIR}"

            rsync -avz --delete \
              -e "ssh -i ${SSH_KEY} -o BatchMode=yes" \
              ${P2_REPO}/ \
              ${SSH_USER}@${P2_HOST}:${P2_DIR}/
          '''
        }
      }
    }
  }

  post {
    success {
      echo '✅ idempiere-log-viewer built and published'
    }
    failure {
      echo '❌ idempiere-log-viewer build/publish failed'
    }
  }
}
