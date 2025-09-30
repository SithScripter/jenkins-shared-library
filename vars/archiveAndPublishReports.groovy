def call() {
    archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
    archiveArtifacts artifacts: 'logs/**', allowEmptyArchive: true

    publishHTML([
        reportName: 'Cumulative Dashboard',
        reportDir: 'reports',
        reportFiles: 'index.html',
        keepAll: true, alwaysLinkToLastBuild: true, allowMissing: false
    ])
}
