def call(String composeFile = 'docker-compose-grid.yml', int maxWaitSeconds = 120, int checkIntervalSeconds = 5, String hubUrl = 'http://localhost:4444/wd/hub') {
    // ✅ Read networkName from Jenkinsfile environment (dynamic per-branch)
    def networkName = env.NETWORK_NAME
    if (!networkName || networkName.trim().isEmpty()) {
        error "❌ 'NETWORK_NAME' environment variable not set. Jenkinsfile must set this."
    }

    try {
        // ✅ Checkout code first
        checkout scm
        
        // Sanitize JOB_NAME for Docker Compose project name
        def projectName = env.JOB_NAME
                                .toLowerCase()
                                .replaceAll(/[^a-z0-not;_-]/, '-') 

        // ❌ REMOVED all the V2 detection logic ❌

        // ✅ Self-Heal: Always try to tear down old containers/networks first.
        echo "--- Attempting pre-build cleanup for project ${projectName} ---"
        // ✅ Reverted to stable V1 commands
        sh "docker-compose -f ${composeFile} -p ${projectName} down --remove-orphans --volumes || true"
        sh "docker network rm ${networkName} || true"

        echo "🚀 Starting Docker Grid using project name: ${projectName}"
        // ✅ Network creation moved here for proper sequencing
        withEnv(["NETWORK_NAME=${networkName}"]) {
            sh "docker network create ${networkName} || true"
            // ✅ Reverted to stable V1 command (no --no-pull)
            sh "docker-compose -p ${projectName} -f ${composeFile} up -d"
        }

        echo "🔗 Connecting Jenkins agent to Grid network for health checks..."
        // Use the DOCKER_CONTAINER_ID environment variable provided by the Docker agent
        def containerId = env.DOCKER_CONTAINER_ID ?: sh(script: 'hostname', returnStdout: true).trim()
        
        if (containerId) {
            sleep time: 3, unit: 'SECONDS'
            sh "docker network connect ${networkName} ${containerId} || true"
        } else {
            echo "⚠️ Could not determine container ID for manual network connection. Proceeding, but connection may be unstable."
        }

        echo "🔍 Performing intelligent Grid health checks..."
        echo "⏳ Max wait time: ${maxWaitSeconds} seconds, Check interval: ${checkIntervalSeconds} seconds"

        def gridReady = false
        def elapsedSeconds = 0

        while (!gridReady && elapsedSeconds < maxWaitSeconds) {
            try {
                // Check if Grid Hub is responding
                def response = sh(
                    script: "curl -s -o /dev/null -w '%{http_code}' ${hubUrl}/status",
                    returnStdout: true
                ).trim()

                if (response == '200') {
                    // Additional check: verify Grid is actually ready by checking status JSON
                    def statusResponse = sh(
                        script: "curl -s ${hubUrl}/status",
                        returnStdout: true
                    ).trim()  

                    // Parse JSON response to check readiness
                    def jsonResponse = readJSON text: statusResponse
                    if (jsonResponse.value?.ready == true) {
                        gridReady = true
                        echo "✅ Selenium Grid is ready and accepting connections!"
                        break
                    }
                    echo "⏳ Grid Hub responding but not fully ready. Status: ${statusResponse}"
                } else {
                    echo "⏳ Grid Hub not responding yet (HTTP ${response})"
                }

            } catch (Exception e) {
                echo "⏳ Grid health check failed: ${e.getMessage()}"
            }

            if (!gridReady) {
                sleep time: checkIntervalSeconds, unit: 'SECONDS'
                elapsedSeconds += checkIntervalSeconds
                echo "⏳ Waited ${elapsedSeconds}/${maxWaitSeconds} seconds for Grid to be ready..."
            }
        }

        if (!gridReady) {
            error "❌ Selenium Grid failed to become ready within ${maxWaitSeconds} seconds. Grid status:"
        }

        echo "✅ Final Grid status:"
        // ✅ Reverted to stable V1 command
        sh "docker-compose -p ${projectName} -f ${composeFile} ps"

    } catch (e) {
        error "❌ Failed to start Docker Grid: ${e.getMessage()}"
    }
}