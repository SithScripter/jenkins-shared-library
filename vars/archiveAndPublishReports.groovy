def call() {
    archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
    archiveArtifacts artifacts: 'logs/**', allowEmptyArchive: true

    // 🔹 If smoke report exists, publish it
    if (fileExists("reports/smoke/smoke-smoke-report.html")) {
        publishHTML([
            reportName: 'Smoke Report',
            reportDir: 'reports/smoke',
            reportFiles: 'smoke-smoke-report.html',
            keepAll: true,
            alwaysLinkToLastBuild: true,
            allowMissing: false
        ])
    }

    // 🔹 Only publish Chrome report if folder exists
    if (fileExists("reports/chrome/index.html")) {
        publishHTML([
            reportName: 'Chrome Report',
            reportDir: 'reports/chrome',
            reportFiles: 'index.html',
            keepAll: true,
            alwaysLinkToLastBuild: true,
            allowMissing: false
        ])
    }

    // 🔹 Only publish Firefox report if folder exists
    if (fileExists("reports/firefox/index.html")) {
        publishHTML([
            reportName: 'Firefox Report',
            reportDir: 'reports/firefox',
            reportFiles: 'index.html',
            keepAll: true,
            alwaysLinkToLastBuild: true,
            allowMissing: false
        ])
    }

    // 🔹 Always try publishing the cumulative dashboard
    if (fileExists("reports/index.html")) {
        publishHTML([
            reportName: 'Cumulative Dashboard',
            reportDir: 'reports',
            reportFiles: 'index.html',
            keepAll: true,
            alwaysLinkToLastBuild: true,
            allowMissing: false
        ])
    }
}
