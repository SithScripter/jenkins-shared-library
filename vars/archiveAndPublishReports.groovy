def call() {
    archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
    archiveArtifacts artifacts: 'logs/**', allowEmptyArchive: true

    publishHTML([
        reportName: 'Chrome Report',
        reportDir: 'reports/chrome',
        reportFiles: 'index.html',
        keepAll: true, alwaysLinkToLastBuild: true, allowMissing: false
    ])
    publishHTML([
        reportName: 'Firefox Report',
        reportDir: 'reports/firefox',
        reportFiles: 'index.html',
        keepAll: true, alwaysLinkToLastBuild: true, allowMissing: false
    ])
    publishHTML([
        reportName: 'Cumulative Dashboard',
        reportDir: 'reports',
        reportFiles: 'index.html',
        keepAll: true, alwaysLinkToLastBuild: true, allowMissing: false
    ])
}
