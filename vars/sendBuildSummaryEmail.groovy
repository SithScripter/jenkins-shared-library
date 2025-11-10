import groovy.xml.XmlUtil // Import sandbox-safe utility

def call(Map config) {
    // --- POLISH 2: Add branchName to the required config ---
    if (!config?.suiteName || !config?.branchName) {
        error "❌ suiteName and branchName are required in sendBuildSummaryEmail.groovy"
    }

    // Defensive defaults for result
    def result = (currentBuild?.currentResult ?: currentBuild?.result ?: 'SUCCESS')
    def suiteName = config.suiteName
    def branchName = config.branchName
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

    // --- POLISH 2: Branch-Aware Recipient Logic ---
    // We get the list of "prod" branches from the 'getBranchConfig' library function
    // This is excellent practice as the logic is defined in one place.
    def branchConfig = getBranchConfig()
    def emailCredsId = (branchName in branchConfig.productionCandidateBranches) 
        ? 'recipient-email-list' 
        : 'dev-recipient-email-list'

    echo "Notifying '${emailCredsId}' for branch '${branchName}'"
    
    withCredentials([ string(credentialsId: emailCredsId, variable: 'RECIPIENT_EMAILS') ]) {
        
        // Guard Clause
        if (!RECIPIENT_EMAILS?.trim()) {
            echo "⚠️ Email recipients list '${emailCredsId}' is empty. Skipping notification."
            return
        }

        echo "Attempting to send email via emailext (using JCasC config)..."
        
        // Retry Wrapper
        retry(2) {
            emailext(
                subject: subject,
                body: body,
                to: RECIPIENT_EMAILS,
                mimeType: 'text/html'
            )
        }
    }
}