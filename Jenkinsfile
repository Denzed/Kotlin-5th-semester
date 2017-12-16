pipeline {
	agent any
	stages {
		stage('Test') {
			steps {
				gradle check
			}
		}
	}
	post {
		always {
			junit 'build/reports/**/*.xml'
		}
	}
}
