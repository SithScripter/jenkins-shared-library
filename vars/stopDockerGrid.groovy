def call(String composeFile = 'docker-compose-grid.yml') {
    try {
        echo "🧹 Stopping Selenium Grid..."
        sh "docker-compose -f ${composeFile} down || echo \"⚠️ Grid already stopped or not found.\""
    } catch (e) {
        echo "⚠️ Exception during grid shutdown: ${e.getMessage()}"
    }
}
