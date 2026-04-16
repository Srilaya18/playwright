nidhireddy
Cnidhi#1120
ai t


cheemarla.srinidhi2023@vitstudent.ac.in
Nidhi#1120


stage('Deploy to Kubernetes') {
            steps {
                bat 'kubectl apply -f deployment.yaml --validate=false || exit 0'
            }
