import groovy.xml.XmlUtil // Import sandbox-safe utility

def call(Map config) {
    if (!config?.suiteName) {
        error "❌ suiteName is required in sendBuildSummaryEmail.groovy"
    }

    // Defensive defaults for result
    def result = (currentBuild?.currentResult ?: currentBuild?.result ?: 'SUCCESS')
    def suiteName = config.suiteName
    def summaryFile = "reports/${suiteName}-failure-summary.txt"

    // This URL matches your 'reportName: "Test Dashboard"'
    def reportURL = "${env.BUILD_URL}Test_20Dashboard/"

    def failureSummary = fileExists(summaryFile)
        ? readFile(summaryFile).trim()
        : "⚠️ No failure summary available. Possible report generation issue."

    def subject
    def bodyTop

    if (result == 'SUCCESS') {
        subject = "✅ SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        bodyTop = """
            <p>Build was successful.</p>
            <p><b><a href='${reportURL}'>📄 View Test Dashboard</a></b></p>
        """
    } else {
        subject = "❌ ${result}: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        bodyTop = """
            <p><b>${result}: The build has issues.</b></p>
            <p><b>Failure Summary:</b></p>
            <pre style="background-color:#F5F5F5;border:1px solid #E0E0E0;padding:10px;font-family:monospace;white-space:pre-wrap;">${failureSummary}</pre>
            <p><b><a href='${reportURL}'>📄 View Test Dashboard</a></b></p>
        """
    }

    // Optional: add a snippet of the console log on failure
    def consoleTail = ''
    if (result != 'SUCCESS') {
        try {
            consoleTail = currentBuild?.rawBuild?.getLog(200)?.join('\n') ?: ''
        } catch (ignored) { /* best-effort */ }
    }

    def body = (result == 'SUCCESS') ? bodyTop : """${bodyTop}
        <details>
          <summary>Console tail (last ~200 lines)</summary>
          <pre style="background-color:#FAFAFA;border:1px solid #EEE;padding:10px;font-family:monospace;white-space:pre-wrap;">${XmlUtil.escapeXml(consoleTail)}</pre>
        </details>
    """

    // Bind only the recipient list. SMTP settings now come from JCasC.
    withCredentials([ string(credentialsId: config.emailCredsId, variable: 'RECIPIENT_EMAILS') ]) {
        
        // --- RELIABILITY FIX 1: Guard Clause ---
        if (!RECIPIENT_EMAILS?.trim()) {
            echo "⚠️ Email recipients list is empty. Skipping notification."
            return // Stop execution if no one is set to receive the email
        }

        echo "Attempting to send email via emailext (using JCasC config)..."
        
        // --- RELIABILITY FIX 2: Retry Wrapper ---
        retry(2) {
            emailext(
                subject: subject,
                body: body,
                to: RECIPIENT_EMAILS,
                mimeType: 'text/html'
                // --- ATTACHMENTS REMOVED ---
            )
        }
    }
}