def call(String suiteName) {
    echo "Initializing test environment for suite: '${suiteName}'"

    // Workspace preparation (Jenkins pipeline functions)
    cleanWs()
    checkout scm

    // Leverage your excellent existing shared library functions
    printBuildMetadata(suiteName)

    // Retry logic using your proven grid function
    retry(2) {
        startDockerGrid('docker-compose-grid.yml', 120, 5, 'http://selenium-hub:4444/wd/hub')  // Configurable hub URL for Docker network
    }

    echo "Test environment initialized successfully"
}