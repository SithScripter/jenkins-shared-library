def call(String composeFile = 'docker-compose-grid.yml') {
    try {
        echo "🧹 Stopping Docker-based Selenium Grid using job context: ${env.JOB_NAME}..."
        sh "docker-compose -p ${env.JOB_NAME} -f ${composeFile} down || echo \"⚠️ Grid already stopped or not found.\""
    } catch (e) {
        echo "⚠️ Exception during grid shutdown: ${e.getMessage()}"
    }
}
