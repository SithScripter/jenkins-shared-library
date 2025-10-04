def call(String composeFile = 'docker-compose-grid.yml', int waitSeconds = 20) {
    try {
        // Sanitize JOB_NAME for Docker Compose project name
        def projectName = env.JOB_NAME
                                .toLowerCase()
                                .replaceAll(/[^a-z0-9_-]/, '-') // replaces `/` and other disallowed characters

        echo "🚀 Starting Docker Grid using project name: ${projectName}"
        sh "docker-compose -p ${projectName} -f ${composeFile} up -d"
        echo "⏳ Waiting ${waitSeconds} seconds for Grid to stabilize..."
        sleep time: waitSeconds, unit: 'SECONDS'
		echo "✅ Grid status:"
		sh "docker-compose -p ${projectName} -f ${composeFile} ps"

    } catch (e) {
        error "❌ Failed to start Docker Grid: ${e.getMessage()}"
    }
}
