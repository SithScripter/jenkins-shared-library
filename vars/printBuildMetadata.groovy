def call(String suiteName = '') {
    echo "================================================="
    echo "         BUILD & TEST METADATA"
    echo "================================================="
    echo "Job: ${env.JOB_NAME}, Build: ${env.BUILD_NUMBER}, Branch: ${env.BRANCH_NAME}"
    if (suiteName) {
        echo "Suite: ${suiteName}"
    }
    echo "Triggered by: ${env.BUILD_USER ?: 'Unknown'}"
    echo "================================================="
}
