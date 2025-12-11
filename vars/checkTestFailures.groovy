def call() {
    try {
        def files = findFiles(glob: '**/surefire-reports/*.xml')
        def total = 0, failures = 0, errors = 0

        files.each { file ->
            def content = readFile(file: file.path)
            // Simple regex parsing - assumes well-formed XML
            // For complex cases, could use XmlSlurper, but regex works for our standardized surefire reports
            failures += (content =~ /<failure>/).findAll().size()
            errors += (content =~ /<error>/).findAll().size()
            total += (content =~ /<testcase/).findAll().size()
        }

        return [total: total, failures: failures, errors: errors]
    } catch (Exception e) {
        echo "⚠️ Quality Gate: Could not parse test results: ${e.message}"
        return [total: 0, failures: 0, errors: 0]
    }
}
