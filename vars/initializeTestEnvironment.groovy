def call(String suiteName) {
    echo "Initializing test environment for suite: '${suiteName}'"

    // Workspace preparation (Jenkins pipeline functions)
    cleanWs()
    checkout scm

    // Leverage your excellent existing shared library functions
    printBuildMetadata(suiteName)

    // Retry logic using your proven grid function
    retry(2) {
        startDockerGrid('docker-compose-grid.yml', 20)
    }

    echo "Test environment initialized successfully"
}