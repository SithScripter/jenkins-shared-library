    def call(Map config) {
        if (!config.suiteName) {
            error "❌ suiteName is required in sendBuildSummaryEmail.groovy"
        }

        def suiteName = config.suiteName
        def branchName = config.branchName ?: 'main'
        def summaryFile = "reports/${suiteName}-failure-summary.txt"
        def reportURL = "${env.BUILD_URL}Cumulative-Dashboard/"

        def failureSummary = fileExists(summaryFile) ? readFile(summaryFile).trim() : "⚠️ No failure summary available. Possible report generation issue."

        def subject, body
        if (currentBuild.currentResult == 'SUCCESS') {
            subject = "✅ SUCCESS: Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}"
            body = """
                <p>Build was successful.</p>
                <p><b><a href='${reportURL}'>📄 View Cumulative Dashboard</a></b></p>
            """
        } else {
            subject = "❌ FAILURE: Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}"
            body = """
                <p><b>WARNING: The build has failed.</b></p>
                <p><b>Failure Summary:</b></p>
                <pre style="background-color:#F5F5F5; border:1px solid #E0E0E0; padding:10px; font-family:monospace;">${failureSummary}</pre>
                <p><b><a href='${reportURL}'>  View Cumulative Dashboard</a></b></p>
            """
        }

        // Use different credentials based on branch
        def credentialId = branchName.startsWith('feature/') || branchName.startsWith('bugfix/') || branchName != 'main' 
            ? 'dev-recipient-email-list' 
            : 'recipient-email-list'

        withCredentials([string(credentialsId: credentialId, variable: 'RECIPIENT_EMAILS')]) {
            emailext(
                subject: subject,
                body: body,
                to: RECIPIENT_EMAILS,
                mimeType: 'text/html'
            )
        }
    }