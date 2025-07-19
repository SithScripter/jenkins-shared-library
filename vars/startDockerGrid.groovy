def call(String composeFile = 'docker-compose-grid.yml', int waitSeconds = 20) {
    try {
        echo "🚀 Starting Docker-based Selenium Grid using ${composeFile} with job context: ${env.JOB_NAME}..."
        sh "docker-compose -p ${env.JOB_NAME} -f ${composeFile} up -d"
        echo "⏳ Waiting ${waitSeconds} seconds for Grid to stabilize..."
        sleep time: waitSeconds, unit: 'SECONDS'
		
		echo "✅ Grid status:"
		sh "docker-compose -p ${env.JOB_NAME} -f ${composeFile} ps"
		
    } catch (e) {
        error "❌ Failed to start Docker Grid: ${e.getMessage()}"
    }
}
