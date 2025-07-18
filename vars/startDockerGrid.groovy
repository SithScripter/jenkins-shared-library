def call(String composeFile = 'docker-compose-grid.yml', int waitSeconds = 20) {
    try {
        echo "🚀 Starting Docker-based Selenium Grid using ${composeFile}..."
        bat "docker-compose -f ${composeFile} up -d"
        echo "⏳ Waiting ${waitSeconds} seconds for Grid to stabilize..."
        sleep time: waitSeconds, unit: 'SECONDS'
    } catch (e) {
        error "❌ Failed to start Docker Grid: ${e.getMessage()}"
    }
}
