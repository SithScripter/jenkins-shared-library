/**
 * Shared library function: AI-powered failure analysis (Phase 5)
 *
 * Runs AiFailureAnalyzer (Phase 4) inside the CI pipeline.
 * Generates a markdown analysis report and archives it as a build artifact.
 *
 * ARCHITECTURE POSITION:
 * - Runs AFTER checkTestFailures() (quality gate)
 * - NEVER blocks the build — all failures are caught and logged
 * - Advisory output only — a markdown report, not a pass/fail decision
 *
 * GRACEFUL DEGRADATION:
 * - No Ollama → skip with warning
 * - LLM timeout → skip with warning
 * - No failures → skip (nothing to analyze)
 * - Any exception → skip with warning
 *
 * PREREQUISITES:
 * - Tests must have been executed (surefire XML must exist)
 * - For AI analysis: Ollama must be accessible from the CI agent
 *   OR ai.provider=openai with OPENAI_API_KEY set
 *
 * USAGE in Jenkinsfile:
 *   analyzeFailuresWithAi()           // defaults from config.properties
 *   analyzeFailuresWithAi(skip: true) // skip AI analysis (useful in debug builds)
 */
def call(Map config = [:]) {
    def skip = config.get('skip', false)

    if (skip) {
        echo "ℹ️ AI Failure Analysis: Skipped (disabled by parameter)"
        return
    }

    echo "🤖 AI Failure Analysis: Starting..."

    try {
        // Step 1: Check if surefire XML exists (tests must have run)
        def xmlFiles = findFiles(glob: '**/surefire-reports/testng-results.xml')
        if (xmlFiles.size() == 0) {
            echo "ℹ️ AI Failure Analysis: No surefire XML found. Skipping."
            return
        }

        // Step 2: Quick check — are there any failures to analyze?
        def xmlContent = readFile(file: xmlFiles[0].path)
        def failedMatch = (xmlContent =~ /failed="(\d+)"/)
        def failedCount = failedMatch ? failedMatch[0][1].toInteger() : 0

        if (failedCount == 0) {
            echo "✅ AI Failure Analysis: No failures detected. Skipping."
            return
        }

        echo "🔍 AI Failure Analysis: ${failedCount} failure(s) detected. Analyzing..."

        // Step 3: Run AiFailureAnalyzer via Maven exec
        // Uses test classpath since AiFailureAnalyzer is in src/test/java
        def exitCode = sh(
            script: '''
                mvn exec:java \
                    -Dexec.mainClass="com.demo.flightbooking.utils.FailureAnalysisDemo" \
                    -Dexec.classpathScope=test \
                    -q \
                    2>&1 || true
            ''',
            returnStatus: true
        )

        // Step 4: Check if report was generated
        def reportExists = fileExists('target/failure-analysis-report.md')

        if (reportExists) {
            echo "✅ AI Failure Analysis: Report generated successfully."

            // Step 5: Archive the report as a build artifact
            archiveArtifacts(
                artifacts: 'target/failure-analysis-report.md',
                allowEmptyArchive: true,
                fingerprint: false
            )

            echo "📎 AI Failure Analysis: Report archived as build artifact."
        } else {
            echo "⚠️ AI Failure Analysis: Report not generated (LLM may be unavailable). Pipeline continues."
        }

    } catch (Exception e) {
        // NEVER fail the build because of AI analysis
        echo "⚠️ AI Failure Analysis: Skipped due to error: ${e.message}"
        echo "⚠️ This does not affect test results or quality gate."
    }
}
