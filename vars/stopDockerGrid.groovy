def call(String composeFile = 'docker-compose-grid.yml') {
    try {
        def projectName = env.JOB_NAME
                                .toLowerCase()
                                .replaceAll(/[^a-z0-9_-]/, '-')

        echo "🧹 Stopping Selenium Grid for project: ${projectName}"
        sh "docker-compose -p ${projectName} -f ${composeFile} down || echo \"⚠️ Grid already stopped or not found.\""
    } catch (e) {
        echo "⚠️ Exception during grid shutdown: ${e.getMessage()}"
    }
}
