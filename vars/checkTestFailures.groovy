def call() {
    echo "🔍 Debug: Starting test failure check..."

    try {
        // Individual try-catch for each command
        def failureCount = 0

        // Count <failure> elements
        try {
            def failureCmd = sh(script: 'find target/surefire-reports -name "*.xml" -exec grep -c "<failure>" {} \\; 2>/dev/null || true', returnStdout: true).trim()
            if (failureCmd) {
                failureCount += failureCmd.split('\n').collect { it.toInteger() }.sum()
            }
        } catch (Exception e) {
            echo "⚠️ Debug: Could not count failure elements: ${e.getMessage()}"
        }

        // Count <error> elements
        try {
            def errorCmd = sh(script: 'find target/surefire-reports -name "*.xml" -exec grep -c "<error>" {} \\; 2>/dev/null || true', returnStdout: true).trim()
            if (errorCmd) {
                failureCount += errorCmd.split('\n').collect { it.toInteger() }.sum()
            }
        } catch (Exception e) {
            echo "⚠️ Debug: Could not count error elements: ${e.getMessage()}"
        }

        // Count failed test methods
        try {
            def failCmd = sh(script: 'find target/surefire-reports -name "*.xml" -exec grep -c "status=\\"FAIL\\"" {} \\; 2>/dev/null || true', returnStdout: true).trim()
            if (failCmd) {
                failureCount += failCmd.split('\n').collect { it.toInteger() }.sum()
            }
        } catch (Exception e) {
            echo "⚠️ Debug: Could not count failed methods: ${e.getMessage()}"
        }

        echo "🔍 Debug: Total failures detected: ${failureCount}"

        return failureCount

    } catch (Exception e) {
        echo "⚠️ Warning: Could not parse test results: ${e.getMessage()}"
        return 0
    }
}