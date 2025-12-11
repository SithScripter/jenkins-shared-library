def call() {
    try {
        def files = findFiles(glob: '**/surefire-reports/*.xml')
        def total = 0, failures = 0, errors = 0

        files.each { file ->
            def content = readFile(file: file.path)
            // Simple regex parsing - supports both JUnit (surefire) and TestNG XML formats
            // For complex cases, could use XmlSlurper, but regex works for our standardized reports

            // Count failures: JUnit <failure> or TestNG status="FAIL"
            def failureCount = (content =~ /<failure>/).findAll().size()
            failureCount += (content =~ /status="FAIL"/).findAll().size()
            failures += failureCount

            // Count errors: JUnit <error> or TestNG exceptions
            def errorCount = (content =~ /<error>/).findAll().size()
            errorCount += (content =~ /<exception/).findAll().size()  // No 'def' here
            errors += errorCount

            // Count total tests: JUnit <testcase> or TestNG <test-method>
            def testCount = (content =~ /<testcase/).findAll().size()
            testCount += (content =~ /<test-method/).findAll().size()  // No 'def' here
            total += testCount
        }

        return [total: total, failures: failures, errors: errors]
    } catch (Exception e) {
        echo "⚠️ Quality Gate: Could not parse test results: ${e.message}"
        return [total: 0, failures: 0, errors: 0]
    }
}
